package com.example.nagoyameshi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer>{
	
	//カテゴリ名でカテゴリを検索し、ページングされた状態で取得する
	public Page<Category> findByNameLike(String keyword, Pageable pageable);
	
	//idが最も大きいカテゴリを取得する
	public Category findFirstByOrderByIdDesc();
	
	//指定したカテゴリ名を持つ最初のカテゴリを取得する
	public Category findFirstByName(String name); 
}
