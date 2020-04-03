package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.rt.impl.vm.dispatch.*
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import kotlinx.coroutines.CoroutineScope

data class OpSpec(val mnemonic: Mnemonic, val handler: OpHandler)

val DISPATCH = arrayListOf(
        object : OpHandler { override fun execute(o: Op, c: CoroutineScope, vm: VMState) {} ; override val mnemonic = Mnemonic.NOP },
        PushI(),
        PushF(),
        PushS(),
        PushB(),

        Add(),
        Sub(),
        Mul(),
        Div(),

        Not(),
        And(),
        Or(),
        GreaterThan(),
        LessThan(),
        GreaterThanEquals(),
        LessThanEquals(),
        Equals(),

        JE(),
        JNE(),
        J(),
        Ext(),
        Call(),

        PushFnRef(),

        GetLocal(),
        SetLocal(),

        Return(),
        PushUnit()
)

fun verifyDispatch() {
    val errors = StringBuilder()
    var hasErrors = false

    for (i in 0 until DISPATCH.size) {
        if (DISPATCH[i].mnemonic.op != i.toByte()) {
            hasErrors = true
            errors.append("DISPATCH entry $i services mnemonic ${DISPATCH[i].mnemonic} but actual mnemonic has code ${DISPATCH[i].mnemonic.op} ")
        }
    }

    if (hasErrors) {
        throw IllegalArgumentException("Cannot initialize dispatch table: $errors")
    }
}