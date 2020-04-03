package net.nicholasgwong.tolu.compiler.nodes

import net.nicholasgwong.tolu.compiler.interfaces.CodeGen

class ProgramNode(private val fns: List<FunctionNode>) : Node {
    override fun generate(c: CodeGen) {
        // first load all function names into CodeGenV1
        for (fn in fns) {
            c.createFunctionIdentifier(fn.name)
        }

        for (fn in fns) {
            fn.generate(c)
        }
    }
}