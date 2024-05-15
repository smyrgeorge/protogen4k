package io.github.smyrgeorge.datasuite.converter.protobuf.types

import java.time.Instant

data class ProtoDef(
    val generatedAt: Instant = Instant.now(),
    val files: List<FileDef> = emptyList()
) {
    fun classOf(clazz: String): ClassDef? =
        files.map { it.classes }.flatten().find { it.name == clazz }

    fun fieldOf(clazz: String, field: String): FieldDef? =
        classOf(clazz)?.fields?.find { it.name == field }
}
