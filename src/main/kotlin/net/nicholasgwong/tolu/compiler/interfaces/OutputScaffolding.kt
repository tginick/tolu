package net.nicholasgwong.tolu.compiler.interfaces

import net.nicholasgwong.tolu.rt.FunctionTableEntry

interface OutputScaffolding {
    fun setString(id: Int, str: String)
    fun setFunction(id: Int, fnName: String, fn: FunctionTableEntry)

}