package customWarts

import org.wartremover.{ WartTraverser, WartUniverse }
import scala.quoted.Expr

object CharEqualsAny extends WartTraverser {
  def apply(u: WartUniverse): u.Traverser = {
    new u.Traverser(this) {
      import q.reflect.*

      object PrimitiveEqualsChar {
        def unapply[A](t: Expr[A]): Boolean = t match {
          case '{ ($x1: Byte) == ($x2: Char) } => true
          case '{ ($x1: Short) == ($x2: Char) } => true
          case '{ ($x1: Int) == ($x2: Char) } => true
          case '{ ($x1: Long) == ($x2: Char) } => true
          case '{ ($x1: Float) == ($x2: Char) } => true
          case '{ ($x1: Double) == ($x2: Char) } => true
          case _ => false
        }
      }

      override def traverseTree(tree: Tree)(owner: Symbol): Unit = {
        val error_message = "Implicit char conversion"
        tree match {
          case t if hasWartAnnotation(tree) =>
          case Apply(Select(lhs, "=="), List(rhs))
          if lhs.tpe <:< TypeRepr.of[Char] && !(rhs.tpe <:< TypeRepr.of[Char]) =>
            error(tree.pos, error_message)
          case t if t.isExpr =>
            t.asExpr match {
              case PrimitiveEqualsChar() =>
                error(tree.pos, error_message)
              case _ =>
                super.traverseTree(tree)(owner)
            }
          case _ =>
            super.traverseTree(tree)(owner)
        }
      }
    }
  }
}
