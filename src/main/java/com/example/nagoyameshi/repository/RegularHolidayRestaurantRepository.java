package com.example.nagoyameshi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.RegularHoliday;
import com.example.nagoyameshi.entity.RegularHolidayRestaurant;
import com.example.nagoyameshi.entity.Restaurant;

public interface RegularHolidayRestaurantRepository extends JpaRepository<RegularHolidayRestaurant, Integer>{

	//指定した店舗の定休日のidをリスト形式で取得する
	@Query("SELECT rr.regularHoliday.id FROM RegularHolidayRestaurant rr WHERE rr.restaurant = :restaurant")
	public List<Integer> findRegularHolidayIdsByRestaurant(@Param("restaurant") Restaurant restaurant);
	
	//指定した店舗と定休日が紐づいたRegularHolidayRestaurantエンティティを取得する
	public Optional<RegularHolidayRestaurant> findByRegularHolidayAndRestaurant(RegularHoliday regularHoliday, Restaurant Restautant);
	
	//指定した店舗に紐づくRegularHolidayRestaurantエンティティをリスト形式で取得する
	public List<RegularHolidayRestaurant> findByRestaurant(Restaurant restaurant);
}
