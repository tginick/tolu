package net.nicholasgwong.tolu.compiler.opt

import net.nicholasgwong.tolu.rt.Op

class DefaultOptimizer : Optimizer {
    override fun optimize(ops: List<Op>): List<Op> {
        val nops = ArrayList<Op>()
        nops.addAll(ops)

        return nops // do no optimization
    }

}