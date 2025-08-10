package com.example.foodmap.foodmap.web;

import java.time.OffsetDateTime;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.ModerationStatus;
import com.example.foodmap.foodmap.domain.NotificationService;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/moderation")
public class AdminModerationController {

    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    public AdminModerationController(RestaurantRepository restaurantRepository,
                                     NotificationService notificationService) {
        this.restaurantRepository = restaurantRepository;
        this.notificationService = notificationService;
    }

    private boolean hasRole(HttpSession session, String role) {
        Object roles = session.getAttribute("loginMemberRoles");
        return roles != null && roles.toString().contains(role);
    }

    private Long currentMemberId(HttpSession session) {
        Object obj = session.getAttribute("loginMemberId");
        if (obj == null) return null;
        if (obj instanceof Long l) return l;
        if (obj instanceof Integer i) return i.longValue();
        return Long.valueOf(obj.toString());
    }

    @GetMapping("/restaurants")
    public String list(@RequestParam(defaultValue = "PENDING") ModerationStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       HttpSession session,
                       Model model) {
        if (!hasRole(session, "ADMIN")) {
            return "redirect:/member/login";
        }
        var pending = restaurantRepository.findByStatus(status, PageRequest.of(page, 20));
        model.addAttribute("page", pending);
        model.addAttribute("status", status);
        return "admin/moderation-list"; // Step 5 會提供模板
    }

    @PostMapping("/restaurants/{id}/approve")
    public String approve(@PathVariable Long id, HttpSession session) {
        if (!hasRole(session, "ADMIN")) {
            return "redirect:/member/login";
        }
        var r = restaurantRepository.findById(id).orElseThrow();
        r.setStatus(ModerationStatus.APPROVED);
        r.setReviewedBy(currentMemberId(session));
        r.setReviewedAt(OffsetDateTime.now());
        r.setRejectReason(null);
        restaurantRepository.save(r);

        if (r.getSubmittedBy() != null) {
            notificationService.notifyMember(
                r.getSubmittedBy(),
                "上架申請通過",
                "你的餐廳「" + r.getName() + "」已通過審核並公開上架。",
                "/restaurant/" + r.getId()
            );
        }
        return "redirect:/admin/moderation/restaurants";
    }

    @PostMapping("/restaurants/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam String reason,
                         HttpSession session) {
        if (!hasRole(session, "ADMIN")) {
            return "redirect:/member/login";
        }
        var r = restaurantRepository.findById(id).orElseThrow();
        r.setStatus(ModerationStatus.REJECTED);
        r.setReviewedBy(currentMemberId(session));
        r.setReviewedAt(OffsetDateTime.now());
        r.setRejectReason(reason);
        restaurantRepository.save(r);

        if (r.getSubmittedBy() != null) {
            notificationService.notifyMember(
                r.getSubmittedBy(),
                "上架申請未通過",
                "你的餐廳「" + r.getName() + "」未通過審核。原因：" + reason,
                "/merchant/restaurant/mine"
            );
        }
        return "redirect:/admin/moderation/restaurants";
    }
}
