package io.github.smyrgeorge.datasuite.converter.protobuf.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtoFile(
    val name: String,
    val table: String = "",
    val schema: String = "",
    val polymorphism: Array<String> = []
)
