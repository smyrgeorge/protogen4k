package io.github.smyrgeorge.datasuite.converter.protobuf

import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoFile
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoOrder
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoSkip
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ClassDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.FieldDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.FieldIndexAssigner
import io.github.smyrgeorge.datasuite.converter.protobuf.types.FileDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ProtoDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.TypeDef
import io.github.smyrgeorge.datasuite.converter.protobuf.util.FileWriter
import io.github.smyrgeorge.datasuite.converter.protobuf.util.arguments
import io.github.smyrgeorge.datasuite.converter.protobuf.util.canonicalName
import io.github.smyrgeorge.datasuite.converter.protobuf.util.ensureNoDuplicateIndex
import io.github.smyrgeorge.datasuite.converter.protobuf.util.findAnnotation
import io.github.smyrgeorge.datasuite.converter.protobuf.util.findAnnotationOptional
import io.github.smyrgeorge.datasuite.converter.protobuf.util.hasAnnotation
import io.github.smyrgeorge.datasuite.converter.protobuf.util.isEnum
import io.github.smyrgeorge.datasuite.converter.protobuf.util.isNullable
import io.github.smyrgeorge.datasuite.converter.protobuf.util.isPrimitive
import io.github.smyrgeorge.datasuite.converter.protobuf.util.jackson
import io.github.smyrgeorge.datasuite.converter.protobuf.util.kClass
import io.github.smyrgeorge.datasuite.converter.protobuf.util.mapTypes
import io.github.smyrgeorge.datasuite.converter.protobuf.util.name
import io.github.smyrgeorge.datasuite.converter.protobuf.util.packageName
import io.github.smyrgeorge.datasuite.converter.protobuf.util.repeatedTypes
import io.github.smyrgeorge.datasuite.converter.protobuf.util.skipPackages
import io.github.smyrgeorge.datasuite.converter.protobuf.util.toMeta
import io.github.smyrgeorge.datasuite.converter.protobuf.util.type
import io.github.smyrgeorge.datasuite.converter.protobuf.util.typeArguments
import io.github.smyrgeorge.datasuite.converter.protobuf.util.types
import io.github.smyrgeorge.datasuite.converter.protobuf.util.typesWithWrappers
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

