package io.github.smyrgeorge.datasuite.converter.protobuf.types

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smyrgeorge.datasuite.converter.protobuf.util.toSnakeCase

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "kind"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = FieldDef.Enum::class, name = "ENUM"),
    JsonSubTypes.Type(value = FieldDef.Generic::class, name = "GENERIC"),
)
sealed class FieldDef {
    abstract val name: String
    abstract val index: Int
    abstract val skip: Boolean

    data class Enum(
        override val name: String,
        override val index: Int,
        override val skip: Boolean
    ) : FieldDef() {
        override fun toString(): String =
            buildString {
                append("  ")
                append(name)
                append(" = ")
                append(index)
                append(";")
            }
    }

    data class Generic(
        override val name: String,
        override val index: Int,
        override val skip: Boolean,
        val type: TypeDef,
        val originalType: String,
        val import: String,
        val nullable: Boolean
    ) : FieldDef() {
        override fun toString(): String =
            buildString {
                append(if (nullable) "optional " else "")
                append(type.name)
                append(" ")
                append(name.toSnakeCase())
                append(" = ")
                append(index)
                append(";")
            }

        companion object {
            fun skipped(name: String, index: Int, import: String): Generic = Generic(
                name = name,
                index = index,
                skip = true,
                type = TypeDef("Skipped"),
                originalType = "Skipped",
                import = import,
                nullable = true
            )

        }
    }
}
