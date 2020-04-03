package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.gen.ToluParserBaseVisitor
import net.nicholasgwong.tolu.compiler.nodes.BlockNode
import net.nicholasgwong.tolu.compiler.nodes.StatementNode

class BlockVisitor : ToluParserBaseVisitor<BlockNode>() {

    override fun visitBlock(ctx: ToluParser.BlockContext): BlockNode {
        val stmts: MutableList<StatementNode> = ArrayList()
        for (stmt in ctx.statement()) {
            val stmtVisitor = StatementVisitor()
            val stmtNode = stmtVisitor.visitStatement(stmt)

            stmts.add(stmtNode)
        }

        return BlockNode(stmts)
    }
}