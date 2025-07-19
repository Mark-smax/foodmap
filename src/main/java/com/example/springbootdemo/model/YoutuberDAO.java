package com.example.springbootdemo.model;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class YoutuberDAO {

	@PersistenceContext
	private EntityManager em;

	@Transactional // AOP 設計模式
	public void insert(String name, Integer subscrib) {

		Youtuber ytr1 = new Youtuber();
		ytr1.setChannelName(name);
		ytr1.setSubscribe(1340000);

		em.persist(ytr1);
	}

	@Transactional(readOnly = true)
	public Youtuber findYtrById(Integer id) {
		return em.find(Youtuber.class, id);

	}
	@Transactional
	public Youtuber updateCount(Integer id) {
		Youtuber ytr = em.find(Youtuber.class, id);	
		ytr.setSubscribe(ytr.getSubscribe()+1);
		return ytr;
		}
	
	@Transactional
	public String deleteChannel(Integer id) {
		Youtuber ytr = em.find(Youtuber.class, id);
		
		if(ytr != null) {
			em.remove(ytr);
			return "有資料，且刪除";
		}
		
		return "沒資料~沒刪除到";
		
	}

	
}
