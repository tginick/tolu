package net.nicholasgwong.tolu.compiler.visitors

import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.gen.ToluParserBaseVisitor
import net.nicholasgwong.tolu.compiler.nodes.*
import net.nicholasgwong.tolu.compiler.nodes.*

class LiteralVisitor : ToluParserBaseVisitor<LiteralNode>() {
    override fun visitLiteral(ctx: ToluParser.LiteralContext): LiteralNode {
        return when {
            ctx.TFLOATING() != null -> FloatLiteral(ctx.TFLOATING().text.toFloat())
            ctx.TINTEGER() != null -> IntLiteral(ctx.TINTEGER().text.toInt())
            ctx.TSCHARSEQ() != null -> StringLiteral(ctx.TSCHARSEQ().text)
            ctx.TBOOLEAN() != null -> BooleanLiteral(ctx.TBOOLEAN().symbol.text == "true")
            ctx.TAT() != null -> FunctionRefLiteral(ctx.identifier().text)

            else -> throw UnsupportedOperationException("Unexpected literal type")
        }

    }
}