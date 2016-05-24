package me.dwnld.slick.codegen

import javax.sql.rowset.serial.SerialBlob
import org.scalacheck.{ Arbitrary, Gen }
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import me.dwnld.slick.codegen.test.TestDB
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import me.dwnld.util.GenWideCaseClass
import org.cvogt.scalacheck.GenTree

class WideTableSpec extends Specification with ScalaCheck {
  import TestDB.profile.api._
  private val waitTime = Duration(10, "seconds")
  implicit val arbSqlDate = Arbitrary(
    Arbitrary.arbDate.arbitrary.map { date =>
      new java.sql.Date(date.getTime)
    }
  )

  implicit val arbSqlTs = Arbitrary(
    Arbitrary.arbDate.arbitrary.map { date =>
      new java.sql.Timestamp(date.getTime)
    }
  )

  implicit val arbSqlTime = Arbitrary(
    Arbitrary.arbDate.arbitrary.map { date =>
      new java.sql.Time(date.getTime)
    }
  )

  implicit val arbBlob: Arbitrary[java.sql.Blob] = Arbitrary(
    Gen.listOf(implicitly[Arbitrary[Byte]].arbitrary).map { bytes =>
      new SerialBlob(bytes.toArray)
    }
  )

  implicit val wideArb = Arbitrary(GenWideCaseClass[Tables.WideRow])

  "WideTable generated code".title

  "generated table classes" should {
    "? mapping" >> {
      "produce valid case classes on reads" >> prop { (seed: Tables.WideRow) =>
        val joinedQueryAction = Tables.Wide.joinLeft(Tables.Wide).on { (lhs, rhs) =>
          lhs.col10.map(_ === rhs.col0)
        }.result

        val toInsert = seed.copy(col10 = None)

        val dbWork = for {
          insertedId <- (Tables.Wide returning Tables.Wide.map(_.col0)) += toInsert
          _ <- Tables.Wide += seed.copy(col10 = Some(insertedId))
          joinedRecords <- joinedQueryAction
        } yield joinedRecords

        val joinedRecords = TestDB.withDb { db =>
           Await.result(db.run(dbWork.withPinnedSession), waitTime)
        }

        val extracted = joinedRecords.map { joinedTuple =>
          joinedTuple._2.map(_.copy(col0 = toInsert.col0).toString)
        }

        extracted must containTheSameElementsAs(Seq(None, Some(toInsert.toString)))
      }
    }

    "* mapping" >> {
      "should roundtrip through the db" >> prop { (toInsert: Tables.WideRow) =>
        val dbWork = for {
          res <- (Tables.Wide returning Tables.Wide.map(_.col0)) += toInsert
          cnt <- Tables.Wide.result
        } yield (res, cnt.head)


        val (newId, elem) = TestDB.withDb { db =>
          Await.result(db.run(dbWork.withPinnedSession), waitTime)
        }
        // It appears that SQL date and datetime equality methods are not implemented. Yay.
        elem.toString === toInsert.copy(col0 = newId).toString
      }.set(minTestsOk = 5)
    }
    "produce correct DDL" >> pending
    "parse custom query results" >> pending
  }

}
