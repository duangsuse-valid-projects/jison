package org.jison

interface JsonVisitor<out R> {
  fun see(dict: Json.Dict): R
  fun see(ary: Json.Ary): R
  fun see(str: Json.Str): R
  fun see(num: Json.Num): R
  fun see(boo: Json.Bool): R
  fun see(nil: Json.Nil): R
}

fun <R> Json.visitedBy(vis: JsonVisitor<R>): R = when (this) {
  is Json.Dict -> vis.see(this)
  is Json.Ary -> vis.see(this)
  is Json.Str -> vis.see(this)
  is Json.Num -> vis.see(this)
  is Json.Bool -> vis.see(this)
  is Json.Nil -> vis.see(this)
}
