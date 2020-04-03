package net.nicholasgwong.tolu.rt


enum class Mnemonic(val op: Byte) {
    NOP(0),

    PUSHI(1),
    PUSHF(2),
    PUSHS(3),
    PUSHB(4),

    ADD(5),
    SUB(6),
    MUL(7),
    DIV(8),

    NOT(9),
    AND(10),
    OR(11),
    GT(12),
    LT(13),
    GTE(14),
    LTE(15),
    EQ(16),

    JE(17),
    JNE(18),
    J(19),
    EXT(20), // for calling built in functions
    CALL(21), // for calling functions

    PUSHFNREF(22),

    // retrieve/set local vars
    GETL(23),
    SETL(24),

    RETN(25),
    PUSHUNIT(26),

    INVALID_OP(Byte.MAX_VALUE);
}

fun createMnemonicReverseLookup(): Map<Byte, Mnemonic> {
    val revLookup = HashMap<Byte, Mnemonic>()
    for (m in Mnemonic.values()) {
        revLookup[m.op] = m
    }

    return revLookup
}

val OP_REVERSE_LOOKUP = createMnemonicReverseLookup()