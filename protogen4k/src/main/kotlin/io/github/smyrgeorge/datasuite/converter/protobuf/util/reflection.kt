package io.github.smyrgeorge.datasuite.converter.protobuf.util

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName

fun KClass<*>.hasAnnotation(clazz: KClass<*>): Boolean =
    annotations.any { a -> a.annotationClass.java == clazz.java }

fun KProperty1<*, *>.hasAnnotation(clazz: KClass<*>): Boolean =
    annotations.any { a -> a.annotationClass.java == clazz.java }

@Suppress("UNCHECKED_CAST")
fun <T : Any> KClass<*>.findAnnotationOptional(clazz: KClass<T>): T? =
    annotations.find { a -> a.annotationClass.java == clazz.java } as? T

@Suppress("UNCHECKED_CAST")
fun <T : Any> KProperty1<*, *>.findAnnotation(clazz: KClass<T>): T =
    (annotations.find { a -> a.annotationClass.java == clazz.java }
        ?: error("Could not find annotation ${clazz.jvmName} in property $name of class ${instanceParameter?.type?.jvmErasure?.jvmName}"))
            as? T ?: error("Could not cast annotation to ${clazz.jvmName}")

@Suppress("UNCHECKED_CAST")
fun <T : Any> KProperty1<*, *>.findAnnotationOptional(clazz: KClass<T>): T? =
    annotations.find { a -> a.annotationClass.java == clazz.java } as? T

@Suppress("UNCHECKED_CAST")
fun <T : Any> Enum<*>.findAnnotation(clazz: KClass<T>): T =
    (javaClass.fields.first { it.name == name }.annotations.find { it.annotationClass.java == clazz.java }
        ?: error("Could not find annotation ${clazz.jvmName} in enum value $this of class ${javaClass.canonicalName}"))
            as? T ?: error("Could not cast annotation to ${clazz.jvmName}")

fun KProperty1<*, *>.kClass(): KClass<*> = returnType.jvmErasure
fun KProperty1<*, *>.isNullable(): Boolean = returnType.isMarkedNullable
fun KTypeProjection.type(): KType = type!!
fun KTypeProjection.kClass(): KClass<*> = type().jvmErasure
fun KTypeProjection.arguments(): List<KTypeProjection> = type().arguments
fun KProperty1<*, *>.typeArguments(): List<KTypeProjection> = returnType.arguments
fun KClass<*>.isEnum(): Boolean = isSubclassOf(Enum::class)
fun KClass<*>.name(): String = jvmName.substringAfterLast('.').replace("$", "")
fun KClass<*>.canonicalName(): String = "${packageName()}.${name()}"
fun KClass<*>.packageName(): String = java.packageName
fun KClass<*>.isPrimitive(): Boolean = java.isPrimitive

@Suppress("UNCHECKED_CAST")
fun KClass<*>.enumConstants(): Array<out Enum<*>> =
    (this as KClass<Enum<*>>).java.enumConstants

fun findProps(
    aClass: KClass<*>,
    path: String = "",
    acc: MutableSet<String> = mutableSetOf(),
    predicate: (p: KProperty1<*, *>) -> Boolean
): Set<String> {
    if (aClass.isEnum()) return acc
    aClass.sealedSubclasses.forEach { findProps(it, path, acc, predicate) }
    aClass.declaredMemberProperties.forEach {
        val repeated = repeatedTypes.any { r -> it.returnType.jvmErasure.name().startsWith(r) }
        val n = if (repeated) "${it.name.toSnakeCase()}[]" else it.name.toSnakeCase()
        val p = if (path.isEmpty()) n else "$path.$n"
        if (predicate(it)) acc.add(p)
        if (repeated) {
            val argument: KTypeProjection = it.typeArguments().first()
            findProps(argument.kClass(), p, acc, predicate)
        }
        findProps(it.kClass(), p, acc, predicate)
    }
    return acc
}
