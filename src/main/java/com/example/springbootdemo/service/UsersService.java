package com.example.springbootdemo.service;


import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;


import com.example.springbootdemo.model.Users;
import com.example.springbootdemo.model.UsersDetail;
import com.example.springbootdemo.model.UsersDetailRepository;
import com.example.springbootdemo.model.UsersRepository;


@Service
public class UsersService {


	@Autowired
	private UsersRepository usersRepo;
	
	@Autowired
	private UsersDetailRepository usersDetailRepo;


	@Autowired
	private PasswordEncoder pwdEncoder;


	public Users registerUser(String username, String password) {
		String encodedPwd = pwdEncoder.encode(password);
		Users users = new Users();
		users.setUsername(username);
		users.setPassword(encodedPwd);


		return usersRepo.save(users);
	}


	public Users findUsersByName(String username) {
		return usersRepo.findByUsername(username);
	}


	public Users checkLogin(String username, String inputPwd) {
		Users users = usersRepo.findByUsername(username);


		if (users == null) {
			return null;
		} else {
			boolean result = pwdEncoder.matches(inputPwd, users.getPassword());


			if (result) {
				return users;
			}
		}


		return null;
	}
	
	@Transactional
	public UsersDetail saveOrUpdateUsersDetail(UUID loginUserId, String address, String phone, String cardNumber) {
		Users users = usersRepo.findById(loginUserId).get();
		
		UsersDetail dbUsersDetail = users.getUsersDetail();
		
		if(dbUsersDetail == null) {
			// 若 detail 還沒寫，新增一筆一對一 User 的資料
			UsersDetail newDetail = new UsersDetail();
			newDetail.setAddress(address);
			newDetail.setPhone(phone);
			newDetail.setCardNumber(cardNumber);
			newDetail.setUsers(users);
			return usersDetailRepo.save(newDetail);
		}else {
			// 如果本來有 detail 資料，做 update 資料
			dbUsersDetail.setAddress(address);
			dbUsersDetail.setPhone(phone);
			dbUsersDetail.setCardNumber(cardNumber);
		}
		
		return dbUsersDetail;
	}


}
