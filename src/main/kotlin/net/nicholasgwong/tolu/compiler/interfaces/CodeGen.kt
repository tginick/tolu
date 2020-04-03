package net.nicholasgwong.tolu.compiler.interfaces

import net.nicholasgwong.tolu.compiler.opt.Optimizer
import net.nicholasgwong.tolu.rt.Op

interface CodeGen {

    fun implementFunction(name: String, args: List<String>)
    fun exitFunction()
    fun addInstruction(op: Op)
    fun nextIP(): Int
    fun newScope()
    fun exitScope()
    fun currentScope(): Scope
    fun findFunctionIdentifier(s: String): Int?
    fun createFunctionIdentifier(s: String): Int
    fun newString(s: String): Int

    fun optimize(o: Optimizer)
    fun createScaffoldedProgram(c: OutputScaffolding)
}