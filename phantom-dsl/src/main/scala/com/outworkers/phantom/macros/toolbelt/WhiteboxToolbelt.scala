/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.macros.toolbelt

import com.outworkers.phantom.macros.debug

import scala.collection.mutable.{Map => MutableMap}
import scala.reflect.macros.whitebox

private[phantom] object WhiteboxToolbelt {
  def apply(ctx: whitebox.Context): WhiteboxToolbelt = new WhiteboxToolbelt {
    override val c: whitebox.Context = ctx
  }

  final class Cache {
    val underlying: MutableMap[Any, Any] = MutableMap.empty

    def show: String = underlying.mkString("\n")
  }
  final val ddHelperCache: Cache = new Cache()
  final val bindHelperCache: Cache = new Cache()
  final val tableHelperCache: Cache = new Cache()
  final val singeGenericCache: Cache = new Cache()
  final val specialEqsCache: Cache = new Cache()
}

@macrocompat.bundle
private[phantom] trait WhiteboxToolbelt {

  val c: whitebox.Context

  import c.universe._

  def abort(msg: String): Nothing = c.abort(c.enclosingPosition, msg)

  lazy val showLogs =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowCompileLog], silent = true).isEmpty

  lazy val showAborts =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowAborts], silent = true).isEmpty

  lazy val showCache =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowCache], silent = true).isEmpty

  lazy val showTrees =
    !c.inferImplicitValue(typeOf[debug.optionTypes.ShowTrees], silent = true).isEmpty

  def memoize[A, B](cache: WhiteboxToolbelt.Cache)(
    a: A, f: A => B
  ): B = cache.underlying.synchronized {
    cache.underlying.get(a) match {
      case Some(b: B @unchecked) =>
        if (showCache) {
          c.echo(c.enclosingPosition, s"ShowCache: $b cached result $b")
        }
        b
      case _ =>
        val b = f(a)
        cache.underlying += (a -> b)
        if (showCache) {
          c.echo(c.enclosingPosition, s"ShowCache: $a computed result $b")
        }

        b
    }
  }


  def info(msg: String, force: Boolean = false): Unit = {
    if (showLogs) {
      c.info(c.enclosingPosition, msg, force)
    }
  }

  def echo(msg: String): Unit = {
    if (showLogs) {
      c.echo(c.enclosingPosition, msg)
    }
  }

  def error(msg: String): Unit = c.error(c.enclosingPosition, msg)


  def warning(msg: String): Unit = c.warning(c.enclosingPosition, msg)

}