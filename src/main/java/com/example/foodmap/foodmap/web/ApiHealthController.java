// src/main/java/.../web/ApiHealthController.java
package com.example.foodmap.foodmap.web;

import java.util.Map;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ApiHealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("ok", true, "service", "foodmap", "ts", System.currentTimeMillis());
    }

    @GetMapping("/whoami")
    public Map<String, Object> whoami(HttpSession session) {
        return Map.of(
            "loginMemberId", session.getAttribute("loginMemberId"),
            "loginMemberName", session.getAttribute("loginMemberName"),
            "loginMemberRoles", session.getAttribute("loginMemberRoles")
        );
    }
}
