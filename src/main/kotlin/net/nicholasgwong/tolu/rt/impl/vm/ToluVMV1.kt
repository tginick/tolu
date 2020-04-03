package net.nicholasgwong.tolu.rt.impl.vm

import net.nicholasgwong.misckt.ext.formatException
import net.nicholasgwong.misckt.ext.slf4jLogger
import net.nicholasgwong.tolu.rt.impl.vm.dispatch.DISPATCH
import net.nicholasgwong.tolu.rt.impl.vm.dispatch.verifyDispatch
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import net.nicholasgwong.tolu.*
import net.nicholasgwong.tolu.rt.*
import java.util.*
import kotlin.collections.HashMap

private const val DEFAULT_TIME_SLICE = 60
private const val MAX_STACK_LEVEL = 256

class ToluStackException(s: String) : RuntimeException(s)
class ToluNoActiveFrameException : IllegalStateException("No active frame")
class ToluStackLevelTooDeepException : IllegalStateException("Stack level has exceeded max $MAX_STACK_LEVEL")

typealias ExtFn = (VMState, CoroutineScope, List<Variable>) -> Unit

data class ExtInvocation(val c: CoroutineScope)

private data class ToluVMFrame(val parentFrame: ToluVMFrame?, val fnId: Int) {
    var ip: Int = 0
    val stack: Deque<Variable> = ArrayDeque()
    val locals: MutableMap<Int, Variable> = HashMap()
}

private class ToluVMState(val program: CompiledProgram, private val extFns: Map<String, ExtFn>) : VMState {
    companion object {
        private const val RETURN_OFFSET: Int = -10
        private val LOG = slf4jLogger(ToluVMState::class)
    }

    override var currentState: ToluProgramState = ToluProgramState.Inactive
    override var returnValue: Variable? = null

    val frames : ArrayDeque<ToluVMFrame> = ArrayDeque()
    var ipOffset: Int? = null
    var additional: Any? = null

    var execDoneCallback: (() -> Unit)? = null

    val stateMutex = Mutex(false)


    fun currentFrame(): ToluVMFrame? = frames.peek()

    fun newFrame(fnId: Int): ToluVMFrame {
        LOG.debug("New frame: $fnId")

        if (frames.size == MAX_STACK_LEVEL) {
            throw ToluStackLevelTooDeepException()
        }

        val nf = ToluVMFrame(currentFrame(), fnId)
        frames.add(nf)

        return nf
    }

    fun popFrame() {
        val cframe = currentFrame()
        if (cframe != null) {
            if (cframe.parentFrame != null) {
                LOG.debug("Pop Frame: Has Parent")

                frames.pop()

                if (returnValue != null) {
                    val returnValue = returnValue!!

                    push(returnValue)

                    this.returnValue = null
                }
            } else {
                LOG.debug("Pop Frame: No Parent. Bye bye")
                currentState = ToluProgramState.Inactive

                execDoneCallback?.invoke()
                execDoneCallback = null
            }

            return
        }

        throw ToluNoActiveFrameException()
    }

    suspend fun setFinishedCallback(callback: () -> Unit) {
        stateMutex.lock()

        execDoneCallback = callback

        stateMutex.unlock()
    }

    suspend fun setAdditionalData(additional: Any) {
        stateMutex.lock()

        this.additional = additional

        stateMutex.unlock()
    }

    override fun getLocal(i: Int): Variable {
        val l = (currentFrame()?.locals ?: throw ToluNoActiveFrameException())[i] ?: throw ToluNoSuchLocalException(i)
        LOG.debug("  GetLocal $i => $l")

        return l
    }

    override fun setLocal(i: Int, v: Variable) {
        LOG.debug("  SetLocal $i: $v")
        currentFrame()?.locals?.put(i, v)
    }

    override fun push(v: Variable) {
        currentFrame()?.stack?.push(v) ?: ToluNoActiveFrameException()
    }

    override fun pop(): Variable {
        return (currentFrame()?.stack ?: throw ToluNoActiveFrameException()).pop() ?: throw ToluStackException("Cannot pop. Stack Empty")
    }

