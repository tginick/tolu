package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import kotlinx.coroutines.CoroutineScope

interface OpHandler {
    val mnemonic: Mnemonic

    fun execute(o: Op, c: CoroutineScope, vm: VMState)
}