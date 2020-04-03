package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.exceptions.NoSuchLocalException
import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.interfaces.CodeGen
import net.nicholasgwong.tolu.compiler.interfaces.LocalID
import net.nicholasgwong.tolu.compiler.nodes.ExpressionNode
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op

internal data class FunctionCallInfo(val identifier: String, val args: List<ExpressionNode>)

internal fun readFunctionInfo(ctx: ToluParser.FunctionCallContext): FunctionCallInfo {
    val exprVisitor = ExpressionVisitor()
    val identifier = ctx.identifier().text

    val exprs: MutableList<ExpressionNode> = ArrayList()

    if (ctx.expressionList() != null) {
        for (expr in ctx.expressionList().expression()) {
            val exprNode: ExpressionNode = exprVisitor.visit(expr)
            exprs.add(exprNode)
        }
    }

    return FunctionCallInfo(identifier, exprs)
}

internal fun processFunctionCall(c: CodeGen, name: String, exprArgs: List<ExpressionNode>) {
    // generate args
    for (arg in exprArgs) {
        arg.generate(c)
    }

    val fnId = c.findFunctionIdentifier(name)
    if (fnId != null) {
        c.addInstruction(Op.make(Mnemonic.PUSHFNREF, fnId.toLong()))
        c.addInstruction(Op.make(Mnemonic.CALL, exprArgs.size.toLong()))
    } else {
        // check if the identifier refers to a variable containing a function reference
        val varID : LocalID = c.currentScope().findElement(name) ?: throw NoSuchLocalException(name)

        c.addInstruction(Op.make(Mnemonic.GETL, varID.toLong()))
        c.addInstruction(Op.make(Mnemonic.CALL, exprArgs.size.toLong()))
    }
}

internal fun processExtFunctionCall(c: CodeGen, name: String, exprArgs: List<ExpressionNode>) {
    // generate args
    for (arg in exprArgs) {
        arg.generate(c)
    }

    val strId = c.newString(name)

    c.addInstruction(Op.make(Mnemonic.PUSHS, strId.toLong()))
    c.addInstruction(Op.make(Mnemonic.EXT, exprArgs.size.toLong()))

}