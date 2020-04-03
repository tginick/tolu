package net.nicholasgwong.tolu.rt.impl.vm

import kotlinx.coroutines.CoroutineScope
import net.nicholasgwong.tolu.rt.Variable

enum class ToluProgramState {
    Inactive,
    Running,
    Sleeping,
    Finalizing,
}

interface VMState {
    var currentState: ToluProgramState
    var returnValue: Variable?

    fun setLocal(i: Int, v: Variable)
    fun getLocal(i: Int): Variable

    fun push(v: Variable)
    fun pop(): Variable

    fun getString(i: Int): String

    fun setNextInstrOffset(newOffset: Int)

    fun enterFunction(id: Int, vars: List<Variable>)
    fun exitFunction()

    fun extCall(fnName: String, c: CoroutineScope, vars: List<Variable>)

    fun pause()
    fun wake()
}