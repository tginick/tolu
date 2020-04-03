package net.nicholasgwong.tolu.compiler

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

class ToluCErrorListener : BaseErrorListener() {
    private val sb: StringBuilder = StringBuilder()
    private var hasErrors: Boolean = false

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        sb.append("line $line:$charPositionInLine $msg\n")

        hasErrors = true
    }

    fun hasErrors(): Boolean {
        return hasErrors
    }

    fun getErrors(): String {
        return sb.toString()
    }
}