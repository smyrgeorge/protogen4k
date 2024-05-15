package io.github.smyrgeorge.datasuite.converter.protobuf.examples

import io.github.smyrgeorge.datasuite.converter.protobuf.KotlinClassToProtobuf
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoFile

class Main

@ProtoFile(name = "a_class.proto", schema = "schema1")
data class AClass(
    val a: String,
    val b: List<AnEnum>
) {
    enum class AnEnum {
        A, B
    }
}

fun main(args: Array<String>) {
    KotlinClassToProtobuf.generate(
        topicPrefix = "sample",
        workDirectory = "examples/src/main/proto",
        classes = listOf(AClass::class),
        protoPackage = "io.github.smyrgeorge.datasuite.proto"
    )
}
