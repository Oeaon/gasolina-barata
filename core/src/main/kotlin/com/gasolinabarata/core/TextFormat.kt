package com.gasolinabarata.core

private val LOWERCASE_WORDS = setOf("de", "del", "la", "las", "el", "los", "y", "a", "en")

/** "AVENIDA DE LA CONSTITUCION, 26" → "Avenida de la Constitucion, 26". El Ministerio lo da todo en mayúsculas. */
fun prettyCase(text: String): String =
    text.lowercase()
        .split(' ')
        .mapIndexed { index, word ->
            if (index > 0 && word in LOWERCASE_WORDS) word else word.replaceFirstChar { it.titlecase() }
        }
        .joinToString(" ")
