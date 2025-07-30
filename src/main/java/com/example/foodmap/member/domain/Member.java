package com.example.foodmap.member.domain;

import com.example.foodmap.member.domain.enums.MemberRole;
import com.example.foodmap.member.domain.enums.MemberStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer memberId;

	@Column(nullable = false)
	private String memberName;

	private String memberNickName;

	@Column(nullable = false)
	private String memberEmail;

	@Column(nullable = false)
	private String memberPassword;

	@Lob
	private byte[] memberPhoto;

	// 會員身費(管理員、一般、商家、供應商)
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MemberRole memberRole;

	// 會員狀態(正常、審核中、刪除、停權)
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private MemberStatus memberStatus;

//	以下為各自關聯表所需欄位
//	請注意getters與setters是否有註解
//	@OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
//	private List<Post> posts = new ArrayList<>();

	public Member() {
	}

	public Integer getMemberId() {
		return memberId;
	}

	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getMemberNickName() {
		return memberNickName;
	}

	public void setMemberNickName(String memberNickName) {
		this.memberNickName = memberNickName;
	}

	public String getMemberEmail() {
		return memberEmail;
	}

	public void setMemberEmail(String memberEmail) {
		this.memberEmail = memberEmail;
	}

	public String getMemberPassword() {
		return memberPassword;
	}

	public void setMemberPassword(String memberPassword) {
		this.memberPassword = memberPassword;
	}

	public byte[] getMemberPhoto() {
		return memberPhoto;
	}

	public void setMemberPhoto(byte[] memberPhoto) {
		this.memberPhoto = memberPhoto;
	}

	public MemberRole getMemberRole() {
		return memberRole;
	}

	public void setMemberRole(MemberRole memberRole) {
		this.memberRole = memberRole;
	}

	public MemberStatus getMemberStatus() {
		return memberStatus;
	}

	public void setMemberStatus(MemberStatus memberStatus) {
		this.memberStatus = memberStatus;
	}

//	public List<Post> getPosts() {
//		return posts;
//	}
//
//	public void setPosts(List<Post> posts) {
//		this.posts = posts;
//	}

}
