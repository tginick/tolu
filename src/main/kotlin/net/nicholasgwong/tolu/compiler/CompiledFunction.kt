package net.nicholasgwong.tolu.compiler

import net.nicholasgwong.tolu.rt.Op

class CompiledFunction(val name: String, val args: List<String>) {
    private val ops: MutableList<Op> = ArrayList()

    fun addOp(op: Op) {
        ops.add(op)
    }

    fun getOps(): List<Op> {
        return ops
    }

    fun replace(newOps: List<Op>) {
        ops.clear()

        ops.addAll(newOps)
    }

    fun offsetJumps(offset: Int) {

    }
}