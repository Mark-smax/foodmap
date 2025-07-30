package com.example.foodmap.member.domain.enums;

public enum MemberStatus {
	// 正常
	ACTIVE("正常"),
	// 審核中
	PENDING("審核中"),
	// 刪除
	DELETED("刪除"),
	// 停權
	SUSPENDED("停權"),
	// 拒絕該申請帳號
	REJECTED("已拒絕");
	
	private final String displayName;

    MemberStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
    
}
