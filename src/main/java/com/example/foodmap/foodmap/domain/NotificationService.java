package com.example.foodmap.foodmap.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.member.domain.Member;
import com.example.foodmap.member.domain.MemberRepository;
import com.example.foodmap.member.domain.enums.MemberRole;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               MemberRepository memberRepository) {
        this.notificationRepository = notificationRepository;
        this.memberRepository = memberRepository;
    }

    /* ======================== 基本通知 ======================== */

    /** 通知所有管理員（可帶連結） */
    @Transactional
    public void notifyAdmins(String title, String message, String link) {
        List<Member> admins = memberRepository.findByMemberRole(MemberRole.ADMIN);
        if (admins == null || admins.isEmpty()) return;

        List<Notification> batch = new ArrayList<>(admins.size());
        for (Member admin : admins) {
            Integer adminId = admin.getMemberId();
            if (adminId == null) continue; // 沒有 ID 直接略過，避免存入 null
            Notification n = new Notification();
            n.setMemberId(Long.valueOf(adminId));
            n.setTitle(title);
            n.setMessage(message);
            n.setLink(link);
            batch.add(n);
        }
        if (!batch.isEmpty()) {
            notificationRepository.saveAll(batch);
        }
    }

    /** 通知所有管理員（不帶連結） */
    @Transactional
    public void notifyAdmins(String title, String message) {
        notifyAdmins(title, message, null);
    }

    /** 通知單一會員（Long 版本） */
    @Transactional
    public void notifyMember(Long memberId, String title, String message, String link) {
        if (memberId == null) return;
        Notification n = new Notification();
        n.setMemberId(memberId);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        notificationRepository.save(n);
    }

    /** 通知單一會員（Integer 版本，方便直接喂 DB 的 int 主鍵） */
    @Transactional
    public void notifyMember(Integer memberIdInt, String title, String message, String link) {
        if (memberIdInt == null) return;
        notifyMember(Long.valueOf(memberIdInt), title, message, link);
    }

    /* ======================== 常用情境封裝（可選） ======================== */

    /**
     * 有人檢舉某則留言 → 通知所有管理員
     * @param restaurantId 餐廳 ID
     * @param reviewId     留言 ID
     * @param reporterId   檢舉者 ID（可為 null）
     * @param reason       檢舉理由
     */
    @Transactional
    public void notifyAdminsReviewReported(Long restaurantId, Long reviewId, Long reporterId, String reason) {
        // 依你的路由使用 createWebHistory，直接用這個 path 就能進到 SPA 頁面並高亮該留言
        String link = "/restaurants/" + restaurantId + "?reviewId=" + reviewId;

        String title = "留言檢舉通知";
        String message = String.format(
                "餐廳ID：%d（評論ID：%d）%s%s",
                restaurantId, reviewId,
                (reporterId == null ? "" : "，檢舉者ID：" + reporterId),
                (reason == null || reason.isBlank() ? "" : "，理由：" + reason.trim())
        );

        // 這裡沿用你既有的群發管理員方法
        notifyAdmins(title, message, link);
    }

    /**
     * 管理員隱藏留言後 → 通知留言作者
     * @param targetMemberId 留言作者的 memberId（Long）
     * @param restaurantId   餐廳 ID
     * @param reviewId       留言 ID
     * @param reason         隱藏原因（可為 null）
     */
    @Transactional
    public void notifyMemberReviewHidden(Long targetMemberId, Long restaurantId, Long reviewId, String reason) {
        if (targetMemberId == null) return;
        String title = "您的留言已被隱藏";
        String msg = "餐廳ID " + restaurantId + " 的留言ID " + reviewId +
                     " 已被管理員隱藏。原因：" + (reason == null ? "未提供" : reason);
        String link = "/restaurants/" + restaurantId; // 前台餐廳頁（依實際路由調整）
        notifyMember(targetMemberId, title, msg, link);
    }

    /* ======================== 查詢/已讀 ======================== */

    /** 取得某會員的通知列表（新到舊） */
    @Transactional(readOnly = true)
    public List<Notification> listFor(Long memberId) {
        if (memberId == null) return List.of();
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /** 單筆標記已讀（會驗證通知所有者） */
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        if (notificationId == null || memberId == null) return;
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (memberId.equals(n.getMemberId())) {
                n.setRead(true); // 若欄位是 isRead，Java Bean 標準 setter 名稱通常是 setRead(...)
                notificationRepository.save(n);
            }
        });
    }

    /** 將某會員所有通知標記為已讀（常見需求） */
    @Transactional
    public void markAllAsRead(Long memberId) {
        if (memberId == null) return;
        List<Notification> all = notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
        if (all == null || all.isEmpty()) return;
        boolean changed = false;
        for (Notification n : all) {
            if (!Boolean.TRUE.equals(n.isRead())) { // 若實體是 isRead，對應 getter 多半是 getRead()/isRead()
                n.setRead(true);
                changed = true;
            }
        }
        if (changed) {
            notificationRepository.saveAll(all);
        }
    }

    /** 未讀數 */
    @Transactional(readOnly = true)
    public long unreadCount(Long memberId) {
        if (memberId == null) return 0L;
        return notificationRepository.countByMemberIdAndIsReadFalse(memberId);
    }
}
