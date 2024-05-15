package io.github.smyrgeorge.datasuite.converter.protobuf.util

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun jackson(): ObjectMapper = jacksonObjectMapper().apply {
    registerModule(JavaTimeModule())
    disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
    disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
    enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)  // Timestamps as milliseconds
    setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
    setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
    disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}
