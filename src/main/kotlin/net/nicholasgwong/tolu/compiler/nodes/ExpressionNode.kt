package net.nicholasgwong.tolu.compiler.nodes

import net.nicholasgwong.tolu.compiler.exceptions.NoSuchLocalException
import net.nicholasgwong.tolu.compiler.interfaces.CodeGen
import net.nicholasgwong.tolu.compiler.visitors.processExtFunctionCall
import net.nicholasgwong.tolu.compiler.visitors.processFunctionCall
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op

interface BooleanExpression
abstract class ExpressionNode : Node

class ElseDummyExpressionNode : ExpressionNode(), BooleanExpression {
    override fun generate(c: CodeGen) {
        throw IllegalStateException("ElseDummyExpressionNode.generate should not be called")
    }
}

sealed class LiteralNode : ExpressionNode()

enum class ComparisonOperator {
    LTE,
    GTE,
    LT,
    GT,
    EQ,
    NEQ
}

enum class BooleanOperator {
    AND,
    OR
}

enum class ArithOperator {
    ADD,
    SUB,
    MUL,
    DIV
}


class IdentifierNode(private val name: String): ExpressionNode() {
    override fun generate(c: CodeGen) {
        val elemId = c.currentScope().findElement(name)
        if (elemId != null) {
            c.addInstruction(Op.make(Mnemonic.GETL, elemId.toLong()))
            return
        }

        throw NoSuchLocalException(name)
    }
}

class FloatLiteral(private val value: Float) : LiteralNode() {
    override fun generate(c: CodeGen) {
        c.addInstruction(Op.makeFloat(Mnemonic.PUSHF, value))
    }
}

class IntLiteral(private val value: Int): LiteralNode() {
    override fun generate(c: CodeGen) {
        c.addInstruction(Op.make(Mnemonic.PUSHI, value.toLong()))
    }
}

class BooleanLiteral(private val value: Boolean): LiteralNode() {
    override fun generate(c: CodeGen) {
        c.addInstruction(Op.make(Mnemonic.PUSHB, if (value) { 1 } else { 0 }))
    }
}

class StringLiteral(private val value: String): LiteralNode() {
    override fun generate(c: CodeGen) {
        val strId = c.newString(value)

        c.addInstruction(Op.make(Mnemonic.PUSHS, strId.toLong()))
    }
}

class FunctionRefLiteral(private val name: String): LiteralNode() {
    override fun generate(c: CodeGen) {
        val fnId = c.findFunctionIdentifier(name)

        if (fnId != null) {
            c.addInstruction(Op.make(Mnemonic.PUSHFNREF, fnId.toLong()))
        } else {
            // function name must exist to take a reference to it
            throw NoSuchLocalException(name)
        }
    }
}

class ComparisonNode(private val lhsExpr: ExpressionNode, private val rhsExpr: ExpressionNode, private val op: ComparisonOperator) : ExpressionNode(),
    BooleanExpression {
    override fun generate(c: CodeGen) {
        lhsExpr.generate(c)
        rhsExpr.generate(c)

        val mnemonic = when (op) {
            ComparisonOperator.GT -> Mnemonic.GT
            ComparisonOperator.GTE -> Mnemonic.GTE
            ComparisonOperator.LT -> Mnemonic.LT
            ComparisonOperator.LTE -> Mnemonic.LTE
            ComparisonOperator.EQ -> Mnemonic.EQ
            else -> throw UnsupportedOperationException("Cannot generate comparison mnemonic for $op")
        }

        c.addInstruction(Op.make(mnemonic, 0))
    }

}

class LogicalExprNode(private val lhsExpr: ExpressionNode, private val rhsExpr: ExpressionNode, private val op: BooleanOperator) : ExpressionNode(),
    BooleanExpression {
    override fun generate(c: CodeGen) {
        lhsExpr.generate(c)
        rhsExpr.generate(c)

        val mnemonic = when (op) {
            BooleanOperator.AND -> Mnemonic.AND
            BooleanOperator.OR -> Mnemonic.OR
        }

        c.addInstruction(Op.make(mnemonic, 0))
    }

}

class ArithExprNode(private val lhsExpr: ExpressionNode, private val rhsExpr: ExpressionNode, private val op: ArithOperator) : ExpressionNode() {
    override fun generate(c: CodeGen) {
        lhsExpr.generate(c)
        rhsExpr.generate(c)

        val mnemonic = when (op) {
            ArithOperator.SUB -> Mnemonic.SUB
            ArithOperator.ADD -> Mnemonic.ADD
            ArithOperator.DIV -> Mnemonic.DIV
            ArithOperator.MUL -> Mnemonic.MUL
        }

        c.addInstruction(Op.make(mnemonic, 0))
    }

}

class FunctionCallExprNode(val identifier: String, private val exprArgs: List<ExpressionNode>) : ExpressionNode() {
    override fun generate(c: CodeGen) {
        processFunctionCall(c, identifier, exprArgs)
    }
}

class ExtFunctionCallExprNode(val identifier: String, private val exprArgs: List<ExpressionNode>): ExpressionNode() {
    override fun generate(c: CodeGen) {
        processExtFunctionCall(c, identifier, exprArgs)
    }
}

class ObjAccessExprNode(val objExpr: ExpressionNode, val accExpr: ExpressionNode): ExpressionNode() {
    override fun generate(c: CodeGen) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

