package me.dwnld.slick.codegen.test

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.jdbc.JdbcBackend.Database
import me.dwnld.slick.codegen.WideTableGenerator

object GenDriver extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val modelAction = TestDB.profile.createModel()

  val db = TestDB.db
  try {
    Await.result(db.run(modelAction).map { model =>
      new WideTableGenerator(model).writeToFile(
        "slick.driver.H2Driver",
        args(0),
        "me.dwnld.slick.codegen",
        "Tables",
        "Tables.scala")
    }, Duration.Inf)
  } finally {
    db.shutdown
  }
}
