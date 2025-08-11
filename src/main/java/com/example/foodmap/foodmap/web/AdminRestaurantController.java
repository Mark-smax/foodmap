package com.example.foodmap.foodmap.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantPhoto;
import com.example.foodmap.foodmap.domain.RestaurantPhotoRepository;
import com.example.foodmap.foodmap.domain.RestaurantService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/restaurant")
public class AdminRestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantPhotoRepository photoRepository;

    public AdminRestaurantController(RestaurantService restaurantService,
                                     RestaurantPhotoRepository photoRepository) {
        this.restaurantService = restaurantService;
        this.photoRepository = photoRepository;
    }

    // 顯示新增餐廳表單
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, RedirectAttributes redirectAttrs, Model model) {
        Object roleObj = session.getAttribute("loginMemberRoles");

        if (roleObj == null) {
            redirectAttrs.addFlashAttribute("error", "請先登入！");
            return "redirect:/login";
        }

        if (!"ADMIN".equals(roleObj.toString())) {
            redirectAttrs.addFlashAttribute("error", "您沒有權限進入此頁面！");
            return "redirect:/";
        }

        return "restaurant-create";
    }

    // 處理新增餐廳表單送出
    @PostMapping(value = "/createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createRestaurant(@RequestParam String name,
                                   @RequestParam String address,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam String county,
                                   @RequestParam(required = false, name = "type") String[] types,
                                   @RequestParam(required = false) String keywords,
                                   @RequestParam(required = false, name = "photos") MultipartFile[] photos,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        Object roleObj = session.getAttribute("loginMemberRoles");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("error", "您沒有權限執行此操作！");
            return "redirect:/login";
        }

        // 必填欄位檢查
        if (isBlank(name) || isBlank(address) || isBlank(county)) {
            redirectAttributes.addFlashAttribute("error", "請填寫必填欄位（店名 / 地址 / 縣市）。");
            return "redirect:/admin/restaurant/create";
        }

        // 整理欄位
        String safeName = name.trim();
        String safeAddress = address.trim();
        String safePhone = phone == null ? "" : phone.trim();
        String safeCounty = county.trim();
        String joinedTypes = (types == null || types.length == 0)
                ? ""
                : String.join(",", Arrays.stream(types)
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList());
        String safeKeywords = keywords == null ? "" : keywords.trim();

        try {
            // 1) 建立餐廳
            Restaurant restaurant = new Restaurant();
            restaurant.setName(safeName);
            restaurant.setAddress(safeAddress);
            restaurant.setPhone(safePhone);
            restaurant.setCounty(safeCounty);
            restaurant.setRating(0.0); // 預設 0 顆星
            restaurant.setType(joinedTypes);
            restaurant.setKeywords(safeKeywords);

            Restaurant saved = restaurantService.createRestaurant(restaurant);

            // 2) 儲存最多 5 張圖片（忽略空檔）
            if (photos != null && photos.length > 0) {
                int limit = Math.min(5, photos.length);
                for (int i = 0; i < limit; i++) {
                    MultipartFile photo = photos[i];
                    if (photo != null && !photo.isEmpty()) {
                        byte[] image = photo.getBytes();
                        if (image != null && image.length > 0) {
                            RestaurantPhoto entity = new RestaurantPhoto();
                            entity.setRestaurantId(saved.getId());
                            entity.setImage(image);
                            photoRepository.save(entity);
                        }
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "餐廳新增成功！");
            // 你也可以改導向詳細頁：return "redirect:/restaurant/" + saved.getId();
            return "redirect:/admin/restaurant/create";
        } catch (MaxUploadSizeExceededException ex) {
            // 超過 spring.servlet.multipart.max-*
            redirectAttributes.addFlashAttribute("error", "上傳失敗：檔案或表單總大小超過限制（單檔 ≤ 15MB、總計 ≤ 50MB）。");
            return "redirect:/admin/restaurant/create";
        } catch (MultipartException ex) {
            // 例如：Tomcat 解析 multipart 失敗（常見於 server.tomcat.max-http-form-post-size 太小）
            redirectAttributes.addFlashAttribute("error", "上傳失敗：無法解析上傳資料，請確認檔案大小與格式。");
            return "redirect:/admin/restaurant/create";
        } catch (IOException ex) {
            redirectAttributes.addFlashAttribute("error", "上傳失敗：讀取檔案時發生錯誤。");
            return "redirect:/admin/restaurant/create";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "新增失敗：" + ex.getMessage());
            return "redirect:/admin/restaurant/create";
        }
    }

    // 顯示編輯餐廳表單
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttrs) {
        Object roleObj = session.getAttribute("loginMemberRoles");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            redirectAttrs.addFlashAttribute("error", "您沒有權限執行此操作！");
            return "redirect:/login";
        }

        Restaurant restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null) {
            redirectAttrs.addFlashAttribute("error", "找不到指定的餐廳。");
            return "redirect:/";
        }
        model.addAttribute("restaurant", restaurant);
        return "restaurant-edit";
    }

    // 處理更新餐廳表單送出
    @PostMapping("/update")
    public String updateRestaurant(@RequestParam Long id,
                                   @RequestParam String name,
                                   @RequestParam String address,
                                   @RequestParam(required = false) String phone,
                                   @RequestParam String county,
                                   @RequestParam(required = false, name = "type") String[] types,
                                   @RequestParam(required = false) String keywords,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        Object roleObj = session.getAttribute("loginMemberRoles");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("error", "您沒有權限執行此操作！");
            return "redirect:/login";
        }

        Restaurant restaurant = restaurantService.getRestaurantById(id);
        if (restaurant == null) {
            redirectAttributes.addFlashAttribute("error", "找不到指定的餐廳。");
            return "redirect:/";
        }

        // 必填欄位
        if (isBlank(name) || isBlank(address) || isBlank(county)) {
            redirectAttributes.addFlashAttribute("error", "請填寫必填欄位（店名 / 地址 / 縣市）。");
            return "redirect:/admin/restaurant/edit/" + id;
        }

        restaurant.setName(name.trim());
        restaurant.setAddress(address.trim());
        restaurant.setPhone(phone == null ? "" : phone.trim());
        restaurant.setCounty(county.trim());
        restaurant.setKeywords(keywords == null ? "" : keywords.trim());
        if (types != null && types.length > 0) {
            String joined = String.join(",", Arrays.stream(types)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList());
            restaurant.setType(joined);
        } else {
            restaurant.setType("");
        }

        restaurantService.updateRestaurant(id, restaurant);
        redirectAttributes.addFlashAttribute("success", "更新成功！");
        return "redirect:/admin/restaurant/edit/" + id;
    }

    // 處理刪除餐廳
    @PostMapping("/delete/{id}")
    public String deleteRestaurant(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttrs) {
        Object roleObj = session.getAttribute("loginMemberRoles");
        if (roleObj == null || !"ADMIN".equals(roleObj.toString())) {
            redirectAttrs.addFlashAttribute("error", "您沒有權限執行此操作！");
            return "redirect:/login";
        }

        restaurantService.deleteRestaurant(id);
        redirectAttrs.addFlashAttribute("success", "已刪除餐廳！");
        return "redirect:/";
    }

    // ===== utils =====
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
