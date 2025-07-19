package com.example.springbootdemo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@JoinColumn(name = "users_id",nullable =false)
	@ManyToOne(fetch = FetchType.LAZY)
	private Users users;
	
	@JoinColumn(name = "goodphoto_id",nullable =false)
	@ManyToOne(fetch = FetchType.LAZY)
	private GoodPhoto goodphoto;
	
	@Column(nullable = false)
	private Integer vol;
	
	public Cart() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Users getUsers() {
		return users;
	}

	public void setUsers(Users users) {
		this.users = users;
	}

	public GoodPhoto getGoodphoto() {
		return goodphoto;
	}

	public void setGoodphoto(GoodPhoto goodphoto) {
		this.goodphoto = goodphoto;
	}

	public Integer getVol() {
		return vol;
	}

	public void setVol(Integer vol) {
		this.vol = vol;
	}

}
