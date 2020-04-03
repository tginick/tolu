package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import kotlinx.coroutines.CoroutineScope

class GetLocal : OpHandler {
    override val mnemonic = Mnemonic.GETL

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val d = o.data.toInt()

        vm.push(vm.getLocal(d))
    }
}

class SetLocal : OpHandler {
    override val mnemonic = Mnemonic.SETL

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v = vm.pop()
        val d = o.data.toInt()

        vm.setLocal(d, v)
    }
}