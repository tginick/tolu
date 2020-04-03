package net.nicholasgwong.tolu.compiler.interfaces

typealias LocalID = Int

interface Scope {
    fun newElement(name: String): LocalID
    fun findElement(name: String): LocalID?

}