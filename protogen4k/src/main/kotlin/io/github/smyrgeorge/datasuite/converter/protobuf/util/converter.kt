package io.github.smyrgeorge.datasuite.converter.protobuf.util

import io.github.smyrgeorge.datasuite.converter.protobuf.annotation.ProtoFile
import io.github.smyrgeorge.datasuite.converter.protobuf.types.ClassDef.Meta
import io.github.smyrgeorge.datasuite.converter.protobuf.types.FieldDef
import io.github.smyrgeorge.datasuite.converter.protobuf.types.TypeDef
import kotlin.reflect.KClass


fun ProtoFile.toMeta(): Meta = Meta(
    name = name,
    table = table,
    schema = schema,
    polymorphism = polymorphism.asList()
)

fun List<FieldDef>.ensureNoDuplicateIndex(aClass: KClass<*>) {
    val duplicates = groupingBy { it.index }.eachCount().filter { it.value > 1 }
    if (duplicates.isNotEmpty()) error("${aClass.name()} :: Duplicate index: $duplicates")
}

val skipPackages: Set<String> = setOf(
    "java."
)

val mapTypes: Set<String> = setOf(
    "java.util.Map",
    "Map"
)

val repeatedTypes: Set<String> = setOf(
    "List",
    "java.util.List",
    "Set",
    "java.util.Set",
)

val types: Map<String, TypeDef> = mapOf(
    "int" to TypeDef("int32"),
    "Int" to TypeDef("int32"),
    "Int?" to TypeDef("int32"),
    "Integer" to TypeDef("int32"),
    "Integer?" to TypeDef("int32"),
    "java.lang.Integer" to TypeDef("int32"),
    "java.lang.Integer?" to TypeDef("int32"),
    "long" to TypeDef("int64"),
    "Long" to TypeDef("int64"),
    "Long?" to TypeDef("int64"),
    "java.lang.Long" to TypeDef("int64"),
    "java.lang.Long?" to TypeDef("int64"),
    "boolean" to TypeDef("bool"),
    "Boolean" to TypeDef("bool"),
    "Boolean?" to TypeDef("bool"),
    "java.lang.Boolean" to TypeDef("bool"),
    "java.lang.Boolean?" to TypeDef("bool"),
//    "float" to TypeDef("float"),
//    "Float" to TypeDef("float"),
//    "Float?" to TypeDef("float"),
//    "java.lang.Float" to TypeDef("float"),
//    "java.lang.Float?" to TypeDef("float"),
    "float" to TypeDef("double"),
    "Float" to TypeDef("double"),
    "Float?" to TypeDef("double"),
    "java.lang.Float" to TypeDef("double"),
    "java.lang.Float?" to TypeDef("double"),
    "double" to TypeDef("double"),
    "Double" to TypeDef("double"),
    "Double?" to TypeDef("double"),
    "java.lang.Double" to TypeDef("double"),
    "java.lang.Double?" to TypeDef("double"),
    "BigDecimal" to TypeDef("double"),
    "BigDecimal?" to TypeDef("double"),
    "java.math.BigDecimal" to TypeDef("double"),
    "java.math.BigDecimal?" to TypeDef("double"),
    "String" to TypeDef("string"),
    "String?" to TypeDef("string"),
    "java.lang.String" to TypeDef("string"),
    "java.lang.String?" to TypeDef("string"),
    "UUID" to TypeDef("string"),
    "UUID?" to TypeDef("string"),
    "java.util.UUID" to TypeDef("string"),
    "java.util.UUID?" to TypeDef("string"),
    "ZoneId" to TypeDef("string"),
    "ZoneId?" to TypeDef("string"),
    "java.time.ZoneId" to TypeDef("string"),
    "java.time.ZoneId?" to TypeDef("string"),
    "Instant" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "Instant?" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "java.time.Instant" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "ZonedDateTime" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "ZonedDateTime?" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "java.time.ZonedDateTime" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "LocalDate" to TypeDef("string"),
    "LocalDate?" to TypeDef("string"),
    "java.time.LocalDate" to TypeDef("string"),
    "java.time.LocalDate?" to TypeDef("string"),
    "LocalTime" to TypeDef("string"),
    "LocalTime?" to TypeDef("string"),
    "java.time.LocalTime" to TypeDef("string"),
    "java.time.LocalTime?" to TypeDef("string"),
    "Any" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "Any?" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "Object" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "Object?" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "java.lang.Object" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "java.lang.Object?" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto")
)

