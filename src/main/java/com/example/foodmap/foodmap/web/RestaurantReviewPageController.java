package com.example.foodmap.foodmap.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.foodmap.member.domain.enums.MemberRole;


import com.example.foodmap.foodmap.domain.RestaurantReviewService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/review")
public class RestaurantReviewPageController {

    private final RestaurantReviewService reviewService;

    public RestaurantReviewPageController(RestaurantReviewService reviewService) {
        this.reviewService = reviewService;
    }

    private Long getLoginMemberId(HttpSession session) {
        Integer loginMemberId = (Integer) session.getAttribute("loginMemberId");
        return (loginMemberId != null) ? loginMemberId.longValue() : null;
    }

    /**
     * ✅ 管理員隱藏留言
     */
    @ResponseBody
    @PostMapping("/hide")
    public String hideReview(@RequestParam Long reviewId,
                             @RequestParam Long restaurantId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

    	MemberRole role = (MemberRole) session.getAttribute("loginMemberRoles");
    	if (role != MemberRole.ADMIN) {

            redirectAttributes.addFlashAttribute("error", "您沒有權限");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        reviewService.setReviewHidden(reviewId, true); // 將評論設為隱藏
        return "redirect:/restaurant-detail?id=" + restaurantId;
    }
//    @PostMapping("/hide")
//    public String hideReview(@RequestParam Long reviewId,
//    		@RequestParam Long restaurantId,
//    		HttpSession session,
//    		RedirectAttributes redirectAttributes) {
//    	
//    	MemberRole role = (MemberRole) session.getAttribute("loginMemberRoles");
//    	if (role != MemberRole.ADMIN) {
//    		
//    		redirectAttributes.addFlashAttribute("error", "您沒有權限");
//    		return "redirect:/restaurant-detail?id=" + restaurantId;
//    	}
//    	
//    	reviewService.setReviewHidden(reviewId, true); // 將評論設為隱藏
//    	return "redirect:/restaurant-detail?id=" + restaurantId;
//    }
    
    @PostMapping("/unhide")
    public String unhideReview(@RequestParam Long reviewId,
                               @RequestParam Long restaurantId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        MemberRole role = (MemberRole) session.getAttribute("loginMemberRoles");
        if (role != MemberRole.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "您沒有權限");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        reviewService.setReviewHidden(reviewId, false); // ✅ 設定為未隱藏
        return "redirect:/restaurant-detail?id=" + restaurantId;
    }

    /**
     * 🗑️ 刪除評論（只能本人）
     */
    @PostMapping("/delete")
    public String deleteReview(@RequestParam Long reviewId,
                               @RequestParam Long restaurantId,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {
        System.out.println("📣 deleteReview 被呼叫了 reviewId = " + reviewId);

        Long memberId = getLoginMemberId(session);
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        boolean success = reviewService.deleteReviewByIdAndMemberId(reviewId, memberId);
        if (!success) {
            redirectAttributes.addFlashAttribute("error", "刪除失敗，請檢查權限！");
        }

        return "redirect:/restaurant-detail?id=" + restaurantId;
    }

    /**
     * ✏️ 編輯評論（只能本人）
     */
    @PostMapping("/edit")
    public String editReview(@RequestParam Long reviewId,
                             @RequestParam int rating,
                             @RequestParam String comment,
                             @RequestParam Long restaurantId,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) {
        System.out.println("📣 editReview 被呼叫了 reviewId = " + reviewId + ", rating = " + rating + ", comment = " + comment);

        Long memberId = getLoginMemberId(session);
        if (memberId == null) {
            redirectAttributes.addFlashAttribute("error", "請先登入！");
            return "redirect:/restaurant-detail?id=" + restaurantId;
        }

        boolean updated = reviewService.updateReview(reviewId, memberId, rating, comment);
        if (!updated) {
            redirectAttributes.addFlashAttribute("error", "更新失敗，請確認權限或資料！");
        }

        return "redirect:/restaurant-detail?id=" + restaurantId;
    }
}
