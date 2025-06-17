package com.dd3ok.authservice.domain.vo

@JvmInline
value class Email(val value: String) {
    init {
        require(isValid(value)) { "Invalid email format: $value" }
    }

    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()

        fun isValid(email: String): Boolean {
            return email.isNotBlank() && EMAIL_REGEX.matches(email)
        }
    }
}