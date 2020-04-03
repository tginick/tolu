package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.ToluTypeException
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import net.nicholasgwong.tolu.rt.Variable
import net.nicholasgwong.tolu.rt.VariableType
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

class Not : OpHandler {
    override val mnemonic = Mnemonic.NOT

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v1 = vm.pop()

        if (v1.typeEq(VariableType.BOOLEAN)) {
            vm.push(Variable(VariableType.BOOLEAN, !v1.asBoolean()))
        } else {
            throw ToluTypeException("Cannot negate", v1.type.toString(), "BOOLEAN")
        }
    }
}

class And : OpHandler {
    override val mnemonic = Mnemonic.AND

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        if (!v1.typeEq(VariableType.BOOLEAN)) {
            throw ToluTypeException("First arg to AND is not boolean", v1.type.toString(), "BOOLEAN")
        }

        if (!v2.typeEq(VariableType.BOOLEAN)) {
            throw ToluTypeException("Second arg to AND is not boolean", v2.type.toString(), "BOOLEAN")
        }

        vm.push(Variable(VariableType.BOOLEAN, v1.asBoolean() && v2.asBoolean()))
    }
}

class Or : OpHandler {
    override val mnemonic = Mnemonic.OR

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        if (!v1.typeEq(VariableType.BOOLEAN)) {
            throw ToluTypeException("First arg to AND is not boolean", v1.type.toString(), "BOOLEAN")
        }

        if (!v2.typeEq(VariableType.BOOLEAN)) {
            throw ToluTypeException("Second arg to AND is not boolean", v2.type.toString(), "BOOLEAN")
        }

        vm.push(Variable(VariableType.BOOLEAN, v1.asBoolean() || v2.asBoolean()))
    }
}

private val I_COMP_DISPATCH = arrayListOf(
        { v1: Int, v2: Int -> v1 > v2 },
        { v1: Int, v2: Int -> v1 < v2 },
        { v1: Int, v2: Int -> v1 >= v2 },
        { v1: Int, v2: Int -> v1 <= v2 }
)

private val F_COMP_DISPATCH = arrayListOf(
        { v1: Float, v2: Float -> v1 > v2 },
        { v1: Float, v2: Float -> v1 < v2 },
        { v1: Float, v2: Float -> v1 >= v2 },
        { v1: Float, v2: Float -> v1 <= v2 }
)

private fun doComparison(v1: Variable, v2: Variable, fnIdx: Int): Variable {
    return when {
        v1.typeEq(VariableType.INT) -> when {
            v2.typeEq(VariableType.INT) -> Variable(VariableType.BOOLEAN, I_COMP_DISPATCH[fnIdx](v1.asInt(), v2.asInt()))
            v2.typeEq(VariableType.FLOAT) -> Variable(VariableType.BOOLEAN, I_COMP_DISPATCH[fnIdx](v1.asInt(), v2.asFloat().roundToInt()))
            else -> throw ToluTypeException("Cannot execute comparison", v2.type.toString(), "NUMERIC")
        }
        v1.typeEq(VariableType.FLOAT) -> when {
            v2.typeEq(VariableType.FLOAT) -> Variable(VariableType.BOOLEAN, F_COMP_DISPATCH[fnIdx](v1.asFloat(), v2.asFloat()))
            v2.typeEq(VariableType.INT) -> Variable(VariableType.BOOLEAN, F_COMP_DISPATCH[fnIdx](v1.asFloat(), v2.asInt().toFloat()))
            else -> throw ToluTypeException("Cannot execute comparison", v2.type.toString(), "NUMERIC")
        }
        else -> throw ToluTypeException("Cannot execute comparison. First operand of unexpected type.", v1.type.toString(), "NUMERIC")
    }
}

private const val GT_IDX = 0
private const val LT_IDX = 1
private const val GTE_IDX = 2
private const val LTE_IDX = 3

class GreaterThan : OpHandler {
    override val mnemonic = Mnemonic.GT

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = doComparison(v1, v2, GT_IDX)
        vm.push(nv)
    }
}

class LessThan : OpHandler {
    override val mnemonic = Mnemonic.LT

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = doComparison(v1, v2, LT_IDX)
        vm.push(nv)
    }
}

class GreaterThanEquals : OpHandler {
    override val mnemonic = Mnemonic.GTE

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = doComparison(v1, v2, GTE_IDX)
        vm.push(nv)
    }
}

class LessThanEquals : OpHandler {
    override val mnemonic = Mnemonic.LTE

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = doComparison(v1, v2, LTE_IDX)

        vm.push(nv)
    }
}

class Equals : OpHandler {
    override val mnemonic = Mnemonic.EQ

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        if (v1.typeEq(v2.type)) {
            vm.push(Variable(VariableType.BOOLEAN, v1 == v2))
        } else {
            vm.push(Variable(VariableType.BOOLEAN, false))
        }
    }
}