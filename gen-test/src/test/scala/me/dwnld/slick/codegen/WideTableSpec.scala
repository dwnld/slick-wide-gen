package me.dwnld.slick.codegen

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import me.dwnld.slick.codegen.test.TestDB
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import org.cvogt.scalacheck.GenTree

class WideTableSpec extends Specification with ScalaCheck {
  import TestDB.profile.api._
  "WideTable generated code".title

  "generated table classes" should {
    "? mapping" >> {
      "produce valid case classes on reads" >> pending
      "throw exceptions on writes" >> pending
    }
    "* mapping" >> {
      "produce valid case classes on reads" >> pending
      "accept writes using case classes" >> prop(GenTree.partialTree[Tables.WideRow]) { (toInsert: Tables.WideRow) =>
        val dbWork = for {
          _ <- Tables.Wide += toInsert
        } yield Tables.Wide.size

        val count =
          Await.result(TestDB.db.run(dbWork.withPinnedSession), Duration.Inf)

        count === 1
      }
    }
    "produce correct DDL" >> pending
    "parse custom query results" >> pending
  }

}
