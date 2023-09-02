package ua.com.radiokot.pc.util

/**
 * Contains different conditions.
 */
object SearchUtil {
    fun isMatchGeneralCondition(query: String, fields: List<String?>): Boolean {
        val unmatchedFieldsParts = fields.fold(mutableSetOf<String>()) { acc, item ->
            if (item != null) {
                acc.addAll(item.split(" "))
            }
            acc
        }

        val unmatchedQueryParts = query.split(" ").toMutableList()
        var unmatchedChanged = true
        while (unmatchedFieldsParts.isNotEmpty()
            && unmatchedQueryParts.isNotEmpty()
            && unmatchedChanged
        ) {
            val unmatchedFieldsPartsIterator = unmatchedFieldsParts.iterator()
            unmatchedChanged = false
            while (unmatchedFieldsPartsIterator.hasNext()) {
                val fieldPart = unmatchedFieldsPartsIterator.next()

                val partsIterator = unmatchedQueryParts.iterator()
                while (partsIterator.hasNext()) {
                    val queryPart = partsIterator.next()

                    if (fieldPart.startsWith(queryPart, true)) {
                        partsIterator.remove()
                        unmatchedFieldsPartsIterator.remove()
                        unmatchedChanged = true
                        break
                    }
                }
            }
        }

        return unmatchedQueryParts.isEmpty()
    }
}
