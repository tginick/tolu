package net.nicholasgwong.tolu.compiler.nodes

import net.nicholasgwong.tolu.compiler.interfaces.CodeGen

class BlockNode(private val stmts: List<StatementNode>) : Node {

    private var fnArgs: List<String>? = null

    fun attachArgs(args: List<String>) {
        fnArgs = args
    }

    override fun generate(c: CodeGen) {
        c.newScope()


        if (fnArgs != null) {
            val fnArgs = fnArgs!!
            for (arg in fnArgs) {
                c.currentScope().newElement(arg)
            }
        }


        for (stmt in stmts) {
            stmt.generate(c)
        }

        c.exitScope()
    }

}