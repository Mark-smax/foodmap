package com.example.foodmap.foodmap.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.foodmap.foodmap.domain.NotificationService;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantPhoto;
import com.example.foodmap.foodmap.domain.RestaurantPhotoRepository;
import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.domain.RestaurantHour;
import com.example.foodmap.foodmap.domain.RestaurantSpecialHour;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/merchant/restaurant")
public class MerchantRestaurantController {

    private final RestaurantService restaurantService;
    private final NotificationService notificationService;
    private final RestaurantPhotoRepository restaurantPhotoRepository;

    public MerchantRestaurantController(RestaurantService restaurantService,
                                        NotificationService notificationService,
                                        RestaurantPhotoRepository restaurantPhotoRepository) {
        this.restaurantService = restaurantService;
        this.notificationService = notificationService;
        this.restaurantPhotoRepository = restaurantPhotoRepository;
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
        return "merchant/restaurant-create";
    }

    // ✅ 可收多選分類與多張照片
    @PostMapping("/create")
    public String submit(@ModelAttribute Restaurant form,
                         @RequestParam(value = "type", required = false) List<String> types,
                         @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                         HttpSession session) {
        if (!hasRole(session, "MERCHANT")) {
            return "redirect:/member/login";
        }
        Long merchantId = currentMemberId(session);

        if (types != null && !types.isEmpty()) {
            form.setType(String.join(",", types));
        }
        if (form.getKeywords() == null) {
            form.setKeywords("");
        }

        var saved = restaurantService.submitByMerchant(form, merchantId);

        if (photos != null && photos.length > 0) {
            int count = 0;
            for (MultipartFile f : photos) {
                if (f == null || f.isEmpty()) continue;
                try {
                    RestaurantPhoto p = new RestaurantPhoto();
                    p.setRestaurantId(saved.getId());
                    p.setImage(f.getBytes());
                    restaurantPhotoRepository.save(p);
                    count++;
                    if (count >= 5) break;
                } catch (Exception ignore) { }
            }
        }

        notificationService.notifyAdmins(
            "新上架申請",
            "商家提交了餐廳：「" + saved.getName() + "」等待審核",
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
        return "merchant/restaurant-mine";
    }

    // 顯示編輯頁（僅限提交者 & 商家角色）
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        var r = restaurantService.findById(id);
        if (r.getSubmittedBy() == null || !r.getSubmittedBy().equals(merchantId)) {
            return "redirect:/merchant/restaurant/mine";
        }
        model.addAttribute("restaurant", r);
        return "merchant/restaurant-edit";
    }

    // 編輯並重新送審（通常用在 REJECTED 狀態；也可給 APPROVED 走修改再審）
    @PostMapping("/edit/{id}")
    public String updateAndResubmit(@PathVariable Long id,
                                    @ModelAttribute Restaurant form,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        restaurantService.updateAndResubmit(id, form, merchantId);
        notificationService.notifyAdmins(
            "餐廳重新送審",
            "商家重新送審：「" + form.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );
        ra.addFlashAttribute("msg", "已送出修改並重新送審");
        return "redirect:/merchant/restaurant/edit/" + id;
    }

    // 不改內容，直接重新送審（提供快捷鍵用）
    @PostMapping("/resubmit/{id}")
    public String resubmit(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        var r = restaurantService.resubmitWithoutChange(id, merchantId);
        notificationService.notifyAdmins(
            "餐廳重新送審",
            "商家重新送審：「" + r.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );
        ra.addFlashAttribute("msg", "已重新送審");
        return "redirect:/merchant/restaurant/edit/" + id;
    }

    // ========= ★ 新增：每週營業時間（整表覆蓋） =========
    @PostMapping("/{id}/hours")
    public String replaceWeeklyHours(@PathVariable Long id,
                                     @RequestParam(name = "dow", required = false) List<String> dows,
                                     @RequestParam(name = "open", required = false) List<String> opens,
                                     @RequestParam(name = "close", required = false) List<String> closes,
                                     @RequestParam(name = "closed", required = false) List<String> closedFlags,
                                     HttpSession session,
                                     RedirectAttributes ra) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        // 權限：只能改自己的餐廳
        var r = restaurantService.findById(id);
        if (r.getSubmittedBy() == null || !r.getSubmittedBy().equals(merchantId)) {
            ra.addFlashAttribute("error", "無權限修改此餐廳的營業時間");
            return "redirect:/merchant/restaurant/mine";
        }

        List<RestaurantHour> rows = new ArrayList<>();
        if (dows != null) {
            for (int i = 0; i < dows.size(); i++) {
                String dowStr = dows.get(i);
                String openStr = (opens != null && opens.size() > i) ? opens.get(i) : "";
                String closeStr = (closes != null && closes.size() > i) ? closes.get(i) : "";
                boolean closedAllDay = (closedFlags != null && closedFlags.contains(String.valueOf(i)));

                RestaurantHour h = new RestaurantHour();
                h.setRestaurantId(id);
                h.setDayOfWeek(DayOfWeek.valueOf(dowStr));

                if (closedAllDay) {
                    h.setClosedAllDay(true);
                    h.setOpenTime(null);
                    h.setCloseTime(null);
                } else {
                    h.setClosedAllDay(false);
                    if (openStr != null && !openStr.isBlank()) {
                        h.setOpenTime(LocalTime.parse(openStr));
                    }
                    if (closeStr != null && !closeStr.isBlank()) {
                        h.setCloseTime(LocalTime.parse(closeStr));
                    }
                }
                rows.add(h);
            }
        }

        restaurantService.replaceWeeklyHours(id, rows);
        ra.addFlashAttribute("msg", "已更新每週營業時間");
        return "redirect:/merchant/restaurant/edit/" + id;
    }

    // ========= ★ 新增：特例（單日店休或特殊時段）新增/更新 =========
    @PostMapping("/{id}/special-hour")
    public String upsertSpecialHour(@PathVariable Long id,
                                    @RequestParam("date") String dateStr,
                                    @RequestParam(name = "closedAllDay", required = false) String closedFlag,
                                    @RequestParam(name = "open", required = false) String openStr,
                                    @RequestParam(name = "close", required = false) String closeStr,
                                    @RequestParam(name = "note", required = false) String note,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        var r = restaurantService.findById(id);
        if (r.getSubmittedBy() == null || !r.getSubmittedBy().equals(merchantId)) {
            ra.addFlashAttribute("error", "無權限修改此餐廳的特例營業時間");
            return "redirect:/merchant/restaurant/mine";
        }

        RestaurantSpecialHour s = new RestaurantSpecialHour();
        s.setRestaurantId(id);
        s.setSpecificDate(LocalDate.parse(dateStr));
        boolean closedAllDay = (closedFlag != null);
        s.setClosedAllDay(closedAllDay);

        if (closedAllDay) {
            s.setOpenTime(null);
            s.setCloseTime(null);
        } else {
            s.setOpenTime((openStr == null || openStr.isBlank()) ? null : LocalTime.parse(openStr));
            s.setCloseTime((closeStr == null || closeStr.isBlank()) ? null : LocalTime.parse(closeStr));
        }
        s.setNote((note == null) ? "" : note.trim());

        restaurantService.upsertSpecialHour(s);
        ra.addFlashAttribute("msg", "已更新特例營業時間");
        return "redirect:/merchant/restaurant/edit/" + id;
    }
}
