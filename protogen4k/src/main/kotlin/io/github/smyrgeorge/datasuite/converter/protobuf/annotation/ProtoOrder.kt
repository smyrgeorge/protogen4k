package io.github.smyrgeorge.datasuite.converter.protobuf.annotation

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProtoOrder(val value: Int) {
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Default
}

