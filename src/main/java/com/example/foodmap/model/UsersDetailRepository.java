package com.example.foodmap.model;

import java.util.UUID;


import org.springframework.data.jpa.repository.JpaRepository;


public interface UsersDetailRepository extends JpaRepository<UsersDetail, UUID> {


}
