package com.example.foodmap.service;


import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.foodmap.model.Messages;
import com.example.foodmap.model.MessagesRepository;
import com.example.foodmap.model.Users;
import com.example.foodmap.model.UsersRepository;


@Service
public class MessagesService {
	
	@Autowired
	private MessagesRepository msgRepo;
	
	@Autowired
	private UsersRepository useresRepo;
	
	public Messages addMessages(String inputText) {
		Messages msg = new Messages();
		msg.setText(inputText);
		return msgRepo.save(msg);
	}
	
	public Messages addMessagesWithUsers(UUID loginUserId, String inputText) {
		
		Users loginUsers = useresRepo.findById(loginUserId).get();
		
		Messages msg = new Messages();
		msg.setText(inputText);
		msg.setUsers(loginUsers);
		
		return msgRepo.save(msg);
	}
	
	public Optional<Messages> findMessagesById(Integer id) {
		return  msgRepo.findById(id);
	}
	
	@Transactional
	public Optional<Messages> updateTextById(Integer id, String newText) {
		Optional<Messages> op = msgRepo.findById(id);
		
		if(op.isPresent()) {
			Messages msg = op.get();
			msg.setText(newText);
			return op;
		}
		
		return op;
	}
	
	public void deleteMessagesById(Integer id) {
		msgRepo.deleteById(id);
	}
	
	public Messages findLatestMessages() {
		
		Pageable pgb = PageRequest.of(0, 1, Sort.Direction.DESC, "createdAt");
		
		Page<Messages> page = msgRepo.findLatestMsg(pgb);
		
		List<Messages> result = page.getContent();
		
		if(result.size() != 0) {
			return result.get(0);
		}
		
		return null;
	}
	
	public Page<Messages> findMessagesByPage(Integer pageNumber){
		Pageable pgb = PageRequest.of(pageNumber-1, 3, Sort.Direction.DESC, "createdAt");
		
		Page<Messages> page = msgRepo.findAll(pgb);
		
		return page;
	}
	
	
	


}
