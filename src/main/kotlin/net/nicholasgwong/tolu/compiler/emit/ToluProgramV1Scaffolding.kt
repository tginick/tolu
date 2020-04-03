package net.nicholasgwong.tolu.compiler.emit

import net.nicholasgwong.misckt.ext.slf4jLogger
import net.nicholasgwong.tolu.compiler.interfaces.OutputScaffolding
import net.nicholasgwong.tolu.rt.FunctionTableEntry
import net.nicholasgwong.tolu.rt.InvalidFunctionEntry
import net.nicholasgwong.tolu.rt.impl.CompiledToluProgramV1


class ToluProgramV1Scaffolding : CompiledToluProgramV1(), OutputScaffolding {
    companion object {
        private val LOG = slf4jLogger(ToluProgramV1Scaffolding::class)
    }

    override fun setString(id: Int, str: String) {
        if (id < 0) {
            throw IllegalArgumentException("Cannot set string to negative id")
        }

        if (id >= mutStrTable.size) {
            val numToGrow = id - mutStrTable.size + 1
            growStrTable(numToGrow)
        }

        mutStrTable[id] = str
    }

    override fun setFunction(id: Int, fnName: String, fn: FunctionTableEntry) {
        if (id < 0) {
            throw IllegalArgumentException("Cannot set function to negative id")
        }

        if (id >= mutFnTable.size) {
            val numToGrow = id - mutFnTable.size + 1
            growFnTable(numToGrow)
        }

        mutFnTable[id] = fn
        mutFnNameTable[fnName] = id
    }

    private fun growStrTable(numToGrow: Int) {
        for (i in 0 until numToGrow) {
            mutStrTable.add("")
        }
    }

    private fun growFnTable(numToGrow: Int) {
        for (i in 0 until numToGrow) {
            mutFnTable.add(InvalidFunctionEntry())
        }
    }

}