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
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;

import com.example.foodmap.foodmap.dto.PhotoDto;           // STEP1
import com.example.foodmap.foodmap.dto.RestaurantDetailsDTO;
import com.example.foodmap.foodmap.dto.RestaurantDto;
import com.example.foodmap.foodmap.dto.ReviewDto;          // STEP2
import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberRepository;


@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantPhotoRepository photoRepository;
    private final RestaurantReviewRepository reviewRepository;
    private final RestaurantFavoriteRepository favoriteRepository;
    private final MemberRepository memberRepository;

    // 營業時間
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

    // ===== 搜尋（保留） =====
    public Page<RestaurantDto> searchRestaurants(String county, Double minRating, String type, Pageable pageable, Long memberId) {
        boolean hasCounty = county != null && !county.trim().isEmpty();
        boolean hasType   = type   != null && !type.trim().isEmpty();

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

    @Transactional
    public Restaurant submitByMerchant(Restaurant r, Long merchantId) {
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

    public Page<RestaurantDto> searchMine(Long merchantId, Pageable pageable) {
        Page<Restaurant> page = restaurantRepository.findBySubmittedBy(merchantId, pageable);
        return convertToDtoPage(page, merchantId);
    }

    public Page<RestaurantDto> searchApprovedAll(Pageable pageable, Long memberId) {
        Page<Restaurant> page = restaurantRepository.findByStatus(ModerationStatus.APPROVED, pageable);
        return convertToDtoPage(page, memberId);
    }

    private Page<RestaurantDto> convertToDtoPage(Page<Restaurant> page, Long memberId) {
        List<RestaurantDto> dtoList = page.getContent().stream().map(r -> {
            setAverageRating(r);
            String thumbnail = getRandomThumbnail(r.getId());

            boolean isFav = (memberId != null) &&
                    favoriteRepository.existsByRestaurantIdAndMemberId(r.getId(), memberId);

            String uploaderName = getUploaderNickname(r.getCreatedBy());
            String status       = (r.getStatus() == null ? null : r.getStatus().name());
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

    // ===== 既有 CRUD =====
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
    public boolean toggleFavorite(Long restaurantId, Long memberId) {
        if (restaurantId == null || memberId == null) {
            throw new IllegalArgumentException("restaurantId / memberId must not be null");
        }

        return favoriteRepository.findByRestaurantIdAndMemberId(restaurantId, memberId)
            .map(existing -> {
                // 已收藏 → 取消收藏
                favoriteRepository.delete(existing);
                return false; // 現在狀態為「未收藏」
            })
            .orElseGet(() -> {
                // 尚未收藏 → 新增收藏
                RestaurantFavorite fav = new RestaurantFavorite();
                fav.setRestaurantId(restaurantId);
                fav.setMemberId(memberId);
                favoriteRepository.save(fav);
                return true; // 現在狀態為「已收藏」
            });
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
     * 餐廳詳細資訊（Step1: photos→URL；Step2: reviews→ReviewDto）
     */
    public RestaurantDetailsDTO getRestaurantDetails(Long restaurantId, Long memberId) {
        Restaurant restaurant = getRestaurantById(restaurantId);
        setAverageRating(restaurant);

        List<RestaurantReview> reviews = getReviewsByRestaurantId(restaurantId);

        boolean isFavorite = (memberId != null) &&
                favoriteRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);

        String uploaderNickname = getUploaderNickname(restaurant.getCreatedBy());

        // 用無參建構子逐一 set，避免舊建構子型別不相容
        RestaurantDetailsDTO dto = new RestaurantDetailsDTO();
        dto.setRestaurant(restaurant);
        dto.setFavorite(isFavorite);
        dto.setUploaderNickname(uploaderNickname);

        // 照片（URL）
        dto.setPhotos(buildPhotoDtos(restaurantId));

        // 評論轉 ReviewDto（時間保留 LocalDateTime，前端格式化）
        List<ReviewDto> reviewDtos = reviews.stream()
            .map(rv -> new ReviewDto(
                    rv.getId(),
                    rv.getMemberId() == null ? null : rv.getMemberId().longValue(),
                    getUploaderNickname(rv.getMemberId()),
                    rv.getRating(),
                    rv.getComment(),
                    rv.getCreatedTime(),                // 傳 LocalDateTime
                    Boolean.TRUE.equals(rv.getIsHidden())
            ))
            .collect(Collectors.toList());
        dto.setReviews(reviewDtos);

        // 營業時間週表
        LinkedHashMap<DayOfWeek, List<String>> weekly = buildWeeklyHours(restaurantId);
        dto.setWeeklyHours(weekly);

        // 今日狀態
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

    // 被退回後：修改內容並重新送審
    @Transactional
    public Restaurant updateAndResubmit(Long id, Restaurant form, Long merchantId) {
        Restaurant r = restaurantRepository.findByIdAndSubmittedBy(id, merchantId)
                .orElseThrow(() -> new IllegalArgumentException("找不到這筆餐廳，或你沒有權限操作"));

        r.setName(form.getName());
        r.setCounty(form.getCounty());
        r.setAddress(form.getAddress());
        r.setPhone(form.getPhone());
        r.setType(form.getType());
        r.setKeywords(form.getKeywords());

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

    // 被退回後：不改內容直接重新送審
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
    // 營業時間
    // ============================================================

    @Transactional(readOnly = true)
    public boolean isClosedAt(Long restaurantId, LocalDateTime when) {
        LocalDate date = when.toLocalDate();
        LocalTime time = when.toLocalTime();

        var specialOpt = specialHourRepository.findByRestaurantIdAndSpecificDate(restaurantId, date);
        if (specialOpt.isPresent()) {
            var s = specialOpt.get();
            if (s.isClosedAllDay()) return true;
            if (s.getOpenTime() == null || s.getCloseTime() == null) return true;
            boolean within = within(time, s.getOpenTime(), s.getCloseTime());
            return !within;
        }

        DayOfWeek dow = when.getDayOfWeek();
        var todays = hourRepository.findByRestaurantIdAndDayOfWeekOrderByIdAsc(restaurantId, dow);
        if (todays.isEmpty()) return true;

        for (var h : todays) {
            if (h.isClosedAllDay()) return true;
        }

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

    @Transactional
    public void upsertSpecialHour(RestaurantSpecialHour spec) {
        var opt = specialHourRepository.findByRestaurantIdAndSpecificDate(
                spec.getRestaurantId(), spec.getSpecificDate());
        if (opt.isPresent()) {
            var exist = opt.get();
            exist.setClosedAllDay(spec.isClosedAllDay());
            exist.setOpenTime(spec.getOpenTime());   // 修正：用 spec
            exist.setCloseTime(spec.getCloseTime()); // 修正：用 spec
            exist.setNote(spec.getNote());
            specialHourRepository.save(exist);
        } else {
            specialHourRepository.save(spec);
        }
    }

    // ============================================================
    // Step 4/5：評論相關（分頁 / 新增 / 編輯 / 刪除 / 隱藏）
    // ============================================================

    // 查詢單張照片（給 /photos/{id}）
    public RestaurantPhoto findPhotoById(Long photoId) {
        return photoRepository.findById(photoId).orElse(null);
    }

    // 簡易判斷圖片 Content-Type
    public String guessImageContentType(byte[] data) {
        if (data == null || data.length < 12) return "image/jpeg";
        if ((data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) return "image/jpeg"; // JPEG
        if ((data[0] & 0xFF) == 0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) return "image/png";  // PNG
        if (data[0] == 'G' && data[1] == 'I' && data[2] == 'F' && data[3] == '8' && (data[4] == '7' || data[4] == '9') && data[5] == 'a') return "image/gif"; // GIF
        if (data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F' &&
            data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P') return "image/webp"; // WEBP
        return "image/jpeg";
    }

    // 分頁取得評論（Repository 原生分頁）
    public Page<ReviewDto> getReviewPage(Long restaurantId,
                                         int page, int size,
                                         String sortBy, String order,
                                         boolean includeHidden) {
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        String sortProp;
        if ("rating".equalsIgnoreCase(sortBy))      sortProp = "rating";
        else if ("id".equalsIgnoreCase(sortBy))     sortProp = "id";
        else                                        sortProp = "createdTime";

        Sort.Direction dir = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pr = PageRequest.of(page, size, Sort.by(dir, sortProp));

        Page<RestaurantReview> raw = includeHidden
                ? reviewRepository.findByRestaurantId(restaurantId, pr)
                : reviewRepository.findByRestaurantIdAndIsHiddenFalse(restaurantId, pr);

        return raw.map(rv -> new ReviewDto(
                rv.getId(),
                rv.getMemberId() == null ? null : rv.getMemberId().longValue(),
                getUploaderNickname(rv.getMemberId()),
                rv.getRating(),
                rv.getComment(),
                rv.getCreatedTime(),
                Boolean.TRUE.equals(rv.getIsHidden())
        ));
    }

    // （保留）新增評論：memberId 為 Long 的版本
    @Transactional
    public ReviewDto createReview(Long restaurantId, Long memberId, int rating, String comment) {
        if (memberId == null) throw new IllegalArgumentException("未登入或缺少會員資訊");
        Integer mid = memberId.intValue();
        return createReview(restaurantId, mid, rating, comment, false);
    }

    // 新增評論：支援 hidden 與 Integer memberId（前端表單友善）
    @Transactional
    public ReviewDto createReview(Long restaurantId, Integer memberId, int rating, String comment, Boolean hidden) {
        if (restaurantId == null || memberId == null)
            throw new IllegalArgumentException("restaurantId / memberId 不可為空");
        if (rating < 1 || rating > 5)
            throw new IllegalArgumentException("評分需介於 1~5 星");
        if (comment == null || comment.isBlank())
            throw new IllegalArgumentException("comment 不可為空");

        // 防重複：用 (Long, Integer) 版本，符合 entity 欄位型別
        boolean exists = reviewRepository.existsByRestaurantIdAndMemberId(restaurantId, memberId);
        if (exists) throw new IllegalStateException("你已對此餐廳發表過評論");

        RestaurantReview rv = new RestaurantReview();
        rv.setRestaurantId(restaurantId);
        rv.setMemberId(memberId);
        rv.setRating(rating);
        rv.setComment(comment.trim());
        rv.setCreatedTime(LocalDateTime.now());
        rv.setIsHidden(Boolean.TRUE.equals(hidden));
        reviewRepository.save(rv);

        // 重算平均星等
        restaurantRepository.findById(restaurantId).ifPresent(r -> {
            setAverageRating(r);
            restaurantRepository.save(r);
        });

        return new ReviewDto(
                rv.getId(),
                memberId.longValue(),
                getUploaderNickname(rv.getMemberId()),
                rv.getRating(),
                rv.getComment(),
                rv.getCreatedTime(),
                Boolean.TRUE.equals(rv.getIsHidden())
        );
    }

    // 編輯自己的評論（以 id 取出，再檢查 owner，避免 Repository 型別不一致問題）
    @Transactional
    public boolean updateReview(Long reviewId, Long memberId, Integer rating, String comment) {
        if (reviewId == null || memberId == null) return false;
        Optional<RestaurantReview> opt = reviewRepository.findById(reviewId);
        if (opt.isEmpty()) return false;
        RestaurantReview rv = opt.get();
        if (rv.getMemberId() == null || rv.getMemberId().intValue() != memberId.intValue()) return false;

        if (rating != null) {
            if (rating < 1 || rating > 5) throw new IllegalArgumentException("評分需介於 1~5 星");
            rv.setRating(rating);
        }
        if (comment != null) {
            if (comment.isBlank()) throw new IllegalArgumentException("comment 不可為空");
            rv.setComment(comment);
        }
        return true;
    }

    // 刪除自己的評論（同上邏輯）
    @Transactional
    public boolean deleteReviewByIdAndMemberId(Long reviewId, Long memberId) {
        if (reviewId == null || memberId == null) return false;
        Optional<RestaurantReview> opt = reviewRepository.findById(reviewId);
        if (opt.isEmpty()) return false;
        RestaurantReview rv = opt.get();
        if (rv.getMemberId() == null || rv.getMemberId().intValue() != memberId.intValue()) return false;

        reviewRepository.delete(rv);
        return true;
    }

    // 管理員：設定隱藏（簡化版）
    @Transactional
    public void setReviewHidden(Long reviewId, boolean hidden) {
        reviewRepository.findById(reviewId).ifPresent(r -> r.setIsHidden(hidden));
    }

    // ============================================================
    // 私有工具
    // ============================================================

    // STEP1：照片轉 URL
    private List<PhotoDto> buildPhotoDtos(Long restaurantId) {
        return photoRepository.findTop5ByRestaurantIdOrderByIdAsc(restaurantId).stream()
                .filter(p -> p.getImage() != null && p.getImage().length > 0)
                .map(p -> new PhotoDto(p.getId(), "/api/restaurants/photos/" + p.getId()))
                .collect(Collectors.toList());
    }

    // 產出週一→週日
    private LinkedHashMap<DayOfWeek, List<String>> buildWeeklyHours(Long restaurantId) {
        LinkedHashMap<DayOfWeek, List<String>> map = new LinkedHashMap<>();
        DayOfWeek[] order = {
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
        };
        for (DayOfWeek d : order) {
            var hours = hourRepository.findByRestaurantIdAndDayOfWeekOrderByIdAsc(restaurantId, d);
            List<String> ranges = hours.stream()
                    .filter(h -> !h.isClosedAllDay() && h.getOpenTime() != null && h.getCloseTime() != null)
                    .map(h -> formatRange(h.getOpenTime(), h.getCloseTime()))
                    .collect(Collectors.toList());
            map.put(d, ranges);
        }
        return map;
    }

    // 判斷 [open, close)；支援跨夜
    private boolean within(LocalTime now, LocalTime open, LocalTime close) {
        if (open == null || close == null) return false;
        if (open.equals(close)) return false;
        if (open.isBefore(close)) {
            return !now.isBefore(open) && now.isBefore(close);
        } else {
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

    @Transactional(readOnly = true)
    public List<RestaurantHour> getWeeklyHourEntities(Long restaurantId) {
        return hourRepository.findByRestaurantIdOrderByDayOfWeekAsc(restaurantId);
    }
    
    

}
