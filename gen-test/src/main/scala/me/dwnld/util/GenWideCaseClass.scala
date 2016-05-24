package me.dwnld.util
import org.scalacheck.Gen
import scala.language.experimental.macros

object GenWideCaseClass {

  def apply[T]: Gen[T] = macro GenWideCaseClass.genClass[T]
}

class GenWideCaseClass(val c: reflect.macros.blackbox.Context) {
  import c.universe._

  private def getConstructorArgs(classDecl: ClassSymbol): Option[List[Symbol]] = {
    classDecl.typeSignature.decls.collectFirst {
      case paramList: MethodSymbol if paramList.isPrimaryConstructor =>
        paramList.paramLists.flatten
    }
  }

  private val scalacheck = q"_root_.org.scalacheck"
  private val Gen        =  q"$scalacheck.Gen"
  private val GenArities =  q"_root_.org.cvogt.scalacheck.GenArities"

  def genClass[T: c.WeakTypeTag]: Tree = {
    val typeSymbol = weakTypeOf[T].typeSymbol
    val (theClass, constructorArgs) = typeSymbol.asClass match {
      case theClass: ClassSymbol if !theClass.isAbstract => {
        getConstructorArgs(theClass)
          .map((theClass, _))
          .getOrElse(c.abort(
            c.enclosingPosition,
            s"Unable to find constructor for ${theClass}"))
      }
      case _ => c.abort(
        c.enclosingPosition,
        s"Can not create generator for ${typeSymbol}. Cannot determine type"
      )
    }

    if(constructorArgs.isEmpty) {
      q"$Gen.wrap($Gen.const(new ${typeSymbol}))"
    } else {
      val tupleified = tupleify(constructorArgs.map(Leaf(_)))
      val tupleName = TermName(c.freshName("v"))
      val args = tupleified.asArgs(q"${tupleName}")
      q"""${GenArities}.resultOf((${tupleName}: ${tupleified.typeTree}) => new ${weakTypeOf[T]}(..${args}))"""
    }
  }

  def tupleify(args: List[TupleTree]): Tuple = {
    if(args.size <= 22) {
      Tuple(args)
    } else {
      val validTuples = args.grouped(22).map(Tuple(_))
      tupleify(validTuples.toList)
    }
  }


  sealed trait TupleTree {
    def typeTree: Type = this match {
      case Tuple(children) => {
        val childTrees = children.map(_.typeTree)
        appliedType(definitions.TupleClass(children.size), childTrees)
      }
      case Leaf(sym) => sym.typeSignature
    }

  }
  case class Tuple(children: List[TupleTree]) extends TupleTree {
    def asArgs(prefix: Tree): List[Tree] = {
      children.zipWithIndex.flatMap {
        case (child: Tuple, idx) => {
          val newPrefix = Select(prefix, s"_${idx + 1}")
          child.asArgs(newPrefix)
        }
        case (Leaf(_), idx) => List(Select(prefix, s"_${idx + 1}"))
      }
    }
  }
  case class Leaf(theType: Symbol) extends TupleTree

}
