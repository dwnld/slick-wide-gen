package me.dwnld.slick.codegen.test

import slick.jdbc.JdbcBackend.Database


object TestDB {
  def db: Database = Database.forConfig("test")
  def withDb[T](body: (Database) => T): T = {
    val theDb = db
    try {
      body(theDb)
    } finally {
      theDb.shutdown
    }
  }
  val profile = slick.driver.H2Driver
}
