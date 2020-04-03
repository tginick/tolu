package net.nicholasgwong.tolu.compiler.exceptions


class NoSuchLocalException(requestedLocal: String) : RuntimeException("No such local variable defined in scope $requestedLocal")
class InvalidConditionalExpression(realClass: Class<*>) : RuntimeException("Expression does not evaluate to boolean: $realClass")