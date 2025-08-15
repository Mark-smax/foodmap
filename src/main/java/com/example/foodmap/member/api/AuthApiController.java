package com.example.foodmap.member.api;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberService;

import jakarta.servlet.http.HttpSession;

/**
 * Lightweight JSON auth endpoints for the Vue frontend.
 * Creates/uses HttpSession attributes: loginMemberId, loginMemberName, loginMemberRoles.
 *
 * Base path: /api/auth
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final MemberService memberService;

    public AuthApiController(MemberService memberService) {
        this.memberService = memberService;
    }

    // Accept JSON body: {"email":"...", "password":"..."}
    public static class LoginRequest {
        public String email;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body, HttpSession session) {
        if (body == null || body.email == null || body.password == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("ok", false, "error", "BAD_REQUEST"));
        }

        Member result = memberService.checkLogin(body.email, body.password);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("ok", false, "error", "BAD_CREDENTIALS"));
        }

        // Put into session for server-rendered pages and any later checks
        session.setAttribute("loginMemberId", result.getMemberId());
        session.setAttribute("loginMemberName", result.getMemberName());
        session.setAttribute("loginMemberRoles", result.getMemberRole());

        Map<String, Object> resp = new HashMap<>();
        resp.put("ok", true);
        resp.put("memberId", result.getMemberId());
        resp.put("name", result.getMemberName());
        resp.put("roles", result.getMemberRole());
        resp.put("status", result.getMemberStatus());
        return ResponseEntity.ok(resp);
    }

    // Optional: form-POST fallback ?email=...&password=...
    @PostMapping(path = "/login", consumes = {"application/x-www-form-urlencoded"})
    public ResponseEntity<?> loginForm(@RequestParam("email") String email,
                                       @RequestParam("password") String password,
                                       HttpSession session) {
        LoginRequest body = new LoginRequest();
        body.email = email;
        body.password = password;
        return login(body, session);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Object id = session.getAttribute("loginMemberId");
        Object name = session.getAttribute("loginMemberName");
        Object roles = session.getAttribute("loginMemberRoles");
        boolean loggedIn = id != null;
        Map<String, Object> m = new HashMap<>();
        m.put("loggedIn", loggedIn);
        if (loggedIn) {
            m.put("memberId", id);
            m.put("name", name);
            m.put("roles", roles);
        }
        return m;
    }
}
