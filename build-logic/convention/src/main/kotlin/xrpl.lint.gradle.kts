plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

ktlint {
    android.set(false)
    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "120",
            "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
            "ij_kotlin_allow_trailing_comma" to "true",
            "ktlint_standard_function-naming" to "disabled",
        ),
    )
}
