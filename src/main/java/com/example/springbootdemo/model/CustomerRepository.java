package com.example.springbootdemo.model;


import java.util.List;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;


public interface CustomerRepository extends JpaRepository<Customer, Integer> {


	@Query("from Customer where name = ?1")
	List<Customer> findCustomerByName(String name);
	
	List<Customer> findByName(String name);
	
	@Query(value = "from Customer where name = :n")
	List<Customer> findCustomerByName2(@Param("n") String name);
	
	@Query(value="from Customer where name like %?1%")
	List<Customer> findCustomerByNameLike(String name);
	
	List<Customer> findByNameContaining(String name);
	
	List<Customer> findByNameContainingOrderByIdDesc(String name);
	
	@Query(value="select top(2) * from customer order by id", nativeQuery = true)
	List<Customer> findCustomerNativeQuery();
	
	// Query 也可以搭配 Pageable (方法參數可以放 Pageable)
	@Query("from Customer")
	List<Customer> findCustomerQuery(Pageable pgb);
	
	@Modifying
	@Transactional
	@Query("update Customer set level = ?2 where id = ?1")
	Integer updateLevelByIdQuery(Integer id, Integer level);
	
//	@Modifying
//	@Transactional
//	@Query("update Customer set level = :level where id = :id ")
//	Integer updateLevelByIdQuery(Integer id,Integer level);
}
