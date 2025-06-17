package com.dd3ok.authservice.infrastructure.adapter.`in`.web

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class WebViewController {
    
    @GetMapping("/")
    fun index(): String {
        return "index"
    }
    
    @GetMapping("/login")
    fun login(): String {
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
        @RequestParam code: String,
        @RequestParam(required = false) state: String?,
        model: Model
    ): String {
        model.addAttribute("code", code)
        model.addAttribute("state", state)
        return "callback"
    }
}
