package com.example.springbootdemo.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table
public class Attractions {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;


	private String name;


	private Integer star;


	public Attractions() {
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Integer getStar() {
		return star;
	}


	public void setStar(Integer star) {
		this.star = star;
	}


}
