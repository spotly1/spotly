package com.example.spotly.database

/** Caché local acotada, en memoria. No representa persistencia offline con Room. */
class AddressCache(private val capacity: Int = 100) {
    init { require(capacity > 0) }
    private val entries = LinkedHashMap<String, String>(capacity, 0.75f, true)

    @Synchronized fun get(key: String): String? = entries[key]

    @Synchronized fun put(key: String, value: String) {
        entries[key] = value
        if (entries.size > capacity) {
            val iterator = entries.entries.iterator()
            iterator.next()
            iterator.remove()
        }
    }
}
