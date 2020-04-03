package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.ToluTypeException
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import net.nicholasgwong.tolu.rt.Variable
import net.nicholasgwong.tolu.rt.VariableType
import kotlinx.coroutines.CoroutineScope

class JE : OpHandler {
    override val mnemonic: Mnemonic = Mnemonic.JE

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v1 = vm.pop()

        if (!v1.typeEq(VariableType.BOOLEAN)) {
            throw ToluTypeException("Cannot use variable as flag", v1.type.toString(), "BOOLEAN")
        }

        if (v1.asBoolean()) {
            vm.setNextInstrOffset(o.data.toInt())
        }
    }
}

class JNE : OpHandler {
    override val mnemonic: Mnemonic = Mnemonic.JNE

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v1 = vm.pop()

        if (!v1.typeEq(VariableType.BOOLEAN)) {
            throw ToluTypeException("Cannot use variable as flag", v1.type.toString(), "BOOLEAN")
        }

        if (!v1.asBoolean()) {
            vm.setNextInstrOffset(o.data.toInt())
        }
    }
}

class J : OpHandler {
    override val mnemonic: Mnemonic = Mnemonic.J

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        vm.setNextInstrOffset(o.data.toInt())
    }

}

class Ext : OpHandler {
    override val mnemonic = Mnemonic.EXT

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val nv = o.data.toInt()
        val vs = ArrayList<Variable>()
        val fnStr = vm.pop()

        for (i in 0 until nv) {
            vs.add(vm.pop())
        }

        vs.reverse()

        if (!fnStr.typeEq(VariableType.STRING)) {
            throw ToluTypeException("Ext function call function type should be string", fnStr.type.toString(), "STRING")
        }

        vm.extCall(fnStr.asString(), c, vs)
    }
}

class Call : OpHandler {
    override val mnemonic = Mnemonic.CALL

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val nv = o.data.toInt()
        val vs = ArrayList<Variable>()
        val fn = vm.pop()

        for (i in 0 until nv) {
            vs.add(vm.pop())
        }

        vs.reverse()

        if (!fn.typeEq(VariableType.FUNCTIONREF)) {
            throw ToluTypeException("Cannot call variable", fn.type.toString(), "FUNCTION")
        }

        val realFn = fn.asFnRef().id

        vm.enterFunction(realFn, vs)
    }
}

class Return : OpHandler {
    override val mnemonic = Mnemonic.RETN

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v = vm.pop()

        vm.returnValue = v
        vm.exitFunction()
    }
}