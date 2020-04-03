package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.gen.ToluParserBaseVisitor
import net.nicholasgwong.tolu.compiler.nodes.FunctionNode

class FunctionVisitor : ToluParserBaseVisitor<FunctionNode>() {

    override fun visitFunction(ctx: ToluParser.FunctionContext): FunctionNode {
        val decl = ctx.identifier()

        val argList = ctx.identifierList()
        val argStrList: MutableList<String> = ArrayList()
        if (argList != null) {
            val argListTokens = argList.identifier()
            for (token in argListTokens) {
                argStrList.add(token.text)
            }
        }

        val blockVisitor = BlockVisitor()
        val blockNode = blockVisitor.visitBlock(ctx.block())

        return FunctionNode(decl.text, argStrList, blockNode)
    }
}