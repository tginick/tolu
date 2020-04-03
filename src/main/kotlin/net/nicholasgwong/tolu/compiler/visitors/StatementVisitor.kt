package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.gen.ToluParserBaseVisitor
import net.nicholasgwong.tolu.compiler.nodes.*
import net.nicholasgwong.tolu.compiler.nodes.*

class StatementVisitor : ToluParserBaseVisitor<StatementNode>() {
    override fun visitStatement(ctx: ToluParser.StatementContext): StatementNode {

        return when {
            ctx.conditional() != null -> {
                processConditional(ctx.conditional())
            }
            ctx.variableAssign() != null -> {
                processVariableAssign(ctx.variableAssign())
            }
            ctx.whileLoop() != null -> {
                processWhileLoop(ctx.whileLoop())
            }
            ctx.functionCall() != null -> {
                processFunctionCall(ctx.functionCall())
            }
            ctx.extFunctionCall() != null -> {
                processExtFunctionCall(ctx.extFunctionCall())
            }
            ctx.returnStatement() != null -> {
                processReturnStatement(ctx.returnStatement())
            }

            else -> {
                throw UnsupportedOperationException("Unimplemented statement type")
            }
        }
    }

    private fun processConditional(ctx: ToluParser.ConditionalContext): ConditionalStatementNode {
        val branches = ArrayList<Branch>()
        val exprVisitor = ExpressionVisitor()
        val blockVisitor = BlockVisitor()

        val ifBranch = ctx.ifBranch().exprAndBlock()
        branches.add(
            Branch(
                exprVisitor.visit(ifBranch.expression()),
                blockVisitor.visitBlock(ifBranch.block())
        )
        )

        for (elsifBranch in ctx.elsifBranch()) {
            branches.add(
                Branch(
                    exprVisitor.visit(elsifBranch.exprAndBlock().expression()),
                    blockVisitor.visitBlock(elsifBranch.exprAndBlock().block())
            )
            )
        }

        val elseBranch = ctx.elseBranch().block()
        branches.add(
            Branch(
                ElseDummyExpressionNode(),
                blockVisitor.visitBlock(elseBranch)
        )
        )


        return ConditionalStatementNode(branches)
    }

    private fun processVariableAssign(ctx: ToluParser.VariableAssignContext): AssignmentStatementNode {
        val identifier = ctx.identifier().text
        val exprVisitor = ExpressionVisitor()

        val exprNode = exprVisitor.visit(ctx.expression())

        return AssignmentStatementNode(identifier, exprNode)
    }

    private fun processWhileLoop(ctx: ToluParser.WhileLoopContext): WhileLoopStatementNode {
        val exprVisitor = ExpressionVisitor()
        val blockVisitor = BlockVisitor()

        val exprAndBlock = ctx.exprAndBlock()

        val exprNode = exprVisitor.visit(exprAndBlock.expression())
        val blockNode = blockVisitor.visitBlock(exprAndBlock.block())

        return WhileLoopStatementNode(exprNode, blockNode)
    }

    private fun processFunctionCall(ctx: ToluParser.FunctionCallContext): FunctionCallStatementNode {
        val info = readFunctionInfo(ctx)

        return FunctionCallStatementNode(info.identifier, info.args)
    }

    private fun processExtFunctionCall(ctx: ToluParser.ExtFunctionCallContext): ExtFunctionCallStatementNode {
        val info = readFunctionInfo(ctx.functionCall())

        return ExtFunctionCallStatementNode(info.identifier, info.args)
    }

    private fun processReturnStatement(ctx: ToluParser.ReturnStatementContext): ReturnStatementNode {
        val exprVisitor = ExpressionVisitor()

        return ReturnStatementNode(if (ctx.expression() != null) { exprVisitor.visit(ctx.expression()) } else { null })
    }
}