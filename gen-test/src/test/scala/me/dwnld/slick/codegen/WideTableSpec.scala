package me.dwnld.slick.codegen

import org.specs2.mutable.Specification

class WideTableSpec extends Specification {
  "WideTable generated code".title

  "generated table classes" should {
    "? mapping" >> {
      "produce valid case classes on reads" >> pending
      "throw exceptions on writes" >> pending
    }
    "* mapping" >> {
      "produce valid case classes on reads" >> pending
      "accept writes using case classes" >> pending
    }
    "produce correct DDL" >> pending
    "parse custom query results" >> pending
  }

}
