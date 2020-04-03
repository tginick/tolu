package net.nicholasgwong.tolu.compiler.nodes

import net.nicholasgwong.tolu.compiler.interfaces.CodeGen

interface Node {
    fun generate(c: CodeGen)
}