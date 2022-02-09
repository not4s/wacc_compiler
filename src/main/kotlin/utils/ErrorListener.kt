package utils

import java.util.Queue
import java.util.LinkedList

class ErrorListener {
    private val errorQueue: Queue<ErrorMessage> = LinkedList<ErrorMessage>()
    fun pushError(exception: ErrorMessage) = errorQueue.add(exception)
    fun popError() = errorQueue.poll()
    fun getIterator() = errorQueue.iterator()
    fun isEmpty() = errorQueue.isEmpty()
}