    override fun getString(i: Int): String {
        return program.strTable[i]
    }

    override fun setNextInstrOffset(newOffset: Int) {
        ipOffset = newOffset
    }

    override fun enterFunction(id: Int, vars: List<Variable>) {
        LOG.debug("Enter function: $id")
        newFrame(id)

        var i = 1
        for (v in vars) {
            val vc = v.copy()

            setLocal(i, vc)
            i++
        }
    }

    override fun exitFunction() {
        LOG.debug("Exit function")
        ipOffset = RETURN_OFFSET
    }

    override fun extCall(fnName: String, c: CoroutineScope, vars: List<Variable>) {
        if (extFns.containsKey(fnName)) {
            extFns[fnName]!!(this, c, vars)
        }
    }

    override fun pause() {
        currentState = ToluProgramState.Sleeping
    }

    override fun wake() {
        currentState = ToluProgramState.Running
    }

    suspend fun restart(fnId: Int) {
        stateMutex.lock()

        frames.clear()

        newFrame(fnId)

        currentState = ToluProgramState.Running

        stateMutex.unlock()
    }

    suspend fun finalizeProcess() {
        stateMutex.lock()

        currentState = ToluProgramState.Finalizing

        stateMutex.unlock()
    }

    fun execute(c: CoroutineScope) {
        val fn = program.fnTable[currentFrame()?.fnId ?: throw ToluNoActiveFrameException()]
        when (fn) {
            is LocalFunctionEntry -> {
                val frame = currentFrame()!!
                val ip = frame.ip

                if (ip >= fn.ops.size || ip == RETURN_OFFSET) {
                    // check if we should return or just end the program
                    LOG.debug("Function exit detected")
                    popFrame()

                    return
                }

                executeOp(c, fn.ops[ip])

                frame.ip = this.ipOffset ?: ++frame.ip

                LOG.debug("Execute IP: $ip = ${fn.ops[ip]}. Next: ${frame.ip}")


                this.ipOffset = null
            }

            is ImportedFunctionEntry -> {
                execDoneCallback?.invoke()
                execDoneCallback = null

                currentState = ToluProgramState.Inactive

                TODO("Imported functions are not supported")
            }

            is InvalidFunctionEntry -> {
                execDoneCallback?.invoke()
                execDoneCallback = null

                currentState = ToluProgramState.Inactive

                throw IllegalArgumentException("Invalid function table entry detected")
            }
        }
    }

    private fun executeOp(c: CoroutineScope, op: Op) {
        DISPATCH[op.instruction.op.toInt()].execute(op, c,this)
    }
}

class ToluVMV1 : ToluVMSubmitter, ToluVM {
    companion object {
        private val LOG = slf4jLogger(ToluVMV1::class)
    }

    private val handles: MutableMap<ToluVMHandle, ToluVMState> = HashMap()

    private var isRunning: Boolean = false
    private var vmCoroutineHandle: Job? = null

    private val handleTableLock: Mutex = Mutex(false)
    private val loopChannel: Channel<Unit> = Channel()
    private var extFns: MutableMap<String, ExtFn> = HashMap()

    init {
        verifyDispatch()
    }

    override fun create(program: CompiledProgram): ToluVMHandle {
        return runBlocking {
            handleTableLock.lock()

            val newId = handles.size
            handles[newId] = ToluVMState(program, extFns)

            handleTableLock.unlock()

            newId
        }
    }

    override fun start(handle: ToluVMHandle, entryPoint: String, callback: () -> Unit) {
        runBlocking {
            handleTableLock.lock()

            try {
                val state = handles[handle] ?: throw ToluNoSuchProcessException(handle)
                val fnId = state.program.fnNameTable[entryPoint] ?: throw ToluNoSuchFunctionException(entryPoint)

                state.setFinishedCallback(callback)
                state.restart(fnId)

                if (loopChannel.isEmpty) {
                    LOG.info("Queuing new execution. Waking up.")
                    loopChannel.send(Unit)
                }
            } finally {
                handleTableLock.unlock()
            }
        }
    }

