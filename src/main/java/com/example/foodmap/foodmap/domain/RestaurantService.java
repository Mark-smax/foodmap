package com.example.foodmap.foodmap.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.foodmap.dto.RestaurantDto;
import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberRepository;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantPhotoRepository photoRepository;
    private final RestaurantReviewRepository reviewRepository;
    private final RestaurantFavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    // ★ 新增：營業時間相關 Repository
    private final RestaurantHourRepository hourRepository;
    private final RestaurantSpecialHourRepository specialHourRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             RestaurantPhotoRepository photoRepository,
                             RestaurantReviewRepository reviewRepository,
                             RestaurantFavoriteRepository favoriteRepository,
                             MemberRepository memberRepository,
                             RestaurantHourRepository hourRepository,
                             RestaurantSpecialHourRepository specialHourRepository) {
        this.restaurantRepository = restaurantRepository;
        this.photoRepository = photoRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.memberRepository = memberRepository;
        this.hourRepository = hourRepository;
        this.specialHourRepository = specialHourRepository;
    }

    // ===== 既有搜尋：不動，保持目前行為 =====
    public Page<RestaurantDto> searchRestaurants(String county, Double minRating, String type, Pageable pageable, Long memberId) {
        boolean hasCounty = county != null && !county.trim().isEmpty();
        boolean hasType = type != null && !type.trim().isEmpty();

        Page<Restaurant> page;

        if (hasCounty && hasType) {
            page = restaurantRepository.findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
                    county, minRating, type, pageable);
        } else if (hasCounty) {
            page = restaurantRepository.findByCounty(county, pageable);
        } else if (hasType) {
            page = restaurantRepository.findByTypeContainingIgnoreCase(type, pageable);
        } else {
            return Page.empty(pageable);
        }

        return convertToDtoPage(page, memberId);
    }

    public Page<RestaurantDto> searchByKeyword(String keyword, Pageable pageable, Long memberId) {
        Page<Restaurant> page = restaurantRepository.searchByKeyword(keyword, pageable);
        return convertToDtoPage(page, memberId);
    }

    public Page<RestaurantDto> searchByCountyAndType(String county, String type, Pageable pageable, Long memberId) {
        Page<Restaurant> page = restaurantRepository.findByCountyAndTypeContainingIgnoreCase(county, type, pageable);
        return convertToDtoPage(page, memberId);
    }

    // ===== 新增：商家提交流程（進入審核） =====
    @Transactional
    public Restaurant submitByMerchant(Restaurant r, Long merchantId) {
        r.setStatus(ModerationStatus.PENDING);
        r.setSubmittedBy(merchantId);
        r.setSubmittedAt(OffsetDateTime.now());
        r.setReviewedBy(null);
        r.setReviewedAt(null);
        r.setRejectReason(null);

        // 若尚未帶 createdBy，順手帶上（你的 createdBy 是 Integer）
        if (r.getCreatedBy() == null && merchantId != null) {
            r.setCreatedBy(merchantId.intValue());
        }
        return restaurantRepository.save(r);
    }

    // ===== 新增：商家自己的餐廳列表（含 PENDING/REJECTED/APPROVED 但僅限本人）=====
    public Page<RestaurantDto> searchMine(Long merchantId, Pageable pageable) {
        Page<Restaurant> page = restaurantRepository.findBySubmittedBy(merchantId, pageable);
        return convertToDtoPage(page, merchantId);
    }

    // ===== 新增：公開列表僅抓已核准（之後控制器切換會用）=====
    public Page<RestaurantDto> searchApprovedAll(Pageable pageable, Long memberId) {
        Page<Restaurant> page = restaurantRepository.findByStatus(ModerationStatus.APPROVED, pageable);
        return convertToDtoPage(page, memberId);
    }

    // ===== DTO 轉換（已改用新建構子，帶入 status / rejectReason） =====
    private Page<RestaurantDto> convertToDtoPage(Page<Restaurant> page, Long memberId) {
        List<RestaurantDto> dtoList = page.getContent().stream().map(r -> {
            setAverageRating(r);
            String thumbnail = getRandomThumbnail(r.getId());

            boolean isFav = false;
            if (memberId != null) {
                isFav = favoriteRepository.existsByRestaurantIdAndMemberId(r.getId(), memberId);
            }

            String uploaderName = getUploaderNickname(r.getCreatedBy());
            String status = (r.getStatus() == null ? null : r.getStatus().name());
            String rejectReason = r.getRejectReason();

            return new RestaurantDto(
                    r.getId(),
                    r.getName(),
                    r.getAddress(),
                    r.getPhone(),
                    r.getCounty(),
                    r.getType(),
                    r.getAvgRating(),
                    thumbnail,
                    isFav,
                    uploaderName,
                    status,
                    rejectReason
            );
        }).collect(Collectors.toList());

        // 收藏置頂 + 星等排序
        dtoList.sort((a, b) -> {
            if (a.isFavorite() != b.isFavorite()) {
                return Boolean.compare(b.isFavorite(), a.isFavorite());
            }
            return Double.compare(b.getAvgRating(), a.getAvgRating());
        });

        return new PageImpl<>(dtoList, page.getPageable(), page.getTotalElements());
    }

    private String getUploaderNickname(Integer memberId) {
        if (memberId == null) return "匿名";
        return memberRepository.findById(memberId)
                .map(Member::getMemberNickName)
                .orElse("匿名");
    }

    private String getRandomThumbnail(Long restaurantId) {
        List<RestaurantPhoto> photos = photoRepository.findTop5ByRestaurantIdOrderByIdAsc(restaurantId);
        if (!photos.isEmpty()) {
            int randomIndex = new Random().nextInt(photos.size());
            return Base64.getEncoder().encodeToString(photos.get(randomIndex).getImage());
        }
        return null;
    }

    // ===== 既有 CRUD：不動 =====
    @Transactional
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public Restaurant updateRestaurant(Long id, Restaurant updated) {
        Optional<Restaurant> optional = restaurantRepository.findById(id);
        if (optional.isPresent()) {
            Restaurant existing = optional.get();
            existing.setName(updated.getName());
            existing.setCounty(updated.getCounty());
            existing.setAddress(updated.getAddress());
            existing.setPhone(updated.getPhone());
            existing.setRating(updated.getRating());
            existing.setType(updated.getType());
            existing.setKeywords(updated.getKeywords());
            return restaurantRepository.save(existing);
        } else {
            return null;
        }
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        restaurantRepository.deleteById(id);
    }

    @Transactional
    public void saveReview(RestaurantReview review) {
        reviewRepository.save(review);
    }

    @Transactional
    public void toggleFavorite(Long restaurantId, Long memberId) {
        if (memberId == null) return;

        boolean exists = favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);

        if (exists) {
            favoriteRepository.deleteByRestaurantIdAndMemberId(restaurantId, memberId);
        } else {
            RestaurantFavorite fav = new RestaurantFavorite();
            fav.setRestaurantId(restaurantId);
            fav.setMemberId(memberId); // ✅ Long
            favoriteRepository.save(fav);
        }
    }

    public Restaurant getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("餐廳不存在"));
    }

    public List<RestaurantPhoto> getPhotosByRestaurantId(Long id) {
        return photoRepository.findTop5ByRestaurantIdOrderByIdAsc(id);
    }

    public List<RestaurantReview> getReviewsByRestaurantId(Long id) {
        return reviewRepository.findByRestaurantIdOrderByCreatedTimeDesc(id);
    }

    /**
     * ✅ 餐廳詳細資訊：加入 weeklyHours / 今日狀態（openNow、todayRange、todayStatusText、todayLabel）
     */
    public RestaurantDetailsDTO getRestaurantDetails(Long restaurantId, Long memberId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        setAverageRating(restaurant);

        List<String> base64Photos = getPhotosByRestaurantId(restaurantId).stream()
            .map(photo -> Base64.getEncoder().encodeToString(photo.getImage()))
            .collect(Collectors.toList());

        List<RestaurantReview> reviews = getReviewsByRestaurantId(restaurantId);

        boolean isFavorite = false;
        if (memberId != null) {
            isFavorite = favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);
        }

        String uploaderNickname = getUploaderNickname(restaurant.getCreatedBy());

        // 先建立 DTO（舊五參數建構子）
        RestaurantDetailsDTO dto = new RestaurantDetailsDTO(restaurant, base64Photos, reviews, isFavorite, uploaderNickname);

        // === 營業時間週表 ===
        LinkedHashMap<DayOfWeek, List<String>> weekly = buildWeeklyHours(restaurantId);
        dto.setWeeklyHours(weekly);

        // === 今日狀態 ===
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        DayOfWeek dow = today.getDayOfWeek();

        boolean openNow;
        String todayRangeText;

        var specOpt = specialHourRepository.findByRestaurantIdAndSpecificDate(restaurantId, today);
        if (specOpt.isPresent()) {
            var spec = specOpt.get();
            if (spec.isClosedAllDay()) {
                openNow = false;
                todayRangeText = "公休";
            } else if (spec.getOpenTime() == null || spec.getCloseTime() == null) {
                openNow = false;
                todayRangeText = "未設定";
            } else {
                openNow = within(now.toLocalTime(), spec.getOpenTime(), spec.getCloseTime());
                todayRangeText = formatRange(spec.getOpenTime(), spec.getCloseTime());
            }
        } else {
            var todays = hourRepository.findByRestaurantIdAndDayOfWeekOrderByIdAsc(restaurantId, dow);
            if (todays.isEmpty()) {
                openNow = false;
                todayRangeText = "未設定";
            } else if (todays.stream().anyMatch(RestaurantHour::isClosedAllDay)) {
                openNow = false;
                todayRangeText = "公休";
            } else {
                LocalTime current = now.toLocalTime();
                openNow = todays.stream().anyMatch(h -> within(current, h.getOpenTime(), h.getCloseTime()));
                todayRangeText = todays.stream()
                        .map(h -> formatRange(h.getOpenTime(), h.getCloseTime()))
                        .collect(Collectors.joining(" / "));
            }
        }

        dto.setOpenNow(openNow);
        dto.setTodayRange(todayRangeText);
        dto.setTodayLabel(toZhDow(dow));
        dto.setTodayStatusText(openNow ? "營業中" : ("公休".equals(todayRangeText) ? "今日公休" : "已打烊"));

        return dto;
    }
    private void setAverageRating(Restaurant restaurant) {
        List<RestaurantReview> reviews = reviewRepository.findByRestaurantId(restaurant.getId());
        if (!reviews.isEmpty()) {
            double avg = reviews.stream()
                .mapToInt(RestaurantReview::getRating)
                .average()
                .orElse(0.0);
            restaurant.setAvgRating(Math.round(avg * 10.0) / 10.0);
        } else {
            restaurant.setAvgRating(0.0);
        }
    }

    public List<Restaurant> getTopRestaurantsByAvgRating(int limit) {
        List<Restaurant> all = restaurantRepository.findAll();
        all.forEach(this::setAverageRating);

        return all.stream()
                .sorted((a, b) -> Double.compare(b.getAvgRating(), a.getAvgRating()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id).orElseThrow();
    }

    public void updateRestaurant(Restaurant restaurant) {
        restaurantRepository.save(restaurant);
    }

    // === 被退回後：修改內容並重新送審 ===
    @Transactional
    public Restaurant updateAndResubmit(Long id, Restaurant form, Long merchantId) {
        // 只允許提交者本人操作
        Restaurant r = restaurantRepository.findByIdAndSubmittedBy(id, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("找不到這筆餐廳，或你沒有權限操作"));

        // 更新可編輯欄位（你需要的就補）
        r.setName(form.getName());
        r.setCounty(form.getCounty());
        r.setAddress(form.getAddress());
        r.setPhone(form.getPhone());
        r.setType(form.getType());
        r.setKeywords(form.getKeywords());

        // 重新送審（清空舊審核結果）
        r.setStatus(ModerationStatus.PENDING);
        r.setSubmittedBy(merchantId);
        r.setSubmittedAt(OffsetDateTime.now());
        r.setReviewedBy(null);
        r.setReviewedAt(null);
        r.setRejectReason(null);

        // 保底：若 createdBy 未設，補上
        if (r.getCreatedBy() == null && merchantId != null) {
            r.setCreatedBy(merchantId.intValue());
        }

        return restaurantRepository.save(r);
    }

    // === 被退回後：不改內容直接重新送審 ===
    @Transactional
    public Restaurant resubmitWithoutChange(Long id, Long merchantId) {
        Restaurant r = restaurantRepository.findByIdAndSubmittedBy(id, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("找不到這筆餐廳，或你沒有權限操作"));

        r.setStatus(ModerationStatus.PENDING);
        r.setSubmittedBy(merchantId);
        r.setSubmittedAt(OffsetDateTime.now());
        r.setReviewedBy(null);
        r.setReviewedAt(null);
        r.setRejectReason(null);

        if (r.getCreatedBy() == null && merchantId != null) {
            r.setCreatedBy(merchantId.intValue());
        }

        return restaurantRepository.save(r);
    }

    // ============================================================
    // ============ ★★★ 營業時間：查詢與維護 API ★★★ ============
    // ============================================================

    /**
     * 判斷指定時間點是否「休息」：
     * 1) 若該日有 SpecialHour -> 以特例為準
     * 2) 否則看固定每週的 RestaurantHour
     */
    @Transactional(readOnly = true)
    public boolean isClosedAt(Long restaurantId, LocalDateTime when) {
        LocalDate date = when.toLocalDate();
        LocalTime time = when.toLocalTime();

        // 先看特例
        var specialOpt = specialHourRepository.findByRestaurantIdAndSpecificDate(restaurantId, date);
        if (specialOpt.isPresent()) {
            var s = specialOpt.get();
            if (s.isClosedAllDay()) return true; // 整天公休
            if (s.getOpenTime() == null || s.getCloseTime() == null) return true; // 無有效時段視為休息
            boolean within = within(time, s.getOpenTime(), s.getCloseTime());
            return !within;
        }

        // 沒特例 -> 看固定班表
        DayOfWeek dow = when.getDayOfWeek();
        var todays = hourRepository.findByRestaurantIdAndDayOfWeekOrderByIdAsc(restaurantId, dow);
        if (todays.isEmpty()) {
            // 未設定 -> 視為休息
            return true;
        }

        // 若其中任何一筆是整天公休 -> 視為休息（可依需求調整策略）
        for (var h : todays) {
            if (h.isClosedAllDay()) return true;
        }

        // 只要命中任一有效時段就算營業中
        boolean open = todays.stream().anyMatch(h ->
                h.getOpenTime() != null && h.getCloseTime() != null &&
                within(time, h.getOpenTime(), h.getCloseTime())
        );
        return !open;
    }

    @Transactional(readOnly = true)
    public List<RestaurantSpecialHour> listSpecialHours(Long restaurantId, LocalDate from, LocalDate to) {
        return specialHourRepository.findByRestaurantIdAndSpecificDateBetweenOrderBySpecificDateAsc(
                restaurantId, from, to);
    }

    /**
     * 覆蓋整週固定班表（簡化作法：清掉舊資料、再存新資料）
     */
    @Transactional
    public void replaceWeeklyHours(Long restaurantId, List<RestaurantHour> newHours) {
        hourRepository.deleteByRestaurantId(restaurantId);
        if (newHours != null && !newHours.isEmpty()) {
            for (var h : newHours) {
                h.setRestaurantId(restaurantId);
            }
            hourRepository.saveAll(newHours);
        }
    }

    /**
     * 新增或更新「特例營業時間」
     */
    @Transactional
    public void upsertSpecialHour(RestaurantSpecialHour spec) {
        var opt = specialHourRepository.findByRestaurantIdAndSpecificDate(
                spec.getRestaurantId(), spec.getSpecificDate());
        if (opt.isPresent()) {
            var exist = opt.get();
            exist.setClosedAllDay(spec.isClosedAllDay());
            exist.setOpenTime(spec.getOpenTime());
            exist.setCloseTime(spec.getCloseTime());
            exist.setNote(spec.getNote());
            specialHourRepository.save(exist);
        } else {
            specialHourRepository.save(spec);
        }
    }

    // ============================================================
    // ================== ★★★ 私有工具方法 ★★★ ==================
    // ============================================================

    /**
     * 產出週一→週日的時段表（固定順序），每一天可能有多段。
     */
    private LinkedHashMap<DayOfWeek, List<String>> buildWeeklyHours(Long restaurantId) {
        LinkedHashMap<DayOfWeek, List<String>> map = new LinkedHashMap<>();
        DayOfWeek[] order = new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };
        for (DayOfWeek d : order) {
            var hours = hourRepository.findByRestaurantIdAndDayOfWeekOrderByIdAsc(restaurantId, d);
            // 過濾整天公休，將有效時段格式化
            List<String> ranges = hours.stream()
                    .filter(h -> !h.isClosedAllDay() && h.getOpenTime() != null && h.getCloseTime() != null)
                    .map(h -> formatRange(h.getOpenTime(), h.getCloseTime()))
                    .collect(Collectors.toList());
            map.put(d, ranges);
        }
        return map;
    }

    /**
     * 判斷 now 是否在 [open, close) 內；支援跨夜（open > close）。
     */
    private boolean within(LocalTime now, LocalTime open, LocalTime close) {
        if (open == null || close == null) return false;
        if (open.equals(close)) return false; // 0 時段
        if (open.isBefore(close)) {
            // 同日時段
            return !now.isBefore(open) && now.isBefore(close);
        } else {
            // 跨夜：例如 18:00 ~ 02:00
            return !now.isBefore(open) || now.isBefore(close);
        }
    }

    private String formatRange(LocalTime open, LocalTime close) {
        if (open == null || close == null) return "未設定";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
        return open.format(fmt) + "–" + close.format(fmt);
    }

    private String toZhDow(DayOfWeek d) {
        switch (d) {
            case MONDAY: return "週一";
            case TUESDAY: return "週二";
            case WEDNESDAY: return "週三";
            case THURSDAY: return "週四";
            case FRIDAY: return "週五";
            case SATURDAY: return "週六";
            case SUNDAY: return "週日";
            default: return "";
        }
    }
 // 讓編輯頁可以預載現有的每週時段（原始 entity 列表）
    @Transactional(readOnly = true)
    public List<RestaurantHour> getWeeklyHourEntities(Long restaurantId) {
        return hourRepository.findByRestaurantIdOrderByDayOfWeekAsc(restaurantId);
    }

}
