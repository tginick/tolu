package net.nicholasgwong.tolu.rt

import net.nicholasgwong.misckt.AutoBuffer
import net.nicholasgwong.tolu.rt.impl.CompiledToluProgramV1
import java.io.RandomAccessFile

interface ToluLoader {
    fun load(fileName: String): CompiledProgram
    fun loadFromBuffer(buf: AutoBuffer): CompiledProgram
}

class ToluV1Loader : ToluLoader {
    override fun load(fileName: String): CompiledProgram {

        val file = RandomAccessFile(fileName, "r")
        file.use {
            val fsize = it.length().toInt()
            if (fsize > MAX_FILE_SIZE) {
                throw IllegalArgumentException("File $fileName exceeds max file size $MAX_FILE_SIZE")
            }

            val buf = AutoBuffer(fsize)
            val oit = it
            buf.use {
                oit.channel.read(buf.backingBuffer())
                buf.startRead()

                return loadFromBuffer(buf)
            }
        }
    }

    override fun loadFromBuffer(buf: AutoBuffer): CompiledProgram {
        val p = CompiledToluProgramV1()
        p.deserializeProgram(buf)

        return p
    }
}