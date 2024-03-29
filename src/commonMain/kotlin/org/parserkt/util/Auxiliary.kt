package org.parserkt.util

typealias Cnt = Int
typealias Idx = Int
typealias IdxRange = IntRange
typealias Producer<R> = () -> R
typealias Consumer<T> = (T) -> Unit
typealias Predicate<T> = (T) -> Boolean
typealias FoldLeft<A, B> = (B, A) -> B
typealias FoldRight<A, B> = (A, B) -> B
typealias Operation = Producer<Unit>

fun <E> MutableList<E>.removeAtBegin(): E = this.removeAt(0)
fun <E> MutableList<E>.removeAtEnd(): E = this.removeAt(lastIndex)
fun <E> List<E>.subList(indices: IdxRange): List<E> = subList(indices.first, indices.last.inc())

fun <E> MutableList<E>.justAdd(item: E) { add(item) }

fun impossible(): Nothing = throw IllegalStateException()

data class Box<T>(var item: T)
inline fun <T> Box<T?>.map(crossinline f: (T?) -> T): Box<T> = Box(f(this.item))
fun <T> Box<T?>.ensure(): Box<T> = map { it!! }
fun <T> Box<T?>.get(): T = item!!
inline fun <reified R> Box<*>.force(): R = item as R
