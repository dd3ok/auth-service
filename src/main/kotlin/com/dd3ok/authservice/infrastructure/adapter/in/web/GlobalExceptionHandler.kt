package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import com.dd3ok.authservice.domain.exception.DuplicateUserException
import com.dd3ok.authservice.domain.exception.InvalidOAuthStateException
import com.dd3ok.authservice.domain.exception.OAuthAccountAlreadyLinkedException
import com.dd3ok.authservice.domain.exception.UserNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFoundException(ex: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("UserNotFoundException: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse("USER_NOT_FOUND", ex.message ?: "User not found"))
    }

    @ExceptionHandler(DuplicateUserException::class)
    fun handleDuplicateUserException(ex: DuplicateUserException): ResponseEntity<ErrorResponse> {
        logger.warn("DuplicateUserException: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse("DUPLICATE_USER", ex.message ?: "User already exists"))
    }

    @ExceptionHandler(InvalidOAuthStateException::class)
    fun handleInvalidOAuthStateException(ex: InvalidOAuthStateException): ResponseEntity<ErrorResponse> {
        logger.warn("InvalidOAuthStateException: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("INVALID_OAUTH_STATE", ex.message ?: "Invalid OAuth state"))
    }

    @ExceptionHandler(OAuthAccountAlreadyLinkedException::class)
    fun handleOAuthAccountAlreadyLinkedException(ex: OAuthAccountAlreadyLinkedException): ResponseEntity<ErrorResponse> {
        logger.warn("OAuthAccountAlreadyLinkedException: {}", ex.message, ex)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse("OAUTH_ACCOUNT_ALREADY_LINKED", ex.message ?: "OAuth account already linked"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }
            .joinToString(", ")

        logger.warn("Validation error: {}", message, ex)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse("VALIDATION_ERROR", message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred"))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String
)