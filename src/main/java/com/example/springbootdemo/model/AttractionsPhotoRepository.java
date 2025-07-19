package com.example.springbootdemo.model;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;



public interface AttractionsPhotoRepository extends JpaRepository<AttractionsPhoto, Integer> {

@Query("select id from AttractionsPhoto att where att.attractions.id =?1 ")	
List<Integer> findAttrPhotosByAttrId(Integer attrId);
	
}
