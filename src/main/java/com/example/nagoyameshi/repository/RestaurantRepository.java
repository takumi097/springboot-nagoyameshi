package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	//店舗名で店舗を検索し、ページングられた状態で取得する
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);
	
	//id最も大きい店舗を取得する。最初の１件
	public Restaurant findFirstByOrderByIdDesc();
}
