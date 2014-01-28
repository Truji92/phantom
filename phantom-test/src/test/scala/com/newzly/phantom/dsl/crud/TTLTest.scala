package com.newzly.phantom.dsl.crud

import org.scalatest.{ Assertions, Matchers, Inside }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._

import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ Primitive, Primitives }
import com.twitter.util.Duration

class TTLTest extends BaseTest with Matchers with Assertions with AsyncAssertions with Inside {
  val keySpace: String = "TTLTest"

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  it should "expire inserterd records" in {
    val row = Primitive.sample
    Primitives.insertSchema(session)
    val test = Primitives.insert
        .value(_.pkey, row.pkey)
        .value(_.long, row.long)
        .value(_.boolean, row.boolean)
        .value(_.bDecimal, row.bDecimal)
        .value(_.double, row.double)
        .value(_.float, row.float)
        .value(_.inet, row.inet)
        .value(_.int, row.int)
        .value(_.date, row.date)
        .value(_.uuid, row.uuid)
        .value(_.bi, row.bi)
        .ttl(Duration.fromSeconds(5))
        .execute() flatMap {
          _ =>  Primitives.select.one
        }

    test.successful {
      record => {
        record.isEmpty shouldEqual false
        record.get should be (row)
        Thread.sleep(Duration.fromSeconds(5).inMillis)
        val test2 = Primitives.select.one
        test2 successful {
          expired => {
            assert(expired.isEmpty)
          }
        }
      }
    }
  }
}