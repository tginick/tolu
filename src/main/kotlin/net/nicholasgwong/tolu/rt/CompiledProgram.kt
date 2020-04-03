package net.nicholasgwong.tolu.rt

import net.nicholasgwong.misckt.AutoBuffer

sealed class FunctionTableEntry
class InvalidFunctionEntry : FunctionTableEntry()
data class LocalFunctionEntry(val ops: List<Op>) : FunctionTableEntry()
data class ImportedFunctionEntry(val sourceFile: String, val fnName: String) : FunctionTableEntry()

class InvalidProgramFormatException(details: String) : Exception(details)

abstract class CompiledProgram {
    abstract val strTable: List<String>
    abstract val fnTable: List<FunctionTableEntry>
    abstract val fnNameTable: Map<String, Int> // function name -> function id

    abstract fun deserializeProgram(buffer: AutoBuffer)
    abstract fun serializeProgram(): AutoBuffer

    abstract fun createDisasm(): String
}

const val MAX_FILE_SIZE = 2 * 1024 * 1024 // 2 MB