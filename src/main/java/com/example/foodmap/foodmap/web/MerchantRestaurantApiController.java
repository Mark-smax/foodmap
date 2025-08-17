// src/main/java/com/example/foodmap/foodmap/web/MerchantRestaurantApiController.java
package com.example.foodmap.foodmap.web;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.foodmap.foodmap.domain.ModerationStatus;
import com.example.foodmap.foodmap.domain.Restaurant;
import com.example.foodmap.foodmap.domain.RestaurantRepository;
import com.example.foodmap.foodmap.domain.RestaurantService;
import com.example.foodmap.foodmap.domain.RestaurantPhoto;
import com.example.foodmap.foodmap.domain.RestaurantPhotoRepository;
import com.example.foodmap.foodmap.domain.RestaurantHour;
import com.example.foodmap.foodmap.domain.RestaurantHourRepository;
import com.example.foodmap.foodmap.dto.RestaurantDto;

@RestController
@RequestMapping("/api/merchant/restaurants")
public class MerchantRestaurantApiController {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantService restaurantService;
    private final RestaurantPhotoRepository photoRepository;
    private final RestaurantHourRepository hourRepository;

    public MerchantRestaurantApiController(RestaurantRepository restaurantRepository,
                                           RestaurantService restaurantService,
                                           RestaurantPhotoRepository photoRepository,
                                           RestaurantHourRepository hourRepository) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantService = restaurantService;
        this.photoRepository = photoRepository;
        this.hourRepository = hourRepository;
    }

    // ---------------- 共用：擁有者檢查 ----------------
    private ResponseEntity<?> forbidIfNotOwner(Restaurant r, Long memberId) {
        if (!Objects.equals(r.getSubmittedBy(), memberId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("ok", false, "error", "NOT_OWNER"));
        }
        return null;
    }

    // ================== 我的餐廳清單 ==================
    @GetMapping("/mine")
    public Page<RestaurantDto> myRestaurants(
            @RequestParam("memberId") Long memberId,
            @PageableDefault(size = 12) Pageable pageable) {

        Page<Restaurant> page = restaurantRepository.findBySubmittedBy(memberId, pageable);
        // 你在 RestaurantService 已新增 toDtoPublic / convertToDtoPagePublic
        return restaurantService.convertToDtoPagePublic(page, memberId);
    }

    // ================== 取得單筆（編輯預載） ==================
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(
            @PathVariable Long id,
            @RequestParam("memberId") Long memberId) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(r, memberId);
        if (forbid != null) return forbid;

        return ResponseEntity.ok(restaurantService.toDtoPublic(r, memberId));
    }

    // ================== 新增 ==================
    @PostMapping
    public ResponseEntity<?> create(
            @RequestParam("memberId") Long memberId,
            @RequestBody UpsertReq req) {

        Restaurant r = new Restaurant();
        r.setName(req.name);
        r.setCounty(req.county);
        r.setAddress(req.address);
        r.setPhone(req.phone);
        r.setType(req.finalType()); // 單選或多選 types 會在這裡串成一個字串
        r.setKeywords(req.keywords);
        r.setSubmittedBy(memberId);
        r.setStatus(ModerationStatus.PENDING);

        Restaurant saved = restaurantService.createRestaurant(r);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("ok", true, "id", saved.getId()));
    }

    // ================== 修改 ==================
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable Long id,
            @RequestParam("memberId") Long memberId,
            @RequestBody UpsertReq req) {

        Restaurant existing = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(existing, memberId);
        if (forbid != null) return forbid;

        Restaurant patch = new Restaurant();
        patch.setName(req.name);
        patch.setCounty(req.county);
        patch.setAddress(req.address);
        patch.setPhone(req.phone);
        patch.setType(req.finalType());
        patch.setKeywords(req.keywords);

        restaurantService.updateRestaurant(id, patch);
        return ResponseEntity.ok(Map.of("ok", true, "id", id));
    }

    // 前端 upsert payload：支援單一 type 或多選 types
    public static class UpsertReq {
        public String name;
        public String county;
        public String address;
        public String phone;
        public String type;             // 單選
        public List<String> types;      // 多選
        public String keywords;

        public String finalType() {
            if (type != null && !type.isBlank()) return type;
            if (types != null && !types.isEmpty()) return String.join(",", types);
            return null;
        }
    }

    // ================== 每週營業時間：讀取 ==================
    @GetMapping("/{id}/hours")
    public ResponseEntity<?> getWeeklyHours(@PathVariable Long id,
                                            @RequestParam("memberId") Long memberId) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(r, memberId);
        if (forbid != null) return forbid;

        var rows = restaurantService.getWeeklyHourEntities(id).stream()
                .map(h -> Map.of(
                        "dayOfWeek", h.getDayOfWeek().name(),
                        "openTime",  h.getOpenTime() == null ? null : h.getOpenTime().toString(),
                        "closeTime", h.getCloseTime() == null ? null : h.getCloseTime().toString(),
                        "closedAllDay", h.isClosedAllDay()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(rows);
    }

    // ================== 每週營業時間：覆蓋儲存 ==================
    public static class HourReq {
        public String dayOfWeek;     // e.g. "MONDAY"
        public String openTime;      // "HH:mm"
        public String closeTime;     // "HH:mm"
        public Boolean closedAllDay;
    }

    @PostMapping("/{id}/hours")
    public ResponseEntity<?> replaceWeeklyHours(@PathVariable Long id,
                                                @RequestParam("memberId") Long memberId,
                                                @RequestBody List<HourReq> hours) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(r, memberId);
        if (forbid != null) return forbid;

        List<RestaurantHour> entities = new ArrayList<>();
        if (hours != null) {
            for (HourReq h : hours) {
                if (h == null || h.dayOfWeek == null) continue;
                RestaurantHour e = new RestaurantHour();
                e.setRestaurantId(id);
                e.setDayOfWeek(DayOfWeek.valueOf(h.dayOfWeek));
                boolean closed = Boolean.TRUE.equals(h.closedAllDay);
                e.setClosedAllDay(closed);
                if (!closed) {
                    if (h.openTime != null && !h.openTime.isBlank())
                        e.setOpenTime(LocalTime.parse(h.openTime));
                    if (h.closeTime != null && !h.closeTime.isBlank())
                        e.setCloseTime(LocalTime.parse(h.closeTime));
                }
                entities.add(e);
            }
        }
        restaurantService.replaceWeeklyHours(id, entities); // 空陣列 = 清空
        return ResponseEntity.ok(Map.of("ok", true, "count", entities.size()));
    }

    // ================== 照片：清單 / 上傳 / 刪除 ==================
    public static class PhotoOut {
        public Long id;
        public String url;
        public PhotoOut(Long id, String url) { this.id = id; this.url = url; }
    }

    @GetMapping("/{id}/photos")
    public ResponseEntity<?> listPhotos(@PathVariable Long id,
                                        @RequestParam("memberId") Long memberId) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(r, memberId);
        if (forbid != null) return forbid;

        var list = photoRepository.findTop5ByRestaurantIdOrderByIdAsc(id).stream()
                .map(p -> new PhotoOut(p.getId(), "/api/restaurants/photos/" + p.getId()))
                .toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping(path = "/{id}/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhotos(@PathVariable Long id,
                                          @RequestParam("memberId") Long memberId,
                                          @RequestParam("photos") MultipartFile[] photos) throws Exception {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(r, memberId);
        if (forbid != null) return forbid;

        int added = 0;
        if (photos != null) {
            for (MultipartFile f : photos) {
                if (f == null || f.isEmpty()) continue;
                RestaurantPhoto p = new RestaurantPhoto();
                p.setRestaurantId(id);
                p.setImage(f.getBytes());
                photoRepository.save(p);
                added++;
                if (added >= 5) break; // 最多 5 張
            }
        }
        return ResponseEntity.ok(Map.of("ok", true, "added", added));
    }

    @DeleteMapping("/{id}/photos/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Long id,
                                         @PathVariable Long photoId,
                                         @RequestParam("memberId") Long memberId) {
        Restaurant r = restaurantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("restaurant not found"));
        var forbid = forbidIfNotOwner(r, memberId);
        if (forbid != null) return forbid;

        photoRepository.findById(photoId).ifPresent(p -> {
            if (Objects.equals(p.getRestaurantId(), id)) {
                photoRepository.deleteById(photoId);
            }
        });
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
