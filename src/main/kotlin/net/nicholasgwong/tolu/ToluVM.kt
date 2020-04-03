package net.nicholasgwong.tolu


import net.nicholasgwong.tolu.rt.CompiledProgram
import net.nicholasgwong.tolu.rt.ToluLoader
import net.nicholasgwong.tolu.rt.Variable
import net.nicholasgwong.tolu.rt.impl.vm.ExtFn
import java.io.Closeable

typealias ToluVMHandle = Int
const val ID_CHRS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

class ToluNoSuchProcessException(requested: Int) : RuntimeException("No such process id $requested")
class ToluNoSuchFunctionException(requested: String) : RuntimeException("No such function id $requested")
class ToluNoSuchLocalException(i: Int) : RuntimeException("No such local $i")
class ToluTypeException(msg: String, actual: String, expected: String) : RuntimeException("$msg. Expected $expected. Actual $actual")
class ToluExtCallException(msg: String) : RuntimeException(msg)

interface ToluVMSubmitter {
    fun create(program: CompiledProgram): ToluVMHandle
    fun setAdditionalData(handle: ToluVMHandle, data: Any)
    fun start(handle: ToluVMHandle, entryPoint: String, callback: () -> Unit = {})
    fun retrieveReturnValue(handle: ToluVMHandle): Variable?
    fun pause(handle: ToluVMHandle)
    fun wake(handle: ToluVMHandle): Boolean
    fun isActive(handle: ToluVMHandle): Boolean
    fun closeProcess(handle: ToluVMHandle, force: Boolean): Boolean
}

interface ToluVM : Closeable {
    fun getLoader(): ToluLoader
    fun runVM()
    fun bindExternalFunction(fnName: String, fnImpl: ExtFn)
}
