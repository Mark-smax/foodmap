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

    // ====== 新增頁 ======
    @GetMapping("/create")
    public String createForm(HttpSession session, Model model) {
        if (!hasRole(session, "MERCHANT")) {
            return "redirect:/member/login";
        }
        model.addAttribute("restaurant", new Restaurant());
        // 新增頁的營業時間由前端提供 7 筆預設行；這裡不用塞資料
        return "merchant/restaurant-create";
    }

    // ====== 新增（含營業時間一起存） ======
    // 前端表單請用以下欄位名稱，出現多筆時用多個 input 形成平行陣列：
    // dayOfWeek[], openTime[], closeTime[], closedAllDay[]
    // （checkbox 請搭配同名 hidden 以確保長度對齊，詳見說明）
    @PostMapping("/create")
    public String submit(@ModelAttribute Restaurant form,
                         @RequestParam(value = "type", required = false) List<String> types,
                         @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                         // ↓↓↓ 營業時間（可省略；若沒填就不建資料）
                         @RequestParam(value = "dayOfWeek",   required = false) List<String> dayOfWeek,
                         @RequestParam(value = "openTime",    required = false) List<String> openTime,
                         @RequestParam(value = "closeTime",   required = false) List<String> closeTime,
                         @RequestParam(value = "closedAllDay",required = false) List<String> closedAllDay,
                         HttpSession session,
                         RedirectAttributes ra) {
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

        // 1) 走你原本的商家送審流程（會填 submittedBy、狀態 PENDING 等）
        var saved = restaurantService.submitByMerchant(form, merchantId);

        // 2) 圖片（最多 5 張）
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

        // 3) 營業時間（若表單有帶就一起建立；否則略過）
        var hours = buildHoursFromRequest(saved.getId(), dayOfWeek, openTime, closeTime, closedAllDay);
        if (!hours.isEmpty()) {
            restaurantService.replaceWeeklyHours(saved.getId(), hours);
        }

        // 通知管理員
        notificationService.notifyAdmins(
            "新上架申請",
            "商家提交了餐廳：「" + saved.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );

        ra.addFlashAttribute("msg", "餐廳已送審，營業時間已保存");
        return "redirect:/merchant/restaurant/mine";
    }

    // ====== 我的餐廳清單 ======
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

    // ====== 編輯頁（預載現有每週時段） ======
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        var r = restaurantService.findById(id);
        if (r.getSubmittedBy() == null || !r.getSubmittedBy().equals(merchantId)) {
            return "redirect:/merchant/restaurant/mine";
        }
        model.addAttribute("restaurant", r);

        // ✅ 預載現有每週時段（給前端帶入）
        // 需要 RestaurantService 提供 getWeeklyHourEntities(id)
        // 若你還沒加，告訴我，我補一版 repository 呼叫
        List<RestaurantHour> weekly = restaurantService.getWeeklyHourEntities(id);
        model.addAttribute("weeklyHoursEntities", weekly);

        return "merchant/restaurant-edit";
    }

    // ====== 編輯送出（含營業時間一起覆蓋） ======
    @PostMapping("/edit/{id}")
    public String updateAndResubmit(@PathVariable Long id,
                                    @ModelAttribute Restaurant form,
                                    // ↓↓↓ 營業時間（同 create）
                                    @RequestParam(value = "dayOfWeek",   required = false) List<String> dayOfWeek,
                                    @RequestParam(value = "openTime",    required = false) List<String> openTime,
                                    @RequestParam(value = "closeTime",   required = false) List<String> closeTime,
                                    @RequestParam(value = "closedAllDay",required = false) List<String> closedAllDay,
                                    HttpSession session,
                                    RedirectAttributes ra) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        // 1) 先更新餐廳內容並重新送審（保留你原本流程）
        restaurantService.updateAndResubmit(id, form, merchantId);

        // 2) 覆蓋營業時間（若表單有帶）
        var hours = buildHoursFromRequest(id, dayOfWeek, openTime, closeTime, closedAllDay);
        restaurantService.replaceWeeklyHours(id, hours); // 空清單表示清掉所有班表

        // 通知管理員
        notificationService.notifyAdmins(
            "餐廳重新送審",
            "商家重新送審：「" + form.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );
        ra.addFlashAttribute("msg", "已送出修改並重新送審（含營業時間）");
        return "redirect:/merchant/restaurant/edit/" + id;
    }

    // ====== 只改時段（保留：給舊頁或 API 用） ======
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

    // ====== 特例（單日店休或特殊時段）新增/更新（保留） ======
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

    // ====== 將表單陣列參數轉 Entity（供 create/edit 共用） ======
    private List<RestaurantHour> buildHoursFromRequest(
            Long restaurantId,
            List<String> dayOfWeek,
            List<String> openTime,
            List<String> closeTime,
            List<String> closedAllDay) {

        List<RestaurantHour> rows = new ArrayList<>();
        if (dayOfWeek == null || dayOfWeek.isEmpty()) return rows;

        int n = dayOfWeek.size();
        for (int i = 0; i < n; i++) {
            String dowStr   = dayOfWeek.get(i);
            String openStr  = (openTime  != null && openTime.size()  > i) ? openTime.get(i)  : null;
            String closeStr = (closeTime != null && closeTime.size() > i) ? closeTime.get(i) : null;

            boolean closed = false;
            // 若 checkbox 有做「hidden 同名欄位 + checkbox」就能保證長度對齊（建議）
            // 否則這裡 fallback：若 closedAllDay 長度等於 dayOfWeek，直接看同 index 的值是否 "true" 或 "on"
            if (closedAllDay != null) {
                if (closedAllDay.size() == n) {
                    String val = closedAllDay.get(i);
                    closed = "true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val) || "1".equals(val);
                } else {
                    // 最保守作法：只要 closedAllDay 有任何值，且 open/close 皆為空，就當公休
                    closed = (openStr == null || openStr.isBlank()) && (closeStr == null || closeStr.isBlank());
                }
            }

            RestaurantHour h = new RestaurantHour();
            h.setRestaurantId(restaurantId);
            h.setDayOfWeek(DayOfWeek.valueOf(dowStr));
            h.setClosedAllDay(closed);

            if (closed) {
                h.setOpenTime(null);
                h.setCloseTime(null);
            } else {
                h.setOpenTime((openStr  == null || openStr.isBlank())  ? null : LocalTime.parse(openStr));
                h.setCloseTime((closeStr == null || closeStr.isBlank()) ? null : LocalTime.parse(closeStr));
            }
            rows.add(h);
        }
        return rows;
    }
}
