package com.example.nagoyameshi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.nagoyameshi.entity.Company;

public interface CompanyRepository extends JpaRepository<Company, Integer>{

	//idが最も大きい会社概要を取得する（idを基準に降順で並べ替え、最初の1剣を取得する）
	public Company findFirstByOrderByIdDesc();
}