    override fun setAdditionalData(handle: ToluVMHandle, data: Any) {
        runBlocking {
            handleTableLock.lock()

            try {
                val state = handles[handle] ?: throw ToluNoSuchProcessException(handle)
                state.setAdditionalData(data)
            } finally {
                handleTableLock.unlock()
            }
        }

    }

    override fun retrieveReturnValue(handle: ToluVMHandle): Variable? {
        return runBlocking {
            handleTableLock.lock()
            try {
                val state = handles[handle] ?: throw ToluNoSuchProcessException(handle)

                state.stateMutex.lock()
                val returnValue = state.returnValue

                state.returnValue = null
                state.stateMutex.unlock()

                returnValue
            } finally {
                handleTableLock.unlock()
            }
        }
    }

    override fun pause(handle: ToluVMHandle) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun wake(handle: ToluVMHandle): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isActive(handle: ToluVMHandle): Boolean {
        return runBlocking {
            handleTableLock.lock()
            try {
                val state = handles[handle] ?: throw ToluNoSuchProcessException(handle)
                state.currentState == ToluProgramState.Running
            } finally {
                handleTableLock.unlock()
            }
        }
    }

    override fun closeProcess(handle: ToluVMHandle, force: Boolean): Boolean {

        return if (!force) {
            GlobalScope.launch {
                handleTableLock.lock()
                try {
                    handles[handle]?.finalizeProcess()
                } finally {
                    handleTableLock.unlock()
                }
            }
            true
        } else {
            runBlocking {
                handleTableLock.lock()
                try {
                    handles.remove(handle) != null
                } finally {
                    handleTableLock.unlock()
                }
            }
        }
    }

    override fun close() {
        if (vmCoroutineHandle != null) {
            val vmCoroutineHandle = vmCoroutineHandle!!
            vmCoroutineHandle.cancel()

            // wait until Toluvm is done closing
            runBlocking {
                launch { vmCoroutineHandle.join() }
                delay(1000)
            }

            this.vmCoroutineHandle = null
            loopChannel.close()
        }
    }

    override fun getLoader(): ToluLoader {
        return ToluV1Loader()
    }

    override fun runVM() {
        if (isRunning) {
            throw IllegalStateException("ToluVM is already running")
        }

        isRunning = true

        vmCoroutineHandle = GlobalScope.launch(Dispatchers.Default) {
            vmProcess(this)
        }
    }

    override fun bindExternalFunction(fnName: String, fnImpl: ExtFn) {
        if (isRunning) {
            throw IllegalStateException("TODO: Binding external functions when VM is running is not allowed in this impl. " +
                    "Make sure you bind all needed functions before calling runVM")
        }
        extFns[fnName] = fnImpl
    }

    private suspend fun vmProcess(c: CoroutineScope) {
        val finalizedProcesses : MutableList<ToluVMHandle> = ArrayList()

        while (c.isActive) {
            handleTableLock.lock()

            var numStillAwake = handles.size
            for (processEntry in handles.entries) {
                val process = processEntry.value
                process.stateMutex.lock()
                for (i in 0 until DEFAULT_TIME_SLICE) {
                    if (process.currentState != ToluProgramState.Running) {
                        numStillAwake -= 1

                        break
                    }

                    try {
                        process.execute(c)
                    } catch (e: Exception) {
                        // TODO: do some reporting. exception should be shown in UI somehow
                        LOG.error("Script failed: ${formatException(e)}")

                        process.execDoneCallback?.invoke()
                        process.currentState = ToluProgramState.Inactive
                        break
                    }
                }

                if (process.currentState == ToluProgramState.Finalizing) {
                    finalizedProcesses.add(processEntry.key)
                }

                process.stateMutex.unlock()
            }

            for (process in finalizedProcesses) {
                handles.remove(process)
            }
            finalizedProcesses.clear()

            handleTableLock.unlock()

            if (numStillAwake == 0) {
                // sleep
                LOG.info("No more processes to run for now. Good night.")
                loopChannel.receive()
            }
        }
    }
}