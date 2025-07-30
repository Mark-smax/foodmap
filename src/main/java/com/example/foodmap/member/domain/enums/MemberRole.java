package com.example.foodmap.member.domain.enums;

public enum MemberRole {
	// 管理員
	ADMIN("管理員"),
	// 一般會員
    USER("一般會員"),
    // 商家
    MERCHANT("商家"),
    // 供應商
    SUPPLIER("供應商");
	
	private final String displayName;

	MemberRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
