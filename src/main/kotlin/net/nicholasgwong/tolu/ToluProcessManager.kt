package net.nicholasgwong.tolu

import net.nicholasgwong.misckt.Cache
import net.nicholasgwong.misckt.ext.formatException
import net.nicholasgwong.misckt.ext.slf4jLogger
import net.nicholasgwong.misckt.newLRUCache
import net.nicholasgwong.tolu.compiler.ToluC
import net.nicholasgwong.tolu.rt.CompiledProgram
import net.nicholasgwong.tolu.rt.impl.vm.ExtFn
import java.util.*
import java.util.concurrent.CountDownLatch

sealed class ToluExecuteResult
object ToluNoResult : ToluExecuteResult()
class ToluSuccess(val result: Any): ToluExecuteResult()
object ToluSuccessAsync : ToluExecuteResult()

class ToluProcess(private val vmSubmitter: ToluVMSubmitter, private val procHandle: ToluVMHandle) {
    fun run(functionName: String): Any {
        // must join this execution
        val latch = CountDownLatch(1)
        vmSubmitter.start(procHandle, functionName) { latch.countDown() }

        latch.await()

        return vmSubmitter.retrieveReturnValue(procHandle)?.data ?: Unit
    }

    fun sleep() {
        vmSubmitter.pause(procHandle)
    }

    fun wake(): Boolean {
        return vmSubmitter.wake(procHandle)
    }

    fun isBusy(): Boolean {
        return vmSubmitter.isActive(procHandle)
    }

}

class ToluVMProcessManager(private val vm: ToluVM, private val vmSubmitter: ToluVMSubmitter) {
    companion object {
        private val LOG = slf4jLogger(ToluVMProcessManager::class)
    }

    private val processes: MutableMap<String, ToluProcess> = HashMap()
    private val programCache: Cache<CompiledProgram> = newLRUCache(100)


    fun bindExtFns(fns: Map<String, ExtFn>) {
        for (fn in fns) {
            vm.bindExternalFunction(fn.key, fn.value)
        }
    }

    fun start() {
        vm.runVM()
    }

    fun newProcess(programFilePath: String): String {
        var newId: String
        while (true) {
            newId = generateRandomId(10)
            if (!processes.containsKey(newId)) {
                break
            }
        }

        val cachedProgram = try {
            programCache.retrieve(programFilePath) ?: loadAndCompile(programFilePath).also { programCache.add(programFilePath, it) }
        } catch (e: Exception) {
            val exceptionAsString = formatException(e)

            LOG.error("Failed to load $programFilePath: $exceptionAsString")

            return ""
        }

        val handle = vmSubmitter.create(cachedProgram)

        val ToluHandle = ToluProcess(vmSubmitter, handle)

        processes[newId] = ToluHandle

        return newId
    }

    fun executeFunction(processId: String, functionName: String): Any {
        val proc = processes[processId]
        if (proc != null) {
            ToluSuccess(proc.run(functionName))
        }

        return ToluNoResult
    }

    fun closeProcess(processId: String, force: Boolean): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun generateRandomId(maxLen: Int): String {
        assert(maxLen > 0)

        val sb = StringBuilder()
        val r = Random()

        for (i in 0 until maxLen) {
            val nextIdx = r.nextInt(ID_CHRS.length)
            sb.append(ID_CHRS[nextIdx])
        }

        return sb.toString()
    }

    private fun loadAndCompile(script: String): CompiledProgram {
        val programBuf = ToluC.compileString(script)

        return vm.getLoader().loadFromBuffer(programBuf)
    }
}