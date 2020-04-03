package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.FunctionRef
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import net.nicholasgwong.tolu.rt.Variable
import net.nicholasgwong.tolu.rt.VariableType
import kotlinx.coroutines.CoroutineScope

class PushI : OpHandler {
    override val mnemonic = Mnemonic.PUSHI

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val d = o.data.toInt()
        vm.push(Variable(VariableType.INT, d))
    }
}

class PushF : OpHandler {
    override val mnemonic = Mnemonic.PUSHF

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val d = Float.fromBits(o.data.toInt()) // TODO MAKE SURE THIS WORKS
        vm.push(Variable(VariableType.FLOAT, d))
    }
}

class PushS : OpHandler {
    override val mnemonic: Mnemonic = Mnemonic.PUSHS

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        vm.push(Variable(VariableType.STRING, vm.getString(o.data.toInt())))
    }
}

class PushB : OpHandler {
    override val mnemonic: Mnemonic = Mnemonic.PUSHB

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        vm.push(Variable(VariableType.BOOLEAN, o.data != 0L))
    }
}

class PushFnRef : OpHandler {
    override val mnemonic = Mnemonic.PUSHFNREF

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        vm.push(Variable(VariableType.FUNCTIONREF, FunctionRef(o.data.toInt())))
    }
}

class PushUnit : OpHandler {
    override val mnemonic = Mnemonic.PUSHUNIT

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        vm.push(Variable(VariableType.UNIT, Unit))
    }
}