package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import java.math.BigInteger
import java.security.SecureRandom

@Controller
class WebViewController {

    @GetMapping("/")
    fun index(): String {
        return "login"
    }

    @GetMapping("/login")
    fun login(session: HttpSession, model: Model): String {
        val state = BigInteger(130, SecureRandom()).toString(32)
        session.setAttribute("oauth_state", state)
        model.addAttribute("state", state)
        return "login"
    }

    @GetMapping("/dashboard")
    fun dashboard(
        @RequestParam(required = false) accessToken: String?,
        model: Model
    ): String {
        model.addAttribute("accessToken", accessToken)
        return "dashboard"
    }

    @GetMapping("/auth/callback/{provider}")
    fun oauthCallback(
        @PathVariable provider: String,
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        session: HttpSession,
        model: Model
    ): String {
        val sessionState = session.getAttribute("oauth_state") as? String

        // 네이버의 경우, state 값 검증
        if (provider == "naver") {
            if (sessionState == null || sessionState != state) {
                model.addAttribute("errorTitle", "보안 오류")
                model.addAttribute("errorMessage", "State 값이 일치하지 않습니다. CSRF 공격일 수 있습니다.")
                return "error"
            }
        }

        session.removeAttribute("oauth_state") // 검증 후 세션에서 제거

        model.addAttribute("code", code)
        model.addAttribute("state", state)
        model.addAttribute("provider", provider)
        return "callback"
    }
}
