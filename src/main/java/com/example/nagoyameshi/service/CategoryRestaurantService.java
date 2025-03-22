package com.example.nagoyameshi.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;

@Service
public class CategoryRestaurantService {
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final CategoryService categoryService;
	
	public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository, CategoryService categoryService) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.categoryService = categoryService;
	}
	
	//指定した店舗のカテゴリidをCategoryRestaurantエンティティのidが小さい順に並べ替えられた状態のリスト形式で取得
	public List<Integer> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}
	
	//フォームから送信されたカテゴリのidリストをもとに、category_restaurantテーブルデータにでーたを登録する
	@Transactional
	public void createCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {
		
		//カテゴリのidのリストの要素数だけ繰り返し処理
		for (Integer categoryId : categoryIds) {
			if (categoryId != null) {
				//カテゴリidと一致するcategoryエンティティを取得
				Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);
				
				//カテゴリエンティティが存在すれば
				if (optionalCategory.isPresent()) {
					//カテゴリオブジェクトを取り出す
					Category category = optionalCategory.get();
					
					//店舗とcategoryエンティティが紐づいたcategoryRestaurantエンティティを取得する
					Optional<CategoryRestaurant> optionalCurrentCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(category, restaurant);
					
					//categoryRestaurantエンティティが存在しなければ新しく	categoryRestaurantを保存する
					if (optionalCurrentCategoryRestaurant.isEmpty()) {		
						CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
						categoryRestaurant.setRestaurant(restaurant);
						categoryRestaurant.setCategory(category);
						
						categoryRestaurantRepository.save(categoryRestaurant);
					}
				}
			}
		}
	}
	
	//フォームから送信されたカテゴリのidリストをもとに、category_restaurantsテーブルのデータを同期する
	@Transactional
	public void syncCategoriesRestaurants(List<Integer> newCategoryIds, Restaurant restaurant) {
		//登録されているrestaurantに紐づくカテゴリを取得
		List<CategoryRestaurant> currentCategoriesRestaurants = categoryRestaurantRepository.findByRestaurantOrderByIdAsc(restaurant);
		
		//newCategoryIdsはフォームから送信された新しいカテゴリ
		if (newCategoryIds == null) {		//フォームから送信されたカテゴリがnullの場合
			for (CategoryRestaurant currentCategoryRestaurant : currentCategoriesRestaurants) {		//保存されていたカテゴリを取得
				categoryRestaurantRepository.delete(currentCategoryRestaurant);		//カテゴリの関連をすべて削除
			}
		} else {		//フォームから送信されたカテゴリがnullではなく新しいカテゴリの場合
			//保存されていたカテゴリを取得
			for (CategoryRestaurant currentCategoryRestaurant : currentCategoriesRestaurants) {
				//取り出したカテゴリが新しいカテゴリリストに含まれていないか検証
				if (!newCategoryIds.contains(currentCategoryRestaurant.getCategory().getId())) {
					//そのカテゴリの関連を削除する
					categoryRestaurantRepository.delete(currentCategoryRestaurant);
				}
			}
			
			//フォームから送信されたカテゴリリストを
			for (Integer newCategoryId : newCategoryIds) {
				if (newCategoryId != null) {
					//新しく送信されたカテゴリをデータベースから取得
					Optional<Category> optionalCategory = categoryService.findCategoryById(newCategoryId);
				
					if (optionalCategory.isPresent()) {
						Category category = optionalCategory.get();
					
						//すでに登録されているか確認
						Optional<CategoryRestaurant> optionalCurrentCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(category, restaurant);
					
						if (optionalCurrentCategoryRestaurant.isEmpty()) {
							CategoryRestaurant categoryRestaurant = new CategoryRestaurant();
							categoryRestaurant.setRestaurant(restaurant);
							categoryRestaurant.setCategory(category);

							categoryRestaurantRepository.save(categoryRestaurant);
						}
					}
				}
			}
		}
	}
}
