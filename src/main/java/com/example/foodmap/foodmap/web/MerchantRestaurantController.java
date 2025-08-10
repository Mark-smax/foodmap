package com.example.foodmap.foodmap.web;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.foodmap.foodmap.domain.NotificationService;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantPhoto;
import com.example.foodmap.foodmap.domain.RestaurantPhotoRepository;
import com.example.foodmap.foodmap.domain.RestaurantService;

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

    // ✅ 改良版：可收多選分類與多張照片
    @PostMapping("/create")
    public String submit(@ModelAttribute Restaurant form,
                         @RequestParam(value = "type", required = false) List<String> types,
                         @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                         HttpSession session) {
        if (!hasRole(session, "MERCHANT")) {
            return "redirect:/member/login";
        }
        Long merchantId = currentMemberId(session);

        // 多選分類 -> 以逗號合併存進 Restaurant.type
        if (types != null && !types.isEmpty()) {
            form.setType(String.join(",", types));
        }

        // 關鍵字簡單必填防守（也可再加進階表單驗證）
        if (form.getKeywords() == null) {
            form.setKeywords("");
        }

        // 送審（狀態設為 PENDING、記錄提交者）
        var saved = restaurantService.submitByMerchant(form, merchantId);

        // 儲存照片（最多 5 張；忽略空檔）
        if (photos != null && photos.length > 0) {
            int count = 0;
            for (MultipartFile f : photos) {
                if (f == null || f.isEmpty()) continue;
                try {
                    RestaurantPhoto p = new RestaurantPhoto();
                    p.setRestaurantId(saved.getId());
                    p.setImage(f.getBytes());
                    // 若有 contentType 欄位可加：p.setContentType(f.getContentType());
                    restaurantPhotoRepository.save(p);
                    count++;
                    if (count >= 5) break;
                } catch (Exception ignore) {
                    // 單張失敗就略過，不影響整體流程
                }
            }
        }

        // 通知所有管理員：有新上架申請
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

        // 只允許看自己的
        var r = restaurantService.findById(id);
        if (r.getSubmittedBy() == null || !r.getSubmittedBy().equals(merchantId)) {
            return "redirect:/merchant/restaurant/mine";
        }
        model.addAttribute("restaurant", r);
        return "merchant/restaurant-edit";
    }

 // 編輯並重新送審（通常用在 REJECTED 或想更新上架內容）
    @PostMapping("/edit/{id}")
    public String updateAndResubmit(@PathVariable Long id,
                                    @ModelAttribute Restaurant form,
                                    @RequestParam(value = "type", required = false) List<String> types,
                                    @RequestParam(value = "photos", required = false) MultipartFile[] photos,
                                    HttpSession session) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        // 多選分類 -> 以逗號合併存進 Restaurant.type（和新增一致）
        if (types != null && !types.isEmpty()) {
            form.setType(String.join(",", types));
        }

        // 關鍵字防守
        if (form.getKeywords() == null) {
            form.setKeywords("");
        }

        // 先更新餐廳本身並轉為待審
        var updated = restaurantService.updateAndResubmit(id, form, merchantId);

        // 若有上傳新照片，最多存 5 張（保留舊照片；若要替換，另做刪除 API）
        if (photos != null && photos.length > 0) {
            int count = 0;
            for (MultipartFile f : photos) {
                if (f == null || f.isEmpty()) continue;
                try {
                    RestaurantPhoto p = new RestaurantPhoto();
                    p.setRestaurantId(updated.getId());
                    p.setImage(f.getBytes());
                    restaurantPhotoRepository.save(p);
                    count++;
                    if (count >= 5) break;
                } catch (Exception ignore) {
                    // 單張失敗就略過，不影響整體流程
                }
            }
        }

        // 通知管理員：有重新送審
        notificationService.notifyAdmins(
            "餐廳重新送審",
            "商家重新送審：「" + updated.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );

        return "redirect:/merchant/restaurant/mine";
    }


    // 不改內容，直接重新送審（提供快捷鍵用）
    @PostMapping("/resubmit/{id}")
    public String resubmit(@PathVariable Long id, HttpSession session) {
        if (!hasRole(session, "MERCHANT")) return "redirect:/member/login";
        Long merchantId = currentMemberId(session);

        var r = restaurantService.resubmitWithoutChange(id, merchantId);

        notificationService.notifyAdmins(
            "餐廳重新送審",
            "商家重新送審：「" + r.getName() + "」等待審核",
            "/admin/moderation/restaurants?status=PENDING"
        );

        return "redirect:/merchant/restaurant/mine";
    }
}
