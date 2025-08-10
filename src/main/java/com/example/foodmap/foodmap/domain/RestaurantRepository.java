package com.example.foodmap.foodmap.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByCounty(String county);

    Page<Restaurant> findByCountyAndRatingGreaterThanEqualAndTypeContainingIgnoreCase(
            String county, Double rating, String type, Pageable pageable);

    Page<Restaurant> findByCountyAndTypeContainingIgnoreCase(String county, String type, Pageable pageable);

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

    @Query("SELECT r FROM Restaurant r")
    List<Restaurant> findAllWithReviews();

    // 審核狀態分頁（之後管理員待審清單會用）
    Page<Restaurant> findByStatus(ModerationStatus status, Pageable pageable);

    // 商家自己的投稿列表
    Page<Restaurant> findBySubmittedBy(Long submittedBy, Pageable pageable);

    // 商家本人才能編輯/重新送審用
    Optional<Restaurant> findByIdAndSubmittedBy(Long id, Long submittedBy);

    // （可選）公開的關鍵字搜尋只抓 APPROVED
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
