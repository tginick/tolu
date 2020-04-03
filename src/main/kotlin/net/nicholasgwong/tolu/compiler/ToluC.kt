package net.nicholasgwong.tolu.compiler

import net.nicholasgwong.misckt.AutoBuffer
import net.nicholasgwong.misckt.ext.slf4jLogger
import net.nicholasgwong.tolu.compiler.emit.ToluProgramV1Scaffolding
import net.nicholasgwong.tolu.compiler.gen.ToluLexer
import net.nicholasgwong.tolu.compiler.gen.ToluParser
import net.nicholasgwong.tolu.compiler.impl.CodeGenV1
import net.nicholasgwong.tolu.compiler.nodes.Node
import net.nicholasgwong.tolu.compiler.opt.DefaultOptimizer
import net.nicholasgwong.tolu.compiler.visitors.ProgramVisitor
import net.nicholasgwong.tolu.rt.impl.CompiledToluProgramV1
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.apache.commons.cli.*
import java.io.FileOutputStream

class ToluCCompilationFailure(msg: String) : RuntimeException(msg)

object ToluC {
    val LOG = slf4jLogger(ToluC::class)

    data class ToluOptions(val fileName: String, val printDisasm: Boolean)

    fun compile(stream: CharStream): AutoBuffer {
        // traverse the ast and generate IR
        val root = parseRoot(stream)

        // compile to bytecode
        val c = CodeGenV1()
        root.generate(c)

        // optimize if possible
        val o = DefaultOptimizer()
        c.optimize(o)

        // emit bytecode
        val s = ToluProgramV1Scaffolding()
        c.createScaffoldedProgram(s)

        return s.serializeProgram()
    }

    fun compileFile(fileName: String): AutoBuffer {
        val file = CharStreams.fromFileName(fileName)
        return compile(file)
    }

    fun compileString(script: String): AutoBuffer {
        val str = CharStreams.fromString(script)

        return compile(str)
    }

    private fun printDisasm(compiledBuf: AutoBuffer) {
        val program = CompiledToluProgramV1()
        program.deserializeProgram(compiledBuf)

        val disasm = program.createDisasm()

        System.out.println(disasm)
    }

    private fun parseRoot(stream: CharStream): Node {
        val lexer = ToluLexer(stream)
        val lexerErrorHandler = ToluCErrorListener()
        lexer.removeErrorListeners()
        lexer.addErrorListener(lexerErrorHandler)

        val tokenStream = CommonTokenStream(lexer)
        val parser = ToluParser(tokenStream)
        val parserErrorHandler = ToluCErrorListener()
        parser.removeErrorListeners()
        parser.addErrorListener(parserErrorHandler)

        val visitor = ProgramVisitor()

        val rootContext = parser.program()

        val visited = visitor.visitProgram(rootContext)

        if (lexerErrorHandler.hasErrors() || parserErrorHandler.hasErrors()) {
            throw ToluCCompilationFailure("Errors detected during compilation!\n\nLexer:\n${lexerErrorHandler.getErrors()}\n\nParser:\n${parserErrorHandler.getErrors()}")
        }

        return visited
    }

    private fun parseOptions(options: Options, args: Array<String>): ToluOptions {
        val cmdParser = DefaultParser()

        val cmdLine: CommandLine = cmdParser.parse(options, args)

        val fname: String = cmdLine.getOptionValue('f')!!
        val printDisasm = cmdLine.hasOption('p')

        return ToluOptions(fname, printDisasm)
    }

    private fun prepOptions(): Options {
        val options = Options()
        options.addOption(Option.builder("f")
                .hasArg()
                .argName("FILE")
                .desc("File name to compile")
                .optionalArg(false)
                .required()
                .build())
        options.addOption(Option.builder("p")
                .desc("Print disassembly instead of writing bytecode")
                .build())

        return options
    }

    private fun printHelp(options: Options) {
        val f = HelpFormatter()

        f.printHelp("ToluC", "Compiles Tolu script into bytecode", options, "", true)
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val opts = prepOptions()

        try {
            val parsedOpts = parseOptions(opts, args)

            val file = CharStreams.fromFileName(parsedOpts.fileName)

            val compiledBuf = compile(file)
            compiledBuf.use {
                if (parsedOpts.printDisasm) {
                    printDisasm(it)
                }

                FileOutputStream(parsedOpts.fileName + "c").channel!!.use {
                    it.write(compiledBuf.backingBuffer())
                }
            }
        } catch (e: ParseException) {
            printHelp(opts)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
    }
}
