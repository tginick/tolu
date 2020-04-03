package net.nicholasgwong.tolu.compiler.impl

import net.nicholasgwong.misckt.ext.slf4jLogger
import net.nicholasgwong.tolu.compiler.interfaces.LocalID
import net.nicholasgwong.tolu.compiler.interfaces.Scope

class ScopeV1(val parentScope: ScopeV1?) : Scope {
    companion object {
        private val LOG = slf4jLogger(ScopeV1::class)
    }

    private val elements: MutableMap<String, LocalID> = HashMap()
    private var lastID: LocalID? = null

    override fun newElement(name: String): LocalID {
        // check only this scope (no recursion) for this symbol
        if (elements.containsKey(name)) {
            return elements[name]!!
        }

        // doesn't exist. create (shadowing any higher level decls)
        // first find the offset that should be used by looking at the parent scope and using its highest id
        val offset = parentScope?.getLastID()?.inc() ?: 0

        val newID = elements.size + offset
        LOG.debug("Assigning new id $newID to $name (offset $offset)")

        elements[name] = newID

        lastID = newID
        return newID
    }

    override fun findElement(name: String): LocalID? {
        if (elements.containsKey(name)) {
            return elements[name]!!
        }

        if (parentScope == null) {
            return null
        }

        return parentScope.findElement(name)
    }

    private fun getLastID(): Int {
        return lastID ?: 0
    }
}