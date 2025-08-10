package com.example.foodmap.foodmap.domain;

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

    /** 通知所有管理員 */
    @Transactional
    public void notifyAdmins(String title, String message, String link) {
        List<Member> admins = memberRepository.findByMemberRole(MemberRole.ADMIN);
        for (Member admin : admins) {
            Notification n = new Notification();
            // Member 的主鍵是 Integer，所以轉成 Long
            n.setMemberId(admin.getMemberId() == null ? null : Long.valueOf(admin.getMemberId()));
            n.setTitle(title);
            n.setMessage(message);
            n.setLink(link);
            notificationRepository.save(n);
        }
    }

    /** 通知單一會員（商家/一般） */
    @Transactional
    public void notifyMember(Long memberId, String title, String message, String link) {
        Notification n = new Notification();
        n.setMemberId(memberId);
        n.setTitle(title);
        n.setMessage(message);
        n.setLink(link);
        notificationRepository.save(n);
    }

    /** 取得某會員的通知列表 */
    public List<Notification> listFor(Long memberId) {
        return notificationRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /** 標記已讀 */
    @Transactional
    public void markAsRead(Long notificationId, Long memberId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getMemberId().equals(memberId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    /** 未讀數 */
    public long unreadCount(Long memberId) {
        return notificationRepository.countByMemberIdAndIsReadFalse(memberId);
    }
}
