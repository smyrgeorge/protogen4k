package io.github.smyrgeorge.datasuite.converter.protobuf

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoFile
import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoSkip

class Main

@ProtoFile(
    name = "test1.proto",
    schema = "schema1",
    polymorphism = ["sealed"]
)
data class Test1(
    @ProtoSkip
    val a: String,
    val test: TestEnum,
    val b: List<String>,
    @ProtoSkip
    val bb: Map<String, List<String>>,
    val kind: Kind,
    val sealed: TestSealed
) {
    enum class Kind {
        A, B, C, D, E
    }
}

@ProtoFile(
    name = "test2.proto",
    schema = "schema2",
    polymorphism = ["sealed", "testCamel"]
)
data class Test2(
    val a: String,
    val sealed: TestSealed,
    @ProtoSkip
    val testCamel: List<TestSealed>
)

enum class TestEnum {
    V1,
    V2,
    V3
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "kind"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TestSealed.S1::class, name = "S1"),
    JsonSubTypes.Type(value = TestSealed.S2::class, name = "S2")
)
sealed interface TestSealed {
    val kind: String

    data class S1(
        override val kind: String
    ) : TestSealed

    data class S2(
        override val kind: String,
        @ProtoSkip val test: String,
        val b: String
    ) : TestSealed
}

fun main(args: Array<String>) {
    KotlinClassToProtobuf.generate(
        topicPrefix = "sample",
        workDirectory = "protogen4k/src/main/proto",
        classes = listOf(Test1::class, Test2::class),
        protoPackage = "io.github.smyrgeorge.datasuite.proto"
    )
}
