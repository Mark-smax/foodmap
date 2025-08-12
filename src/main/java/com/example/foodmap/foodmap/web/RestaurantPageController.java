package com.example.foodmap.foodmap.web;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.dto.PhotoDto;
import com.example.foodmap.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.foodmap.dto.ReviewDto;

import jakarta.servlet.http.HttpSession;

@Controller
public class RestaurantPageController {

    private final RestaurantService restaurantService;

    public RestaurantPageController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/restaurant-detail")
    public String showDetailPage(
            @RequestParam("id") Long id,
            @RequestParam(value = "memberId", required = false) String memberIdStr,
            HttpSession session,
            Model model) {

        // 1) 取得 loginMemberId（querystring > session）
        Long loginMemberId = null;
        if (memberIdStr != null && !memberIdStr.isBlank()) {
            try { loginMemberId = Long.parseLong(memberIdStr); } catch (NumberFormatException ignore) {}
        }
        if (loginMemberId == null) {
            Object raw = session.getAttribute("loginMemberId");
            if (raw instanceof Long) loginMemberId = (Long) raw;
            else if (raw instanceof Integer) loginMemberId = ((Integer) raw).longValue();
            else if (raw instanceof String) { try { loginMemberId = Long.parseLong((String) raw); } catch (Exception ignore) {} }
        }

        // 2) 取 DTO（已含：restaurant、photos(URL)、reviews(ReviewDto)、weeklyHours、openNow...）
        RestaurantDetailsDTO dto = restaurantService.getRestaurantDetails(id, loginMemberId);
        if (dto == null || dto.getRestaurant() == null) {
            model.addAttribute("message", "找不到餐廳資料");
            return "error/404";
        }

        // 3) 讀取評論
        List<ReviewDto> reviews = dto.getReviews() != null ? dto.getReviews() : List.of();

        // 4) 是否為管理員（給模板使用）
        boolean isAdmin = false;
        Object roles = session.getAttribute("loginMemberRoles");
        if (roles != null) {
            String s = String.valueOf(roles);
            isAdmin = s.contains("ADMIN") || s.contains("管理員");
        }

        // 5) 將 weeklyHours 轉為模板用固定順序列（週一→週日）
        List<DayRow> weeklyHoursRows = buildWeeklyRows(dto.getWeeklyHours());

        // 6) 丟資料給 View
        model.addAttribute("restaurant", dto.getRestaurant());

        // 新：照片 URL（模板：<img th:src="${p.url}">）
        List<PhotoDto> photos = dto.getPhotos() != null ? dto.getPhotos() : List.of();
        model.addAttribute("photos", photos);

        // ✅ 刪除舊相容欄位：photoBase64List（Step 3 已移除）
        // model.addAttribute("photoBase64List", dto.getPhotoBase64List());

        model.addAttribute("reviews", reviews);
        model.addAttribute("favorite", dto.isFavorite());
        model.addAttribute("loginMemberId", loginMemberId);
        model.addAttribute("uploaderNickname", dto.getUploaderNickname());
        model.addAttribute("isAdmin", isAdmin);

        // ✅ 營業資訊
        model.addAttribute("weeklyHoursRows", weeklyHoursRows);
        model.addAttribute("openNow", dto.getOpenNow());
        model.addAttribute("todayRange", dto.getTodayRange());
        model.addAttribute("todayStatusText", dto.getTodayStatusText());
        model.addAttribute("todayLabel", dto.getTodayLabel());

        return "restaurant-detail";
    }

    // 把 Map<DayOfWeek, List<String>> 轉成固定順序的列資料（週一→週日）
    private List<DayRow> buildWeeklyRows(Map<DayOfWeek, List<String>> weekly) {
        List<DayRow> rows = new ArrayList<>();
        DayOfWeek[] order = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };
        String[] labels = {"週一","週二","週三","週四","週五","週六","週日"};

        for (int i = 0; i < order.length; i++) {
            DayOfWeek d = order[i];
            List<String> ranges = (weekly != null && weekly.get(d) != null) ? weekly.get(d) : List.of();
            String text = ranges.isEmpty() ? "公休" : String.join(" / ", ranges);
            rows.add(new DayRow(labels[i], text));
        }
        return rows;
    }

    // 提供模板使用的簡單資料列
    public static class DayRow {
        private final String label;   // 週幾（中文）
        private final String ranges;  // 時段字串或「公休」

        public DayRow(String label, String ranges) {
            this.label = label;
            this.ranges = ranges;
        }
        public String getLabel() { return label; }
        public String getRanges() { return ranges; }
    }

    // 除錯端點（可留著）
    @GetMapping("/check-session")
    public String checkSession(HttpSession session) {
        Enumeration<String> attrs = session.getAttributeNames();
        System.out.println("=== Session Attributes ===");
        while (attrs.hasMoreElements()) {
            String name = attrs.nextElement();
            Object value = session.getAttribute(name);
            System.out.println(name + " = " + value);
        }
        System.out.println("==========================");
        return "redirect:/";
    }
}
