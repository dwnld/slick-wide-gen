package me.dwnld.slick.codegen.test

import slick.jdbc.JdbcBackend.Database


object TestDB {
  def db: Database = Database.forURL(
    "jdbc:h2:mem:test;INIT=runscript from 'classpath:test_schema.sql'",
    driver = "org.h2.Driver"
  )
  val profile = slick.driver.H2Driver
}
