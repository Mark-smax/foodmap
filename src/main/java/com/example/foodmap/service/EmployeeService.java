package com.example.foodmap.service;


import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.foodmap.model.Employee;
import com.example.foodmap.model.EmployeeRepository;


@Service
public class EmployeeService {
	
	@Autowired
	private EmployeeRepository empRepo;
	
	public Employee addEmployee(String name, String email, Integer jobAge) {
		
		Employee emp = new Employee();
		emp.setName(name);
		emp.setEmail(email);
		emp.setJobAge(jobAge);
		
		return  empRepo.save(emp);	
	}
	
	public List<Employee> findAllEmployee(){
		return empRepo.findAll();
	}
	
	public Optional<Employee> findEmpById(Integer id) {
		return empRepo.findById(id);
	}
	
	public Employee updateByEmp(Employee employee) {
		// savrOrUpdate()
		//有 id 做 Update
		//沒有 id,做 insert into
		
		return empRepo.save(employee); 
	}
	
	public void deleteEmployeeById(Integer id) {
		empRepo.deleteById(id);
	}

}

