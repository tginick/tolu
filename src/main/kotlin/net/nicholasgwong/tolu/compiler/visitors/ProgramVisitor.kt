package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.gen.ToluParserBaseVisitor
import net.nicholasgwong.tolu.compiler.nodes.FunctionNode
import net.nicholasgwong.tolu.compiler.nodes.ProgramNode

class ProgramVisitor : ToluParserBaseVisitor<ProgramNode>() {

    override fun visitProgram(ctx: ToluParser.ProgramContext): ProgramNode {
        val fns = ArrayList<FunctionNode>()
        for (fnCtx in ctx.function()) {
            val fnVisitor = FunctionVisitor()

            val fnNode = fnVisitor.visitFunction(fnCtx)
            fns.add(fnNode)
        }

        return ProgramNode(fns)
    }

}