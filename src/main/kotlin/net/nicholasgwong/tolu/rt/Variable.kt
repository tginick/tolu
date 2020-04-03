package net.nicholasgwong.tolu.rt

enum class VariableType(val ord: Int) {
    INT(0),
    FLOAT(1),
    BOOLEAN(2),
    STRING(3),
    FUNCTIONREF(4),
    UNIT(5),
}

data class FunctionRef(val id: Int)

data class Variable(val type: VariableType, val data: Any) {
    fun typeEq(type: VariableType) = type == this.type

    fun asInt(): Int = data as Int
    fun asFloat(): Float = data as Float
    fun asBoolean(): Boolean = data as Boolean
    fun asString(): String = data as String
    fun asFnRef(): FunctionRef = data as FunctionRef
}