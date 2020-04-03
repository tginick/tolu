package net.nicholasgwong.tolu.compiler.impl

import net.nicholasgwong.tolu.compiler.CompiledFunction
import net.nicholasgwong.tolu.compiler.interfaces.CodeGen
import net.nicholasgwong.tolu.compiler.interfaces.OutputScaffolding
import net.nicholasgwong.tolu.compiler.interfaces.Scope
import net.nicholasgwong.tolu.compiler.opt.Optimizer
import net.nicholasgwong.tolu.rt.LocalFunctionEntry
import net.nicholasgwong.tolu.rt.Op

class CodeGenV1 : CodeGen {
    private val strTable: MutableMap<String, Int> = HashMap()
    private val fnTable: MutableMap<String, Int> = HashMap()

    private val functions: MutableMap<String, CompiledFunction> = HashMap()

    private val topScope: ScopeV1 = ScopeV1(null)
    private var currentScope: ScopeV1 = topScope

    private var currentFunction: CompiledFunction? = null

    override fun newString(s: String): Int {
        if (strTable.containsKey(s)) {
            return strTable[s]!!
        }

        val nextId = strTable.size
        strTable[s] = nextId

        return nextId
    }

    override fun createFunctionIdentifier(s: String): Int {
        if (fnTable.containsKey(s)) {
            return fnTable[s]!!
        }

        val nextId = fnTable.size
        fnTable[s] = nextId

        // also create function name in top scope
        //topScope.newElement(s)

        return nextId
    }

    override fun findFunctionIdentifier(s: String): Int? {
        return fnTable[s]
    }

    override fun implementFunction(name: String, args: List<String>) {
        if (currentFunction == null) {
            if (!fnTable.containsKey(name)) {
                throw IllegalStateException("Function name $name not registered. Register first before implementing")
            }

            currentFunction = CompiledFunction(name, args)

            return
        }

        throw IllegalStateException("Cannot start a new function while another one is being processed")
    }

    override fun exitFunction() {
        val currentFunction = this.currentFunction
        if (currentFunction != null) {
            functions[currentFunction.name] = currentFunction

            this.currentFunction = null

            return
        }

        throw IllegalStateException("Cannot end a function. One isn't being processed now")
    }

    override fun newScope() {
        val newScope = ScopeV1(currentScope)
        currentScope = newScope
    }

    override fun exitScope() {
        currentScope = currentScope.parentScope!!
    }

    override fun currentScope(): Scope {
        return currentScope
    }

    override fun nextIP(): Int {
        val currentFunction = this.currentFunction ?: throw IllegalStateException("Cannot call next IP. Not in function context")

        return currentFunction.getOps().size
    }

    override fun addInstruction(op: Op) {
        val currentFunction = this.currentFunction
        if (currentFunction != null) {
            currentFunction.addOp(op)

            return
        }

        throw IllegalStateException("No current function to add instruction to")
    }

    override fun optimize(o: Optimizer) {
        for (fn in functions.values) {
            val optimized = o.optimize(fn.getOps())

            fn.replace(optimized)
        }
    }

    override fun createScaffoldedProgram(c: OutputScaffolding) {
        for (s in strTable) {
            c.setString(s.value, s.key)
        }

        for (f in fnTable) {
            c.setFunction(f.value, f.key, LocalFunctionEntry(functions[f.key]!!.getOps()))
        }
    }
}