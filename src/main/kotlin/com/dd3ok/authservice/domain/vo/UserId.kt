package com.dd3ok.authservice.domain.vo

@JvmInline
value class UserId(val value: Long) {
    init {
        require(value > 0) { "UserId must be positive: $value" }
    }

    companion object {
        fun newUser(): UserId {
            return UserId(0L)
        }

        fun of(id: Long): UserId {
            return UserId(id)
        }
    }
}