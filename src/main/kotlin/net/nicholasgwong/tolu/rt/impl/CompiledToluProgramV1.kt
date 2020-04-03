package net.nicholasgwong.tolu.rt.impl

import net.nicholasgwong.misckt.AutoBuffer
import net.nicholasgwong.misckt.ext.slf4jLogger
import net.nicholasgwong.tolu.compiler.interfaces.OutputScaffolding
import net.nicholasgwong.tolu.rt.*
import java.util.*

open class CompiledToluProgramV1 : CompiledProgram() {
    companion object {
        private val LOG = slf4jLogger(CompiledToluProgramV1::class)
        private const val MAGIC: String = "KHF01"

        private const val LOCAL_FN_MARKER: Byte = 0x01
        private const val IMPORTED_FN_MARKER: Byte = 0x02
    }

    override val strTable: List<String>
        get() = mutStrTable
    override val fnTable: List<FunctionTableEntry>
        get() = mutFnTable
    override val fnNameTable: Map<String, Int>
        get() = mutFnNameTable

    protected val mutStrTable: MutableList<String> = ArrayList()
    protected val mutFnTable: MutableList<FunctionTableEntry> = ArrayList()
    protected val mutFnNameTable: MutableMap<String, Int> = HashMap()

    override fun deserializeProgram(buffer: AutoBuffer) {
        try {
            readMagic(buffer)
            readStringTable(buffer)
            readFunctions(buffer)
        } catch (e: Exception) {
            LOG.error("Error reading program from buffer: ${e.message}")

            throw e
        }
    }

    override fun serializeProgram(): AutoBuffer {
        if (this !is OutputScaffolding) {
            throw UnsupportedOperationException("Cannot save program in read only context")
        }

        if (fnTable.size != fnNameTable.size) {
            throw IllegalStateException("Function table size ${fnTable.size} does not match name table size ${fnNameTable.size}")
        }

        val buf = AutoBuffer()
        try {
            writeMagic(buf)
            writeStringTable(buf)
            writeFunctions(buf)
        } catch (e: Exception) {
            LOG.error("Error writing program to buffer: ${e.message}")
            buf.close()

            throw e
        }

        buf.startRead() // ready to read!
        return buf
    }

    override fun createDisasm(): String {
        val sb = StringBuilder()

        sb.append("String Table:\n")
        for (i in strTable.indices) {
            sb.append("  $i: ${strTable[i]}\n")
        }

        sb.append("\nFunction ID Table:\n")
        for (fnName in fnNameTable.keys) {
            sb.append("  ${fnNameTable[fnName]!!}: $fnName\n")
        }

        sb.append("\nImplementation:\n")
        for (i in fnTable.indices) {
            when (fnTable[i]) {
                is InvalidFunctionEntry -> sb.append("  $i: INVALID\n\n")
                is ImportedFunctionEntry -> {
                    sb.append("  $i: Imported\n")

                    val realEntry = fnTable[i] as ImportedFunctionEntry
                    sb.append("    ${realEntry.sourceFile}:${realEntry.fnName}\n\n")
                }
                is LocalFunctionEntry -> {
                    sb.append("  $i: Local\n")

                    val realEntry = fnTable[i] as LocalFunctionEntry
                    for (j in realEntry.ops.indices) {
                        sb.append("    $j: ${realEntry.ops[j]}\n")
                    }
                }
            }
        }

        return sb.toString()
    }

    private fun writeMagic(b: AutoBuffer) {
        b.writeChars(MAGIC)
    }

    private fun readMagic(f: AutoBuffer) {
        val readMagic = f.readChars(MAGIC.length)
        if (readMagic != MAGIC) {
            throw InvalidProgramFormatException("Unexpected magic $readMagic")
        }
    }

    private fun writeStringTable(f: AutoBuffer) {
        f.writeInt(strTable.size)
        for (element in strTable) {
            writeString(f, element)
        }
    }

    private fun readStringTable(f: AutoBuffer) {
        val strTableLen = f.readInt()

        if (strTableLen < 0) {
            throw IllegalArgumentException("Unexpected str table len $strTableLen at offset ${f.position()}")
        }

        for (i in 0 until strTableLen) {
            val str = readString(f)
            mutStrTable.add(str)
        }
    }

    private fun writeFunctions(f: AutoBuffer) {
        f.writeInt(fnNameTable.size)

        // write the name table
        for (fnNamePair in fnNameTable) {
            f.writeInt(fnNamePair.value)
            writeString(f, fnNamePair.key)
        }

        // write the functions
        for (fnDef in fnTable) {
            writeFunction(f, fnDef)
        }
    }

    private fun readFunctions(f: AutoBuffer) {
        val fnTableLen = f.readInt()

        if (fnTableLen < 0) {
            throw IllegalArgumentException("Unexpected fn table len $fnTableLen at offset ${f.position()}")
        }

        for (i in 0 until fnTableLen) {
            val fnId = f.readInt()
            val fnName = readString(f)

            mutFnNameTable[fnName] = fnId
        }

        for (i in 0 until fnTableLen) {
            val fn = readFunction(f)

            mutFnTable.add(fn)
        }
    }

    private fun writeString(f: AutoBuffer, s: String) {
        f.writeInt(s.length)
        f.writeChars(s)
    }

    private fun readString(f: AutoBuffer): String {
        val strLen = f.readInt()
        if (strLen <= 0) {
            throw IllegalArgumentException("Unexpected String length $strLen found at offset ${f.position()}")
        }

        return f.readChars(strLen)
    }

    private fun writeFunction(f: AutoBuffer, fn: FunctionTableEntry) {
        when (fn) {
            is InvalidFunctionEntry -> throw IllegalStateException("There are one or more invalid function entries in the program scaffolding.")
            is LocalFunctionEntry -> writeLocalFunction(f, fn)
            is ImportedFunctionEntry -> writeImportedFunction(f, fn)
        }
    }

    private fun writeLocalFunction(f: AutoBuffer, fn: LocalFunctionEntry) {
        // mark this fn as a local function (in this file)
        f.writeByte(LOCAL_FN_MARKER)

        // instruction count
        f.writeInt(fn.ops.size)

        // instructions!
        for (op in fn.ops) {
            f.writeLong(op.asBits())
        }
    }

    private fun writeImportedFunction(f: AutoBuffer, fn: ImportedFunctionEntry) {
        f.writeByte(IMPORTED_FN_MARKER)

        writeString(f, fn.sourceFile)

        writeString(f, fn.fnName)
    }

    private fun readFunction(f: AutoBuffer): FunctionTableEntry {
        val fType = f.readByte()
        return when (fType) {
            LOCAL_FN_MARKER -> readLocalFunction(f)
            IMPORTED_FN_MARKER -> readImportedFunction(f)
            else -> throw IllegalArgumentException("Unexpected function type $fType at offset ${f.position()}")
        }
    }

    private fun readLocalFunction(f: AutoBuffer): LocalFunctionEntry {
        val numOps = f.readInt()
        if (numOps < 0) {
            throw IllegalArgumentException("Invalid number ops $numOps for offset ${f.position()}")
        }

        val decodedOps: MutableList<Op> = ArrayList()
        for (i in 0 until numOps) {
            val rawOp = f.readLong()
            val decoded = Op.decode(rawOp)

            decodedOps.add(decoded)
        }

        return LocalFunctionEntry(decodedOps)
    }

    private fun readImportedFunction(f: AutoBuffer): ImportedFunctionEntry {
        val sourceFile = readString(f)
        val fnName = readString(f)

        return ImportedFunctionEntry(sourceFile, fnName)
    }
}
