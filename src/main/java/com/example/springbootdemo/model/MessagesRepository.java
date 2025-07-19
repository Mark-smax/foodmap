package com.example.springbootdemo.model;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface MessagesRepository extends JpaRepository<Messages, Integer> {

	@Query("from Messages")
	Page<Messages> findLatestMsg(Pageable pgb);


}
