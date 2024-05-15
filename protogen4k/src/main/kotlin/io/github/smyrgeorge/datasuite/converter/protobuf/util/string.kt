package io.github.smyrgeorge.datasuite.converter.protobuf.util

import com.google.common.base.CaseFormat

fun String.toSnakeCase(): String =
    CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this)

fun String.toUpperCamelCase(): String =
    CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this)