@Suppress("MemberVisibilityCanBePrivate", "unused")
class KotlinClassToProtobuf(
    private val useWrapperTypes: Boolean = false,
    private val defaultProtoFile: String = "default.proto",
    private val fieldIndexAssigner: FieldIndexAssigner = FieldIndexAssigner.ANNOTATION,
    private val protoPackage: String,
) {

    private val kotlinEnumClassToProtobuf = KotlinEnumClassToProtobuf(defaultProtoFile, fieldIndexAssigner)

    fun convert(aClass: KClass<*>, oldGeneration: File): ProtoDef =
        convert(listOf(aClass), oldGeneration)

    fun convert(classes: List<KClass<*>>, oldGeneration: File): ProtoDef {
        val mapper = jackson()
        val old = try {
            val content = oldGeneration.readText()
            mapper.readValue(content, ProtoDef::class.java)
        } catch (_: Exception) {
            ProtoDef()
        }
        val res = convert(classes, old)
        val json = mapper.writeValueAsString(res)
        oldGeneration.writeText(json)
        return res
    }

    fun convert(aClass: KClass<*>, oldGeneration: ProtoDef? = null): ProtoDef =
        convert(listOf(aClass), oldGeneration)

    fun convert(classes: List<KClass<*>>, oldGeneration: ProtoDef? = null): ProtoDef {
        if (fieldIndexAssigner == FieldIndexAssigner.OLD_GENERATION && oldGeneration == null)
            error("Old generation is required for FieldIndexAssigner.OLD_GENERATION.")

        val res = mutableMapOf<String, ClassDef>()
        classes.forEach { convert(res, it, oldGeneration) }

        return ProtoDef(
            files = res.values.groupBy { it.meta.name }.map { (name, classes) ->
                val imports: List<String> = classes.asSequence()
                    .map { c -> c.fields.mapNotNull { if (it is FieldDef.Generic) it.import else null } }
                    .flatten()
                    .distinct()
                    .filter { f -> f != name }
                    .sorted()
                    .toList()

                FileDef(name, imports, classes, protoPackage)
            }
        )
    }

    private fun convert(
        acc: MutableMap<String, ClassDef>,
        aClass: KClass<*>,
        oldGeneration: ProtoDef?
    ): Map<String, ClassDef> {

        fun skip(aClass: KClass<*>) =
            aClass.hasAnnotation(ProtoSkip::class)
                    || acc.contains(aClass.canonicalName())
                    || aClass.isPrimitive()
                    || skipPackages.any { aClass.java.name.startsWith(it) }

        // Skip some classes.
        if (skip(aClass)) return acc

        println("KotlinClassToProtobuf :: Converting class ${aClass.name()}")

        // Convert current class
        val res = if (aClass.isEnum()) {
            kotlinEnumClassToProtobuf.convert(aClass, oldGeneration)
        } else {
            // Generate all sub-classes.
            aClass.sealedSubclasses.forEach { convert(acc, it, oldGeneration) }
            val sealedSubclasses = acc.entries
                .map { it.value }
                .filter { aClass.sealedSubclasses.any { sub -> sub.name() == it.name } }

            ClassDef(
                name = aClass.name(),
                packageName = aClass.packageName(),
                jvmName = aClass.jvmName,
                fields = aClass.toFieldDefList(acc, oldGeneration),
                sealedSubclasses = sealedSubclasses,
                meta = aClass.findAnnotationOptional(ProtoFile::class)?.toMeta() ?: ClassDef.Meta(defaultProtoFile)
            )
        }

        // Append generated class to the accumulator.
        acc[aClass.canonicalName()] = res

        return acc
    }

    private fun KClass<*>.toFieldDefList(
        acc: MutableMap<String, ClassDef>,
        oldGeneration: ProtoDef?
    ): List<FieldDef.Generic> {
        fun type(
            nullable: Boolean, type: KClass<*>, typeArguments: List<KTypeProjection> = emptyList()
        ): TypeDef {
            fun TypeDef.isEnum() = acc.values.any { it.isEnum && it.name == name }
            fun TypeDef.isMap(): Boolean = mapTypes.any { name.startsWith(it) }
            fun TypeDef.isRepeated(): Boolean = repeatedTypes.any { name.startsWith(it) }
            fun List<KTypeProjection>.containsRepeated(): Boolean = repeatedTypes.any {
                map { a -> a.type().jvmErasure.name() }.contains(it)
            }

            val types = if (useWrapperTypes) typesWithWrappers else types
            val typeName: String = type.name()

            var t: TypeDef = (if (nullable) types["$typeName?"] else types[typeName]) ?: TypeDef(typeName)
            if (t.isEnum()) t = t.copy(name = "${t.name}.Enum")

            return if (t.isRepeated()) {
                // Case List/Set.
                if (typeArguments.size != 1) error("Expected 1 type argument, found: ${typeArguments.size}")
                if (typeArguments.containsRepeated()) error("Repeated types is not supported as type-argument in list.")
                val typeArgument = typeArguments.first().let {
                    convert(acc, it.kClass(), oldGeneration)
                    type(false, it.kClass())
                }
                val name = "repeated ${typeArgument.name}"
                TypeDef(name, repeated = true, import = typeArgument.import)
            } else if (t.isMap()) {
                // Case Map.
                if (typeArguments.size != 2) error("Expected 2 type argument, found: ${typeArguments.size}")
                if (typeArguments.containsRepeated()) error("Repeated types is not supported as type-argument in map.")
                typeArguments.forEach { convert(acc, it.kClass(), oldGeneration) }
                val name = "map<${typeArguments.joinToString { type(false, it.kClass(), it.arguments()).name }}>"
                val import = (typeArguments.last().type()).let { type(false, it.jvmErasure, it.arguments) }
                TypeDef(name, map = true, import = import.import)
            } else t
        }

        fun index(i: Int, fields: List<FieldDef.Generic>, p: KProperty1<*, *>): Int {
            val index = when (fieldIndexAssigner) {
                FieldIndexAssigner.SERIAL -> i
                FieldIndexAssigner.ANNOTATION -> {
                    if (hasAnnotation(ProtoOrder.Default::class)) i
                    else p.findAnnotation(ProtoOrder::class).value
                }

                FieldIndexAssigner.OLD_GENERATION -> {
                    oldGeneration!!.classOf(name())?.let {
                        oldGeneration.fieldOf(name(), p.name)?.index ?: run {
                            val max = (it.fields.maxBy { f -> f.index }.index + 1)
                            if (fields.none { it.index == max }) max
                            else fields.maxBy { it.index }.index + 1
                        }
                    } ?: i
                }
            }
            if (index <= 0) error("${name()} :: ${p.name} index < 0")
            return index
        }

        fun import(type: TypeDef, p: KProperty1<*, *>): String =
            type.import
                ?: p.kClass().findAnnotationOptional(ProtoFile::class)?.name
                ?: defaultProtoFile

        val fields = mutableListOf<FieldDef.Generic>()
        declaredMemberProperties.forEachIndexed { i, p ->
            @Suppress("NAME_SHADOWING")
            val i = i + 1

            val index = index(i, fields, p)

            if (p.hasAnnotation(ProtoSkip::class)) {
                fields.add(FieldDef.Generic.skipped(p.name, index, defaultProtoFile))
                return@forEachIndexed
            }

            // Convert the field type.
            convert(acc, p.kClass(), oldGeneration)

            val nullable: Boolean = p.isNullable()
            val type: TypeDef = type(nullable, p.kClass(), p.typeArguments())

            val field = FieldDef.Generic(
                name = p.name,
                index = index,
                import = import(type, p),
                type = type,
                originalType = p.returnType.jvmErasure.java.canonicalName,
                nullable = nullable,
                skip = false
            )

            fields.add(field)
        }

        if (fieldIndexAssigner == FieldIndexAssigner.OLD_GENERATION) {
            oldGeneration?.classOf(name())?.let { oc ->
                oc.fields
                    .filter { f -> fields.none { it.name == f.name } }
                    .filterIsInstance<FieldDef.Generic>()
                    .forEach { f ->
                        if (!f.skip) {
                            // Find missing fields (from the old generation) and add them to the result.
                            val clazz: KClass<*> = Class.forName(f.originalType).kotlin
                            convert(acc, clazz, oldGeneration)
                        }
                        val nullable = !f.type.repeated && !f.type.map
                        fields.add(f.copy(nullable = nullable))
                    }
            }
        }

        // Check for duplicate indexes.
        fields.ensureNoDuplicateIndex(this)

        // Sort by index.
        return fields.sortedBy { it.index }
    }

    companion object {
        fun generate(
            useWrapperTypes: Boolean = false,
            defaultProtoFile: String = "default.proto",
            workDirectory: String = "./generated",
            oldGenerationSchemaFile: String = "schema.json",
            aClass: KClass<*>,
            protoPackage: String,
            topicPrefix: String? = null
        ) = generate(
            useWrapperTypes = useWrapperTypes,
            defaultProtoFile = defaultProtoFile,
            workDirectory = workDirectory,
            oldGenerationSchemaFile = oldGenerationSchemaFile,
            classes = listOf(aClass),
            protoPackage = protoPackage,
            topicPrefix = topicPrefix
        )

        fun generate(
            useWrapperTypes: Boolean = false,
            defaultProtoFile: String = "default.proto",
            workDirectory: String = "./generated",
            oldGenerationSchemaFile: String = "schema.json",
            classes: List<KClass<*>>,
            protoPackage: String,
            topicPrefix: String? = null
        ) {
            File(workDirectory).mkdirs()
            println("KotlinClassToProtobuf :: Working directory $workDirectory")

            val converter = KotlinClassToProtobuf(
                useWrapperTypes = useWrapperTypes,
                defaultProtoFile = defaultProtoFile,
                fieldIndexAssigner = FieldIndexAssigner.OLD_GENERATION,
                protoPackage = protoPackage
            )

            val schema = "$workDirectory/$oldGenerationSchemaFile"
            println("KotlinClassToProtobuf :: Generated schema will be saved in $schema")
            val proto = converter.convert(classes, File(schema))

            println("KotlinClassToProtobuf :: Proto files will be saved in $workDirectory")
            File(workDirectory).listFiles()?.filter { it.name.endsWith(".proto") }?.forEach { it.delete() }
            FileWriter(workDirectory, topicPrefix).write(proto)
        }
    }
}
