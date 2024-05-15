package io.github.smyrgeorge.datasuite.converter.protobuf.types

import io.github.smyrgeorge.datasuite.converter.protobuf.util.toSnakeCase

data class ClassDef(
    val name: String,
    val packageName: String,
    val jvmName: String,
    val isEnum: Boolean = false,
    val fields: List<FieldDef>,
    val sealedSubclasses: List<ClassDef> = emptyList(),
    val meta: Meta,
) {

    private fun fields(): String =
        buildString {
            val reserved = fields.filter { it.skip }.map { it.index }
            if (reserved.isNotEmpty()) {
                append("  reserved ")
                append(reserved.joinToString())
                append(";\n")
            }
            append("  ")
            append(fields.filter { !it.skip }.joinToString("\n  "))
        }

    private fun enumToString(): String =
        buildString {
            append("message ")
            append(name)
            append(" {\n  enum Enum {\n")
            append(fields())
            append("\n  }\n}")
        }

    private fun sealedClassToString(): String =
        buildString {
            append("message $name {\n")
            append("  oneof ${name.toSnakeCase()} {\n")
            sealedSubclasses.forEachIndexed { i, it ->
                append("    ${it.name} ${it.name.toSnakeCase()} = ${i + 1};\n")
            }
            append("  }\n}")
        }

    private fun genericToString(): String =
        buildString {
            append("message ")
            append(name)
            append(" {\n")
            append(fields())
            append("\n}")
        }

    override fun toString(): String = when {
        isEnum -> enumToString()
        sealedSubclasses.isNotEmpty() -> sealedClassToString()
        else -> genericToString()
    }

    data class Meta(
        val name: String = "",
        val table: String = "",
        val schema: String = "",
        val polymorphism: List<String> = emptyList()
    )
}
