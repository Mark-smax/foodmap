package com.example.foodmap.controller;


import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.foodmap.model.Employee;
import com.example.foodmap.service.EmployeeService;

import org.springframework.web.bind.annotation.RequestBody;






@Controller
public class EmployeeController {
	
	@Autowired
	private EmployeeService empService;
	
	@GetMapping("/employee/add")
	public String addEmp() {
		return "employee/addEmpView";
	}
	
	@PostMapping("/employee/addPost")
	public String postEmp(
			@RequestParam("n") String EmpName,
			String email,
			Integer jobAge,
			Model model) {
		
		empService.addEmployee(EmpName, email, jobAge);
		
		model.addAttribute("okMsg", "新增成功");
		
		return "employee/addEmpView";
	}
	
	@GetMapping("/employee/all")
	public String listAllEMP(Model model) {
		
		List<Employee> allEmpList = empService.findAllEmployee();
		
		model.addAttribute("allEmpList", allEmpList);
		
		return "employee/allEmpView";
	}
	
	@GetMapping("/employee/update")
	public String updateEmpID(@RequestParam Integer id, Model model) {
		
		Optional<Employee> op = empService.findEmpById(id);
		
		if(op.isPresent()) {
			model.addAttribute("employee", op.get());
		}else {
			model.addAttribute("errorMsg", "沒有這筆資料");
		}
		
		return "employee/updateEmpView";
	}
	
	@PostMapping("/employee/updatePost")
	public String updateEmpPost(@ModelAttribute Employee employee) {
		
		empService.updateByEmp(employee);

		return "redirect:/employee/all";
	}
	
	@GetMapping("/employee/delete")
	public String deleteById(@RequestParam Integer id) {
		empService.deleteEmployeeById(id);
		return "redirect:/employee/all";
	}
	
	@GetMapping("/employee/update2")
	public String updateEmpID2(@RequestParam Integer id, Model model) {
		
		Optional<Employee> op = empService.findEmpById(id);
		
		if(op.isPresent()) {
			model.addAttribute("employee", op.get());
		}else {
			model.addAttribute("errorMsg", "沒有這筆資料");
		}
		
		return "employee/updateEmpView2";
	}

	
	


}
