package io.github.smyrgeorge.datasuite.converter.protobuf.types

data class TypeDef(
    val name: String,
    val import: String? = null,
    val repeated: Boolean = false,
    val map: Boolean = false
)
