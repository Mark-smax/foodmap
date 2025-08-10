package com.example.foodmap.foodmap.domain;

import java.time.OffsetDateTime;
import java.util.Base64;
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

    public RestaurantService(RestaurantRepository restaurantRepository,
                             RestaurantPhotoRepository photoRepository,
                             RestaurantReviewRepository reviewRepository,
                             RestaurantFavoriteRepository favoriteRepository,
                             MemberRepository memberRepository) {
        this.restaurantRepository = restaurantRepository;
        this.photoRepository = photoRepository;
        this.reviewRepository = reviewRepository;
        this.favoriteRepository = favoriteRepository;
        this.memberRepository = memberRepository;
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

    // ===== 既有 DTO 轉換：不動 =====
    private Page<RestaurantDto> convertToDtoPage(Page<Restaurant> page, Long memberId) {
        List<RestaurantDto> dtoList = page.getContent().stream().map(r -> {
            setAverageRating(r);
            String thumbnail = getRandomThumbnail(r.getId());

            boolean isFav = false;
            if (memberId != null) {
                isFav = favoriteRepository.existsByRestaurantIdAndMemberId(r.getId(), memberId);
            }

            String uploaderName = getUploaderNickname(r.getCreatedBy());
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
                    uploaderName
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

        return new RestaurantDetailsDTO(restaurant, base64Photos, reviews, isFavorite, uploaderNickname);
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
}
