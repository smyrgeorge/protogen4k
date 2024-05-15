package io.github.smyrgeorge.datasuite.converter.protobuf

import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoFile
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoOrder
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoSkip
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ClassDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.FieldDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.FieldIndexAssigner
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ProtoDef
import io.github.smyrgeorge.datasuite.converter.protobuf.util.ensureNoDuplicateIndex
import io.github.smyrgeorge.datasuite.converter.protobuf.util.enumConstants
import io.github.smyrgeorge.datasuite.converter.protobuf.util.findAnnotation
import io.github.smyrgeorge.datasuite.converter.protobuf.util.findAnnotationOptional
import io.github.smyrgeorge.datasuite.converter.protobuf.util.hasAnnotation
import io.github.smyrgeorge.datasuite.converter.protobuf.util.name
import io.github.smyrgeorge.datasuite.converter.protobuf.util.packageName
import io.github.smyrgeorge.datasuite.converter.protobuf.util.toMeta
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class KotlinEnumClassToProtobuf(
    private val defaultProtoFile: String = "default.proto",
    private val fieldIndexAssigner: FieldIndexAssigner = FieldIndexAssigner.ANNOTATION
) {
    private fun KClass<*>.toFieldDefList(oldGeneration: ProtoDef?): List<FieldDef.Enum> {
        val values = mutableListOf<FieldDef.Enum>()
        enumConstants().mapIndexed { i, e ->

            @Suppress("NAME_SHADOWING")
            val i = i + 1

            val index: Int = when (fieldIndexAssigner) {
                FieldIndexAssigner.SERIAL -> i
                FieldIndexAssigner.ANNOTATION -> {
                    if (hasAnnotation(ProtoOrder.Default::class)) i
                    else e.findAnnotation(ProtoOrder::class).value
                }

                FieldIndexAssigner.OLD_GENERATION -> {
                    oldGeneration!!.classOf(name())?.let {
                        oldGeneration.fieldOf(name(), e.name)?.index ?: run {
                            val max = (it.fields.maxBy { it.index }.index + 1)
                            if (values.none { it.index == max }) max
                            else values.maxBy { it.index }.index + 1
                        }
                    } ?: i
                }
            }

            if (index <= 0) error("${name()} :: ${e.name} index < 0")
            val value = FieldDef.Enum(e.name, index, hasAnnotation(ProtoSkip::class))
            values.add(value)
        }

        // Add default value to the head of the list.
        values.add(FieldDef.Enum("PROTO_EMPTY", 0, false))

        if (fieldIndexAssigner == FieldIndexAssigner.OLD_GENERATION) {
            oldGeneration?.classOf(name())?.let { c ->
                c.fields
                    .filter { f -> values.none { it.name == f.name } }
                    .filterIsInstance<FieldDef.Enum>()
                    .forEach { values.add(it) }
            }
        }

        // Check for duplicate indexes.
        values.ensureNoDuplicateIndex(this)

        // Sort by index.
        return values.sortedBy { it.index }
    }

    fun convert(aClass: KClass<*>, oldGeneration: ProtoDef? = null): ClassDef {
        if (fieldIndexAssigner == FieldIndexAssigner.OLD_GENERATION && oldGeneration == null)
            error("Old generation is required for FieldIndexAssigner.OLD_GENERATION.")

        return ClassDef(
            name = aClass.name(),
            packageName = aClass.packageName(),
            jvmName = aClass.jvmName,
            isEnum = true,
            fields = aClass.toFieldDefList(oldGeneration),
            meta = aClass.findAnnotationOptional(ProtoFile::class)?.toMeta() ?: ClassDef.Meta(defaultProtoFile)
        )
    }
}
