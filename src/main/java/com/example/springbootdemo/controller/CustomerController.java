package com.example.springbootdemo.controller;


import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RestController;


import com.example.springbootdemo.model.Customer;
import com.example.springbootdemo.model.CustomerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
public class CustomerController {
	
	// 若有介面，應優先注入介面
	@Autowired
	private CustomerRepository customerRepository;


	@GetMapping("/customer/add")
	public Customer addCustomer() {
		
		Customer cus = new Customer();
		cus.setName("Mary");
		cus.setLevel(5);
		
		return customerRepository.save(cus);	
	}
	
	@GetMapping("/customer/addmany")
	public List<Customer> testAddMany() {
		
		Customer cus1 = new Customer();
		cus1.setName("Tom");
		cus1.setLevel(3);
		
		Customer cus2 = new Customer();
		cus2.setName("Tina");
		cus2.setLevel(4);
		
		Customer cus3 = new Customer();
		cus3.setName("Mark");
		cus3.setLevel(1);
		
		List<Customer> customerList = new LinkedList<>();
		customerList.add(cus1);
		customerList.add(cus2);
		customerList.add(cus3);
		
		return customerRepository.saveAll(customerList);
	}
	
	@GetMapping("/customer")
	public Customer testFindById(@RequestParam Integer id) {
		Optional<Customer> op = customerRepository.findById(id);
		
		if(op.isPresent()) {
			return op.get();
		}
		
		return null;	
	}
	
	
	@GetMapping("/customer/all")
	public List<Customer> testFindAll() {
		return customerRepository.findAll();
	}
	
	
	@GetMapping("/customer/delete")
	public String testDeleteById(@RequestParam Integer id) {
		customerRepository.deleteById(id);
		
		return "那筆資料被刪除了";
	}
	
//	http://localhost:8080/customer/page/6
	@GetMapping("/customer/page/{p}")
	public Page<Customer> getMethodName(@PathVariable("p") Integer pageNumber) {
		
		Pageable pgb = PageRequest.of(pageNumber-1, 2, Sort.Direction.ASC, "id");
		
		Page<Customer> page = customerRepository.findAll(pgb);
		
		return page;
	}
	
	@GetMapping("/customer/query1")
	public List<Customer> testQuery1(@RequestParam String name) {
//		return customerRepository.findCustomerByName(name);
//		return customerRepository.findCustomerByName2(name);
//		return customerRepository.findCustomerByNameLike(name);
//		return customerRepository.findByName(name);
//		return customerRepository.findByNameContaining(name);
		return customerRepository.findByNameContainingOrderByIdDesc(name);
		
	}
	
	@GetMapping("/customer/query2")
	public List<Customer> testNativeQuery() {
		return customerRepository.findCustomerNativeQuery();
	}
	
	
	@GetMapping("/customer/query3")
	public List<Customer> testPageableQuery() {
		Pageable pgb = PageRequest.of(0, 2, Sort.Direction.ASC, "id");
		
		return customerRepository.findCustomerQuery(pgb);	
	}
	
	
	// /customer/testUpdate?id=xxx&lv=yyy
	@GetMapping("/customer/testUpdate")
	public Integer testUpdateQuery(@RequestParam Integer id, @RequestParam("lv") Integer level) {
		return customerRepository.updateLevelByIdQuery(id, level);
	}
	
	
	
	
}
