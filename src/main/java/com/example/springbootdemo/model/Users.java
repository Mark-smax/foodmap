package com.example.springbootdemo.model;


import java.util.UUID;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table
public class Users {


	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;


	@Column(unique = true, nullable = false)
	private String username;


	@Column(nullable = false)
	private String password;


	@PrimaryKeyJoinColumn // 這個屬性(usersDetail) 對應的 FK, 是我們這邊(Users)的 PK
	@OneToOne(fetch = FetchType.LAZY)
	private UsersDetail usersDetail;


}
