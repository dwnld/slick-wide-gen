package me.dwnld.slick.codegen

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.codegen.SourceCodeGenerator
import slick.jdbc.JdbcBackend.Database
import slick.{ model => m }

class WideTableGenerator(model: m.Model) extends SourceCodeGenerator(model) {

  override def Table = { table: m.Table =>
    if(table.columns.size > 22)
      new WideTableDef(table)
    else
      super.Table(table)
  }

  class WideTableDef(table: m.Table) extends super.TableDef(table) {
    override def hlistEnabled = false

    override def extractor =
      throw new RuntimeException("No extractor is defined for a table with more than 22 columns")

    def compoundRepName = s"${tableName(table.name.table)}Rep"
    def compoundLiftedValue(values: Seq[String]): String = {
        s"""${compoundRepName}(${values.mkString(",")})"""
    }

    override def TableClass = new WideTableClassDef
    class WideTableClassDef extends TableClassDef {
      def repConstructor = compoundLiftedValue(columns.map(_.name))
      override def star = {
        s"def * = ${repConstructor}"
      }
      override def option = {
        s"def ? = Rep.Some(${repConstructor})"
      }
    }

    override def definitions = Seq[Def](
      EntityType, CompoundRep, RowShape, PlainSqlMapper, TableClass, TableValue
    )

    override def compoundValue(values: Seq[String]) =
      s"""${entityName(table.name.table)}(${values.mkString(", ")})"""

    override def factory = ""

    def CompoundRep = new CompoundRepDef
    class CompoundRepDef extends TypeDef {
      override def code: String = {
        val args = columns.map(c=>
          c.default.map( v =>
            s"${c.name}: Rep[${c.exposedType}] = $v"
          ).getOrElse(
            s"${c.name}: Rep[${c.exposedType}]"
          )
        ).mkString(", ")

        val prns = (parents.take(1).map(" extends "+_) ++
          parents.drop(1).map(" with "+_)).mkString("")

        s"""case class $name($args)$prns"""
      }
      override def doc: String = "" // TODO
      override def rawName: String = compoundRepName
    }

    def RowShape = new RowShapeDef
    class RowShapeDef extends TypeDef {
      override def code: String = {
        val dependencies = (columns.map { column =>
          val repType = s"Rep[${column.exposedType}]"
          s"implicitly[Shape[FlatShapeLevel, ${repType}, ${column.exposedType}, ${repType}]]"
        }).mkString(", ")
        val dependencySeq = s"Seq(${dependencies})"
        def seqConversionCode(columnTypeMapper: String => String) =
          columns.zipWithIndex.map { case (column, index)=>
            s"seq(${index}).asInstanceOf[${columnTypeMapper(column.exposedType)}]"
          }

        val seqParseFunctionBody = seqConversionCode(identity)
        val liftedSeqParseBody = seqConversionCode { tpe => s"Rep[${tpe}]" }
        val seqParseFunction = s"seq => ${compoundValue(seqParseFunctionBody)}"
        val liftedSeqParseFunc = s"seq => ${compoundLiftedValue(liftedSeqParseBody)}"

        s"""implicit object ${name} extends ProductClassShape(${dependencySeq}, ${liftedSeqParseFunc}, ${seqParseFunction})"""

      }
      override def doc: String = "" // TODO
      override def rawName: String = s"${tableName(table.name.table)}Shape"
    }
  }
}

object WideTableGenerator extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val testDb = Database.forURL(
    "jdbc:h2:mem:test;INIT=runscript from 'classpath:test_schema.sql'",
    driver = "org.h2.Driver"
  )

  val profile = slick.driver.H2Driver
  val modelAction = profile.createModel(Some(profile.defaultTables))

  Await.result(testDb.run(modelAction).map { model =>
    new WideTableGenerator(model).writeToFile(
      "slick.driver.H2Driver",
      args(0),
      "me.dwnld.slick.codegen",
      "Tables",
      "Tables.scala")
  }, Duration.Inf)

}
