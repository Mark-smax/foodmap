package com.example.springbootdemo.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CartRepository extends JpaRepository<Cart, Integer> {
	
	@Query("from Cart c where c.users.id = :uid and c.goodphoto.id = :pId")
	Optional<Cart> findUsersAndGoodPhotos(@Param("uid") UUID usersId, @Param("pId") Integer goodphotoId);


//	@Query("from Cart c where c.users.id = ?1")
//	Optional<Cart> findCartsByUsers(UUID usersId);
	
	List<Cart> findByUsersId(UUID usersId);
}
