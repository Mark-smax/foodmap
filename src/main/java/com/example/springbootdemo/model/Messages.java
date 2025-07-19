package com.example.springbootdemo.model;

import java.sql.Timestamp;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "messages")
public class Messages {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String text;

	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 前端對應的時間格式，要搭配雙層${{time}}
	@Temporal(TemporalType.TIMESTAMP)
	private Date createdAt;

	@JoinColumn(name = "fk_users_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Users users;

	@PrePersist // 當物件要轉移到 Persistent (Managed)狀態以前，先做這件事
	public void onCreate() {
		if (createdAt == null) {
			createdAt = new Date();
		}
	}

	public Messages() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

}
