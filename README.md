# tolu
A scripting language and VM built on the JVM

## Name
In Portuguese, Lua means "moon," but in Polynesian languages, Lua means "two." Tolu simply means "three" in those languages.
I was playing around with Lua before I had the weird urge to see how hard it'd be to create my own script language (spoiler alert: it's quite difficult)

## About
Tolu is a stack VM that was originally designed to be a built in script language for a side game project way back.
It uses ANTLR4 to generate lexer and parser logic. Once the script file has been transformed to an AST by the parser, it
is then translated into Tolu bytecode that is executable on the VM.

## How do I use it?
TBD sorry!

## What doesn't work yet?

* String Concatenation
* Tables
* Importing other scripts

## What does it look like?

```
npc:

fn init() {

}

fn main() {
    x = 4;

    while (x < 100) {
        x = x + 1;
    }

    ~dbg("Hello", " World");
    ~dbg("Float", ~f(1));
    return x;
}
```
compiles to the following disassembly
```
String Table:
  0: "Hello"
  1: " World"
  2: dbg
  3: "Float"
  4: f

Function ID Table:
  0: init
  1: main

Implementation:
  0: Local
  1: Local
    0: PUSHI  4
    1: SETL  1
    2: GETL  1
    3: PUSHI  100
    4: LT  0
    5: JNE  11
    6: GETL  1
    7: PUSHI  1
    8: ADD  0
    9: SETL  1
    10: J  2
    11: PUSHS  0
    12: PUSHS  1
    13: PUSHS  2
    14: EXT  2
    15: PUSHS  3
    16: PUSHI  1
    17: PUSHS  4
    18: EXT  1
    19: PUSHS  2
    20: EXT  2
    21: GETL  1
    22: RETN  0
```
Some instructions don't have operands (e.g. ADD or RETN) but since all instructions are encoded as 64-bit numbers right now for simplicity, those just show up as 0.
