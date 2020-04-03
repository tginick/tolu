package net.nicholasgwong.tolu.compiler.nodes

import net.nicholasgwong.tolu.compiler.interfaces.CodeGen

class FunctionNode(val name: String, private val args: List<String>, private val block: BlockNode) : Node {

    override fun generate(c: CodeGen) {
        c.implementFunction(name, args)

        block.attachArgs(args)
        block.generate(c)

        c.exitFunction()
    }

}