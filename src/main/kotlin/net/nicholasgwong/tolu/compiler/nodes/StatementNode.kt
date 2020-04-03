package net.nicholasgwong.tolu.compiler.nodes

import net.nicholasgwong.tolu.compiler.exceptions.InvalidConditionalExpression
import net.nicholasgwong.tolu.compiler.interfaces.CodeGen
import net.nicholasgwong.tolu.compiler.interfaces.LocalID
import net.nicholasgwong.tolu.compiler.nodes.*
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op

abstract class StatementNode : Node

class WhileLoopStatementNode(private val booleanExpr: ExpressionNode, private val block: BlockNode) : StatementNode() {
    override fun generate(c: CodeGen) {
        val condStartIP = c.nextIP()
        c.newScope()

        booleanExpr.generate(c)

        // get the next instruction pointer for the block. this is where the JNE instruction will go
        val jumpToEndOp = Op.make(Mnemonic.JNE, 0)

        c.addInstruction(jumpToEndOp)

        // generate body
        block.generate(c)

        val loopContinueIP = c.nextIP()
        val jumpToBeginningOp = Op.make(Mnemonic.J, 0)

        c.addInstruction(jumpToBeginningOp)

        c.exitScope()

        // set jump destinations
        jumpToEndOp.data = (loopContinueIP + 1).toLong()
        jumpToBeginningOp.data = condStartIP.toLong()
    }

}

data class Branch(val booleanExpr: ExpressionNode, val block: BlockNode) {
    init {
        if (booleanExpr !is BooleanExpression) {
            throw InvalidConditionalExpression(booleanExpr.javaClass)
        }
    }
}

class ConditionalStatementNode(private val branches: List<Branch>) : StatementNode() {
    override fun generate(c: CodeGen) {
        val jToEndOps = ArrayList<Op>()
        for (i in 0 until branches.size - 1) {
            branches[i].booleanExpr.generate(c)

            // if check fails, jump to next check (or to the end). not sure where that is yet
            // so first take the location of this check
            val jneOp = Op.make(Mnemonic.JNE, 0)
            c.addInstruction(jneOp)

            // generate the block
            branches[i].block.generate(c)

            // now we need to jump to the end of the conditional. not sure where that is!
            val jToEnd = Op.make(Mnemonic.J, 0)
            jToEndOps.add(jToEnd)
            c.addInstruction(jToEnd)

            jneOp.data = c.nextIP().toLong() // and now update that instruction
        }

        // generate else block
        branches[branches.size - 1].block.generate(c)

        // what's the end?
        val condEndIP = c.nextIP()
        for (jToEnd in jToEndOps) {
            jToEnd.data = condEndIP.toLong()
        }
    }
}


class AssignmentStatementNode(val name: String, private val expr: ExpressionNode) : StatementNode() {
    override fun generate(c: CodeGen) {
        val newVarID: LocalID = c.currentScope().newElement(name)

        expr.generate(c)

        c.addInstruction(Op.make(Mnemonic.SETL, newVarID.toLong()))
    }
}

class FunctionCallStatementNode(val identifier: String, exprArgs: List<ExpressionNode>) : StatementNode() {
    private val fnExprNode = FunctionCallExprNode(identifier, exprArgs)
    override fun generate(c: CodeGen) {
        fnExprNode.generate(c)
    }
}

class ExtFunctionCallStatementNode(val identifier: String, exprArgs: List<ExpressionNode>): StatementNode() {
    private val fnExprNode = ExtFunctionCallExprNode(identifier, exprArgs)
    override fun generate(c: CodeGen) {
        fnExprNode.generate(c)
    }
}

class ReturnStatementNode(private val expr: ExpressionNode?) : StatementNode() {
    override fun generate(c: CodeGen) {
        if (expr != null) {
            expr.generate(c)
        } else {
            c.addInstruction(Op.make(Mnemonic.PUSHUNIT, 0))
        }

        c.addInstruction(Op.make(Mnemonic.RETN, 0))
    }
}