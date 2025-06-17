package com.dd3ok.authservice.domain.exception

sealed class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class UserNotFoundException(email: String) : DomainException("User not found with email: $email")

class DuplicateUserException(email: String) : DomainException("User already exists with email: $email")

class OAuthAccountAlreadyLinkedException(provider: String) : DomainException("OAuth account already linked: $provider")

class InvalidOAuthStateException(message: String) : DomainException(message)
