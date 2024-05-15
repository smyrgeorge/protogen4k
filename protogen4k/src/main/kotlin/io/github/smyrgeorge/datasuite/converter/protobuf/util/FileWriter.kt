package io.github.smyrgeorge.datasuite.converter.protobuf.util

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoSkip
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ClassDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ProtoDef
import java.io.File
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.jvmErasure

class FileWriter(
    private val path: String,
    private val topicPrefix: String? = null
) {
    fun write(proto: ProtoDef) {
        val path = path.removeSuffix("/")
        File(path).mkdirs()

        // 1. Write proto files.
        writeProtoFiles(path, proto)

        // 2. Write connector yml file.
        writeConnectorFile(path, proto)
    }

    private fun writeProtoFiles(path: String, proto: ProtoDef) {
        proto.files.forEach {
            val p = "$path/${it.name}"
            println("FileWriter :: Writing file $p")
            File(p).writeText(it.toString())
        }
    }

    private fun writeConnectorFile(path: String, proto: ProtoDef) {
        val classes: List<ClassDef> = proto.classes()
        val tables: Sequence<ClassDef> = classes.tables()
        val sealed: Map<String, String> = classes.sealed()

        val yml = buildString {
            append("spec:\n")
            append("  config:\n")
            topicPrefix?.let { append("    topic.prefix: $topicPrefix\n") }
            classes.schemas().let { if (it.isNotEmpty()) append("    schema.include.list: |-\n      $it\n") }
            tables.tablesWithSchema().let { if (it.isNotEmpty()) append("    table.include.list: |-\n      $it\n") }
            tables.skipProps().let {
                if (it.isNotEmpty()) append("    transforms.json.ignore.fields: |-\n      $it\n")
            }
            tables.polymorphism(sealed).let {
                if (it.isNotEmpty()) append("    transforms.polymorphic.paths: |-\n      $it\n")
            }
        }

        val ymlPath = "$path/connector.yml"
        println("FileWriter :: Writing file $ymlPath")
        File(ymlPath).writeText(yml)
    }

    private fun ProtoDef.classes() = files.map { it.classes }.flatten()
    private fun List<ClassDef>.tables(): Sequence<ClassDef> = asSequence().filter { it.meta.schema.isNotBlank() }

    private fun List<ClassDef>.sealed() = asSequence()
        .filter { it.sealedSubclasses.isNotEmpty() }
        .map { it to Class.forName(it.jvmName).kotlin }
        .map {
            val jsonTypeInfo = it.second.findAnnotation<JsonTypeInfo>()
                ?: error("Could not find @JsonTypeInfo ${it.first.jvmName}")
            val jsonSubTypes = it.second.findAnnotation<JsonSubTypes>()
                ?: error("Could not find @JsonSubTypes ${it.first.jvmName}")
            it.first.name to buildString {
                append(jsonTypeInfo.property)
                append('{')
                val types = jsonSubTypes.value.joinToString(",") { t ->
                    "${t.name}:${t.value.name().toSnakeCase()}"
                }
                append(types)
                append('}')
            }
        }.toMap()

    private fun List<ClassDef>.schemas(): String = asSequence()
        .filter { it.meta.schema.isNotBlank() }
        .map { it.meta.schema }
        .toSet().sorted().joinToString(",\n      ")

    private fun ClassDef.tableWithSchema(): String = buildString {
        if (meta.schema.isNotBlank()) {
            append(meta.schema)
            append('.')
        }
        if (meta.table.isNotBlank()) append(meta.table)
        else append(meta.name.removeSuffix(".proto"))
    }

    private fun Sequence<ClassDef>.tablesWithSchema(): String =
        map { it.tableWithSchema() }.toSet().sorted().joinToString(",\n      ")

    private fun Sequence<ClassDef>.skipProps(): String = map { t ->
        val c = Class.forName(t.jvmName).kotlin
        findProps(c) { it.hasAnnotation(ProtoSkip::class) }.map {
            buildString {
                topicPrefix?.let { tp ->
                    append(tp)
                    append('.')
                }
                append(t.tableWithSchema())
                append('.')
                append(it)
            }
        }
    }.flatten().toSet().sorted().joinToString(",\n      ")


    private fun Sequence<ClassDef>.polymorphism(sealed: Map<String, String>): String =
        filter { it.meta.polymorphism.isNotEmpty() }
            .map {
                it.meta.polymorphism.map { p ->
                    var c: Class<*> = Class.forName(it.jvmName)
                    var repeated = false
                    p.split('.').forEach { s ->
                        val m = c.kotlin.declaredMemberProperties.first { f -> f.name == s }
                        c = m.returnType.jvmErasure.java
                        if (repeatedTypes.contains(c.simpleName)) {
                            repeated = true
                            val args: List<KTypeProjection> = m.typeArguments()
                            if (args.size != 1) error("Expected 1 type argument, found: ${args.size}")
                            c = args.first().kClass().java
                        }
                    }

                    val def = sealed[c.simpleName] ?: error("Could not find polymorphic class ${c.simpleName}")

                    buildString {
                        topicPrefix?.let { tp ->
                            append(tp)
                            append('.')
                        }
                        append(it.meta.schema)
                        append('.')
                        if (it.meta.table.isNotBlank()) append(it.meta.table)
                        else append(it.meta.name.removeSuffix(".proto"))
                        append('.')
                        append(p.split('.').joinToString(".") { p -> p.toSnakeCase() })
                        if (repeated) append("[]")
                        append('.')
                        append(def)
                    }
                }
            }.flatten().toSet().sorted().joinToString(",\n      ")
}
