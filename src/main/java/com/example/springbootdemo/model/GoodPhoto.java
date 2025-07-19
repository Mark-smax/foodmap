package com.example.springbootdemo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;


@Entity
@Table
public class GoodPhoto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String photoName;
	@Lob
	private byte[] photoFile;
	
	public GoodPhoto() {
		
		
		
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public byte[] getPhotoFile() {
		return photoFile;
	}

	public void setPhotoFile(byte[] photoFile) {
		this.photoFile = photoFile;
	}

}
