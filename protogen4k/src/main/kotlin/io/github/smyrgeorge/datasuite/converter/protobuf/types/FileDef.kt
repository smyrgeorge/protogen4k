package io.github.smyrgeorge.datasuite.converter.protobuf.types

import io.github.smyrgeorge.datasuite.converter.protobuf.util.toUpperCamelCase

data class FileDef(
    val name: String,
    val imports: List<String>,
    val classes: List<ClassDef>,
    val protoPackage: String
) {
    override fun toString(): String = buildString {
        append("// file: $name\n")
        append("syntax = \"proto3\";")
        if (protoPackage.isNotBlank()) {
            // Ignore the package if it is empty
            append("\n\npackage $protoPackage;")
            append("\n\noption java_package = \"$protoPackage\";")
        }

        val javaOuterClassname = name
            .removeSuffix(".proto")
            .replace('-', '_')
            .toUpperCamelCase()
            .plus("Proto")

        append("\noption java_outer_classname = \"${javaOuterClassname}\";")
        if (imports.isNotEmpty()) append("\n")
        imports.forEach {
            append("\n")
            append("import \"$it\";")
        }
        classes.forEach {
            append("\n\n")
            append(it)
        }
    }
}
