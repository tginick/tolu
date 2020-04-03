package net.nicholasgwong.tolu.rt.impl.vm.dispatch

import net.nicholasgwong.tolu.ToluTypeException
import net.nicholasgwong.tolu.rt.Mnemonic
import net.nicholasgwong.tolu.rt.Op
import net.nicholasgwong.tolu.rt.impl.vm.VMState
import net.nicholasgwong.tolu.rt.Variable
import net.nicholasgwong.tolu.rt.VariableType
import kotlinx.coroutines.CoroutineScope
import net.nicholasgwong.tolu.rt.impl.vm.dispatch.OpHandler
import kotlin.math.roundToInt

class Add : OpHandler {
    companion object {
        private val intAdd = { v1: Variable, v2: Variable ->
            val nd = v1.asInt() + when(v2.type) {
                VariableType.INT -> v2.asInt()
                VariableType.FLOAT -> v2.asFloat().roundToInt()
                else -> throw ToluTypeException("Cannot add to INT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.INT, nd)
        }

        private val fltAdd = { v1: Variable, v2: Variable ->
            val nd = v1.asFloat() + when(v2.type) {
                VariableType.FLOAT -> v2.asFloat()
                VariableType.INT -> v2.asInt().toFloat()
                else -> throw ToluTypeException("Cannot add to FLOAT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.FLOAT, nd)
        }

        private val strAdd = { v1: Variable, v2: Variable ->
            val nd = v1.asString() + v2.data.toString()

            Variable(VariableType.STRING, nd)
        }

        private val invalidAdd = { v1 : Variable, v2 : Variable ->
            throw ToluTypeException("Cannot add non numeric types", "${v1.type}, ${v2.type}", "NUMERIC")
        }

        @Suppress("PrivatePropertyName")
        private val ADD_DISPATCH = arrayListOf(
                intAdd,
                fltAdd,
                invalidAdd,
                strAdd
        )
    }
    override val mnemonic: Mnemonic = Mnemonic.ADD


    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = try {
            ADD_DISPATCH[v1.type.ord](v1, v2)
        } catch (e: IndexOutOfBoundsException) {
            invalidAdd(v1, v2)
        }

        vm.push(nv)
    }


}

class Sub : OpHandler {
    companion object {
        private val intSub = { v1: Variable, v2: Variable ->
            val nd = v1.asInt() - when(v2.type) {
                VariableType.INT -> v2.asInt()
                VariableType.FLOAT -> v2.asFloat().roundToInt()
                else -> throw ToluTypeException("Cannot sub from INT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.INT, nd)
        }

        private val fltSub = { v1: Variable, v2: Variable ->
            val nd = v1.asFloat() - when(v2.type) {
                VariableType.FLOAT -> v2.asFloat()
                VariableType.INT -> v2.asInt().toFloat()
                else -> throw ToluTypeException("Cannot sub from FLOAT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.FLOAT, nd)
        }

        private val invalidSub = { v1 : Variable, v2 : Variable ->
            throw ToluTypeException("Cannot sub non numeric types", "${v1.type}, ${v2.type}", "NUMERIC")
        }

        @Suppress("PrivatePropertyName")
        private val SUB_DISPATCH = arrayListOf(
                intSub,
                fltSub
        )
    }

    override val mnemonic = Mnemonic.SUB

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = try {
            SUB_DISPATCH[v1.type.ord](v1, v2)
        } catch (e: IndexOutOfBoundsException) {
            invalidSub(v1, v2)
        }

        vm.push(nv)
    }
}

class Mul : OpHandler {
    companion object {
        private val intMul = { v1: Variable, v2: Variable ->
            val nd = v1.asInt() * when(v2.type) {
                VariableType.INT -> v2.asInt()
                VariableType.FLOAT -> v2.asFloat().roundToInt()
                else -> throw ToluTypeException("Cannot mul to INT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.INT, nd)
        }

        private val fltMul = { v1: Variable, v2: Variable ->
            val nd = v1.asFloat() * when(v2.type) {
                VariableType.FLOAT -> v2.asFloat()
                VariableType.INT -> v2.asInt().toFloat()
                else -> throw ToluTypeException("Cannot mul to FLOAT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.FLOAT, nd)
        }

        private val invalidMul = { v1 : Variable, v2 : Variable ->
            throw ToluTypeException("Cannot mul non numeric types", "${v1.type}, ${v2.type}", "NUMERIC")
        }

        @Suppress("PrivatePropertyName")
        private val MUL_DISPATCH = arrayListOf(
                intMul,
                fltMul
        )
    }

    override val mnemonic: Mnemonic = Mnemonic.MUL

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = try {
            MUL_DISPATCH[v1.type.ord](v1, v2)
        } catch (e: IndexOutOfBoundsException) {
            invalidMul(v1, v2)
        }

        vm.push(nv)
    }
}

class Div : OpHandler {
    companion object {
        private val intDiv = { v1: Variable, v2: Variable ->
            val nd  = v1.asInt() / when(v2.type) {
                VariableType.INT -> v2.asInt()
                VariableType.FLOAT -> v2.asFloat().roundToInt()
                else -> throw ToluTypeException("Cannot div from INT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.INT, nd)
        }

        private val fltDiv = { v1: Variable, v2: Variable ->
            val nd = v1.asFloat() / when(v2.type) {
                VariableType.FLOAT -> v2.asFloat()
                VariableType.INT -> v2.asInt().toFloat()
                else -> throw ToluTypeException("Cannot div from FLOAT", v2.type.toString(), "NUMERIC")
            }

            Variable(VariableType.FLOAT, nd)
        }

        private val invalidDiv = { v1 : Variable, v2 : Variable ->
            throw ToluTypeException("Cannot div non numeric types", "${v1.type}, ${v2.type}", "NUMERIC")
        }

        @Suppress("PrivatePropertyName")
        private val DIV_DISPATCH = arrayListOf(
                intDiv,
                fltDiv
        )
    }

    override val mnemonic = Mnemonic.DIV

    override fun execute(o: Op, c: CoroutineScope, vm: VMState) {
        val v2 = vm.pop()
        val v1 = vm.pop()

        val nv = try {
            DIV_DISPATCH[v1.type.ord](v1, v2)
        } catch (e: IndexOutOfBoundsException) {
            invalidDiv(v1, v2)
        }

        vm.push(nv)
    }
}