package com.example.foodmap.foodmap.web;

import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.foodmap.foodmap.domain.NotificationService;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/restaurant")
public class MerchantRestaurantController {

    private final RestaurantService restaurantService;
    private final NotificationService notificationService;

    public MerchantRestaurantController(RestaurantService restaurantService,
                                        NotificationService notificationService) {
        this.restaurantService = restaurantService;
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

    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        if (!hasRole(session, "MERCHANT")) {
            return "redirect:/member/login";
        }
        model.addAttribute("restaurant", new Restaurant());
        return "merchant/restaurant-create"; // Step 5 會提供模板
    }

    @PostMapping("/create")
    public String submit(@ModelAttribute Restaurant form, HttpSession session) {
        if (!hasRole(session, "MERCHANT")) {
            return "redirect:/member/login";
        }
        Long merchantId = currentMemberId(session);
        restaurantService.submitByMerchant(form, merchantId);

        // 通知所有管理員有新上架申請
        notificationService.notifyAdmins(
            "新上架申請",
            "商家提交了餐廳：「" + form.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );

        return "redirect:/merchant/restaurant/mine";
    }

    @GetMapping("/mine")
    public String myRestaurants(@RequestParam(defaultValue = "0") int page,
                                HttpSession session,
                                Model model) {
        if (!hasRole(session, "MERCHANT")) {
            return "redirect:/member/login";
        }
        Long merchantId = currentMemberId(session);
        var result = restaurantService.searchMine(merchantId, PageRequest.of(page, 10));
        model.addAttribute("page", result);
        return "merchant/restaurant-mine"; // Step 5 會提供模板
    }
}
