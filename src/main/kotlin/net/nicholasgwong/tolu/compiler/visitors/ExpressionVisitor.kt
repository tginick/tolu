package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.gen.ToluParserBaseVisitor
import net.nicholasgwong.tolu.compiler.nodes.*
import net.nicholasgwong.tolu.compiler.nodes.*

class ExpressionVisitor : ToluParserBaseVisitor<ExpressionNode>() {
    override fun visitParenExpr(ctx: ToluParser.ParenExprContext): ExpressionNode {
        // just visit the expression
        return visit(ctx)
    }

    override fun visitLiteralExpr(ctx: ToluParser.LiteralExprContext): ExpressionNode {
        val literalVisitor = LiteralVisitor()

        return literalVisitor.visitLiteral(ctx.literal())
    }

    override fun visitIdentifierExpr(ctx: ToluParser.IdentifierExprContext): ExpressionNode {
        return IdentifierNode(ctx.text)
    }

    override fun visitObjAccessExpr(ctx: ToluParser.ObjAccessExprContext): ExpressionNode {
        val objExpr = visit(ctx.expression(0))
        val accExpr = visit(ctx.expression(1))

        return ObjAccessExprNode(objExpr, accExpr)
    }

    override fun visitFunctionCallExpr(ctx: ToluParser.FunctionCallExprContext): ExpressionNode {
        val info = readFunctionInfo(ctx.functionCall())

        return FunctionCallExprNode(info.identifier, info.args)
    }

    override fun visitExtFunctionCallExpr(ctx: ToluParser.ExtFunctionCallExprContext): ExpressionNode {
        val info = readFunctionInfo(ctx.extFunctionCall().functionCall())

        return ExtFunctionCallExprNode(info.identifier, info.args)
    }

    override fun visitArithAddExpr(ctx: ToluParser.ArithAddExprContext): ExpressionNode {
        val lhsExpr = visit(ctx.expression(0))
        val rhsExpr = visit(ctx.expression(1))

        return ArithExprNode(lhsExpr, rhsExpr, when {
            ctx.TPLUS() != null -> ArithOperator.ADD
            ctx.TMINUS() != null -> ArithOperator.SUB
            else -> throw IllegalStateException("Unexpected operator found for add/sub visit")
        })
    }

    override fun visitArithMulExpr(ctx: ToluParser.ArithMulExprContext): ExpressionNode {
        val lhsExpr = visit(ctx.expression(0))
        val rhsExpr = visit(ctx.expression(1))

        return ArithExprNode(lhsExpr, rhsExpr, when {
            ctx.TSTAR() != null -> ArithOperator.MUL
            ctx.TDIV() != null -> ArithOperator.DIV
            else -> throw IllegalStateException("Unexpected operator found for add/sub visit")
        })
    }

    override fun visitComparisonExpr(ctx: ToluParser.ComparisonExprContext): ExpressionNode {
        val lhsExpr = visit(ctx.expression(0))
        val rhsExpr = visit(ctx.expression(1))
        val op: ComparisonOperator = when {
            ctx.TLTE() != null -> ComparisonOperator.LTE
            ctx.TGTE() != null -> ComparisonOperator.GTE
            ctx.TLT() != null  -> ComparisonOperator.LT
            ctx.TGT() != null  -> ComparisonOperator.GT
            else -> throw IllegalStateException("Unexpected comparison operator found")
        }

        return ComparisonNode(lhsExpr, rhsExpr, op)
    }

    override fun visitEqualityExpr(ctx: ToluParser.EqualityExprContext): ExpressionNode {
        val lhsExpr = visit(ctx.expression(0))
        val rhsExpr = visit(ctx.expression(1))
        val op: ComparisonOperator = when {
            ctx.TEQUALITY() != null -> ComparisonOperator.EQ
            ctx.TNOTEQUAL() != null -> ComparisonOperator.NEQ
            else -> throw IllegalStateException("Unexpected comparison operator found")
        }

        return ComparisonNode(lhsExpr, rhsExpr, op)
    }

    override fun visitLogicalAndExpr(ctx: ToluParser.LogicalAndExprContext): ExpressionNode {
        val lhsExpr = visit(ctx.expression(0))
        val rhsExpr = visit(ctx.expression(1))

        return LogicalExprNode(lhsExpr, rhsExpr, BooleanOperator.AND)
    }

    override fun visitLogicalOrExpr(ctx: ToluParser.LogicalOrExprContext): ExpressionNode {
        val lhsExpr = visit(ctx.expression(0))
        val rhsExpr = visit(ctx.expression(1))

        return LogicalExprNode(lhsExpr, rhsExpr, BooleanOperator.OR)
    }




}