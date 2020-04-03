package net.nicholasgwong.tolu.compiler.opt

import net.nicholasgwong.tolu.rt.Op

interface Optimizer {
    fun optimize(ops: List<Op>): List<Op>
}