val typesWithWrappers: Map<String, TypeDef> = mapOf(
    "int" to TypeDef("int32"),
    "Int" to TypeDef("int32"),
    "Int?" to TypeDef("google.protobuf.Int32Value", "google/protobuf/wrappers.proto"),
    "Integer" to TypeDef("google.protobuf.Int32Value", "google/protobuf/wrappers.proto"),
    "Integer?" to TypeDef("google.protobuf.Int32Value", "google/protobuf/wrappers.proto"),
    "java.lang.Integer" to TypeDef("google.protobuf.Int32Value", "google/protobuf/wrappers.proto"),
    "java.lang.Integer?" to TypeDef("google.protobuf.Int32Value", "google/protobuf/wrappers.proto"),
    "long" to TypeDef("int64"),
    "Long" to TypeDef("google.protobuf.Int64Value", "google/protobuf/wrappers.proto"),
    "Long?" to TypeDef("google.protobuf.Int64Value", "google/protobuf/wrappers.proto"),
    "java.lang.Long" to TypeDef("google.protobuf.Int64Value", "google/protobuf/wrappers.proto"),
    "java.lang.Long?" to TypeDef("google.protobuf.Int64Value", "google/protobuf/wrappers.proto"),
    "boolean" to TypeDef("bool"),
    "Boolean" to TypeDef("bool"),
    "Boolean?" to TypeDef("google.protobuf.BooleanValue", "google/protobuf/wrappers.proto"),
    "java.lang.Boolean" to TypeDef("google.protobuf.BooleanValue", "google/protobuf/wrappers.proto"),
    "java.lang.Boolean?" to TypeDef("google.protobuf.BooleanValue", "google/protobuf/wrappers.proto"),
//    "float" to TypeDef("float"),
//    "Float" to TypeDef("float"),
//    "Float?" to TypeDef("google.protobuf.FloatValue", "google/protobuf/wrappers.proto"),
//    "java.lang.Float" to TypeDef("google.protobuf.FloatValue", "google/protobuf/wrappers.proto"),
//    "java.lang.Float?" to TypeDef("google.protobuf.FloatValue", "google/protobuf/wrappers.proto"),
    "float" to TypeDef("double"),
    "Float" to TypeDef("double"),
    "Float?" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "java.lang.Float" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "java.lang.Float?" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "double" to TypeDef("double"),
    "Double" to TypeDef("double"),
    "Double?" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "java.lang.Double" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "java.lang.Double?" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "BigDecimal" to TypeDef("double"),
    "BigDecimal?" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "java.math.BigDecimal" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "java.math.BigDecimal?" to TypeDef("google.protobuf.DoubleValue", "google/protobuf/wrappers.proto"),
    "String" to TypeDef("string"),
    "String?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "java.lang.String" to TypeDef("string"),
    "java.lang.String?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "UUID" to TypeDef("string"),
    "UUID?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "java.util.UUID" to TypeDef("string"),
    "java.util.UUID?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "ZoneId" to TypeDef("string"),
    "ZoneId?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "java.time.ZoneId" to TypeDef("string"),
    "java.time.ZoneId?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "Instant" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "Instant?" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "java.time.Instant" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "ZonedDateTime" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "ZonedDateTime?" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "java.time.ZonedDateTime" to TypeDef("google.protobuf.Timestamp", "google/protobuf/timestamp.proto"),
    "LocalDate" to TypeDef("string"),
    "LocalDate?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "java.time.LocalDate" to TypeDef("string"),
    "java.time.LocalDate?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "LocalTime" to TypeDef("string"),
    "LocalTime?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "java.time.LocalTime" to TypeDef("string"),
    "java.time.LocalTime?" to TypeDef("google.protobuf.StringValue", "google/protobuf/wrappers.proto"),
    "Any" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "Any?" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "Object" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "Object?" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "java.lang.Object" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto"),
    "java.lang.Object?" to TypeDef("google.protobuf.Any", "google/protobuf/any.proto")
)
