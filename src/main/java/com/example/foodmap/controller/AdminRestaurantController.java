package com.example.foodmap.controller;

import com.example.foodmap.model.Restaurant;
import com.example.foodmap.model.RestaurantPhoto;
import com.example.foodmap.repository.RestaurantPhotoRepository;
import com.example.foodmap.service.RestaurantService;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

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

    // 顯示新增表單
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, RedirectAttributes redirectAttrs, Model model) {
        Object roleObj = session.getAttribute("loginMemberRoles");

        if (roleObj == null) {
            redirectAttrs.addFlashAttribute("error", "請先登入！");
            return "redirect:/login"; // 導向登入頁
        }

        if (!"admin".equals(roleObj.toString())) {
            redirectAttrs.addFlashAttribute("error", "您沒有權限進入此頁面！");
            return "redirect:/"; // 回首頁
        }

        return "restaurant-create"; // templates/restaurant-create.html
    }

    // 處理表單送出
    @PostMapping("/create")
    public String createRestaurant(@RequestParam String name,
                                   @RequestParam String address,
                                   @RequestParam String phone,
                                   @RequestParam String county,
                                   @RequestParam(required = false) String[] type,
                                   @RequestParam(required = false) MultipartFile[] photos,
                                   RedirectAttributes redirectAttributes,
                                   HttpSession session) {

        Object roleObj = session.getAttribute("loginMemberRoles");
        if (roleObj == null || !"admin".equals(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("error", "您沒有權限執行此操作！");
            return "redirect:/login";
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setAddress(address);
        restaurant.setPhone(phone);
        restaurant.setCounty(county);
        restaurant.setRating(0.0); // 預設 0 顆星
        if (type != null) {
            restaurant.setType(String.join(",", type));
        }

        // 儲存餐廳
        Restaurant saved = restaurantService.createRestaurant(restaurant);

        // 儲存照片
        if (photos != null) {
            for (MultipartFile photo : photos) {
                if (!photo.isEmpty()) {
                    try {
                        RestaurantPhoto entity = new RestaurantPhoto();
                        entity.setRestaurantId(saved.getId());
                        entity.setImage(photo.getBytes());
                        photoRepository.save(entity);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        redirectAttributes.addFlashAttribute("success", "餐廳新增成功！");
        return "redirect:/admin/restaurant/create";
    }
}
