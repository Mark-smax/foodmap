package com.example.foodmap.foodmap.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // ===== 既有：後台/商家中心可用（保留） =====
    List<Restaurant> findByCounty(String county);

    Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
            String county, Double rating, String type, Pageable pageable);

    Page<Restaurant> findByCountyAndTypeContainingIgnoreCase(
            String county, String type, Pageable pageable);

    Page<Restaurant> findByTypeContainingIgnoreCase(String type, Pageable pageable);

    Page<Restaurant> findByCounty(String county, Pageable pageable);

    @Query("""
        SELECT r FROM Restaurant r
        WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(r.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Restaurant> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    Page<Restaurant> findByKeywordsContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // 這個方法名雖叫 WithReviews，但目前只是全撈；若要真的 join fetch 請再告訴我
    @Query("SELECT r FROM Restaurant r")
    List<Restaurant> findAllWithReviews();


    // ===== 審核/權限相關 =====
    // 管理員待審清單
    Page<Restaurant> findByStatus(ModerationStatus status, Pageable pageable);

    // 商家自己的投稿列表
    Page<Restaurant> findBySubmittedBy(Long submittedBy, Pageable pageable);

    // 商家本人編輯/重送審
    Optional<Restaurant> findByIdAndSubmittedBy(Long id, Long submittedBy);


    // ===== 對外公開搜尋：只回傳 APPROVED =====
    Page<Restaurant> findByStatusAndCounty(
            ModerationStatus status, String county, Pageable pageable);

    Page<Restaurant> findByStatusAndTypeContainingIgnoreCase(
            ModerationStatus status, String type, Pageable pageable);

    Page<Restaurant> findByStatusAndCountyAndTypeContainingIgnoreCase(
            ModerationStatus status, String county, String type, Pageable pageable);

    Page<Restaurant> findByStatusAndCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
            ModerationStatus status, String county, Double rating, String type, Pageable pageable);

    @Query("""
        SELECT r FROM Restaurant r
        WHERE r.status = :status AND (
            LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(r.address) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
            LOWER(r.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        """)
    Page<Restaurant> searchApprovedByKeyword(@Param("keyword") String keyword,
                                             @Param("status") ModerationStatus status,
                                             Pageable pageable);
}
