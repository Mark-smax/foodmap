package com.example.foodmap.model;


import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


@Entity
@Table
public class AttractionsPhoto {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;


	@Lob
	private byte[] photoFile;


	@JoinColumn(name = "fk_attractions_id")
	@ManyToOne(fetch = FetchType.LAZY)
	private Attractions attractions;


	public AttractionsPhoto() {
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public byte[] getPhotoFile() {
		return photoFile;
	}


	public void setPhotoFile(byte[] photoFile) {
		this.photoFile = photoFile;
	}


	public Attractions getAttractions() {
		return attractions;
	}


	public void setAttractions(Attractions attractions) {
		this.attractions = attractions;
	}


}
