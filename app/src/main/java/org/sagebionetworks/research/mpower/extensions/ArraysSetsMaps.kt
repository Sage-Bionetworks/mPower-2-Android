package org.sagebionetworks.research.mpower.extensions

import java.util.*

/**
 * File to hold extensions for arrays, sets, and maps.
 * TODO: mdephillips 8/8/18 Unit test these extensions
 */

/**
 * @return a random element in the array if any exist
 */
fun <T> Array<out T>?.randomElement(): T? {
    if (this == null) {
        return null
    }
    if (size <= 0) {
        return null
    }
    return get(Random().nextInt(size))
}

/**
 * @return a [HashSet] of all elements.
 */
fun <T> Array<out T>?.toHashSet(): HashSet<T>? {
    if (this == null) {
        return null
    }
    return toCollection(HashSet<T>(size))
}