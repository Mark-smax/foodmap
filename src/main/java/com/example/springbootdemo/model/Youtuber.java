package com.example.springbootdemo.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "youtuber")
public class Youtuber {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;


	private String channelName;


	private Integer subscribe;


	public Youtuber() {
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getChannelName() {
		return channelName;
	}


	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}


	public Integer getSubscribe() {
		return subscribe;
	}


	public void setSubscribe(Integer subscribe) {
		this.subscribe = subscribe;
	}


}
