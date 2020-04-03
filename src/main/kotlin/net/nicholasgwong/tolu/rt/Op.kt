package net.nicholasgwong.tolu.rt

import net.nicholasgwong.misckt.ext.slf4jLogger

// there are 7 bytes available to use in data.
// highest byte is reserved for instruction
class Op private constructor(val instruction: Mnemonic, inData: Long) {
    companion object {
        private val LOG = slf4jLogger(Op::class)

        fun makeFloat(instruction: Mnemonic, data: Float): Op {
            val bits = data.toBits()

            return make(instruction, bits.toLong())
        }


        fun make(instruction: Mnemonic, data: Long): Op {
            val tdata = formatData(data)

            return Op(instruction, tdata)
        }

        fun formatData(raw: Long): Long {
            //return raw and 0x3FFFFFFFFFFFFFFF
            return raw and (-1L ushr 8)
        }

        fun decode(raw: Long): Op {
            val insByte = (raw ushr 56).toByte()

            val decodedIns = OP_REVERSE_LOOKUP[insByte] ?: throw IllegalArgumentException("Unknown opcode $insByte")

            return Op(decodedIns, formatData(raw))
        }
    }

    var scaffoldingInfo: String? = null

    var data = formatData(inData)
        set(value) {
            field = formatData(value)
        }

    fun asBits(): Long {
        return (instruction.op.toLong() shl 56) or data
    }

    override fun toString(): String {
        return "$instruction  $data"
    }
}
