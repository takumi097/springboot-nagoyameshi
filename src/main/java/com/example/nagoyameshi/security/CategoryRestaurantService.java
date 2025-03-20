package com.example.nagoyameshi.security;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.CategoryRestaurant;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.repository.CategoryRestaurantRepository;
import com.example.nagoyameshi.service.CategoryService;

@Service
public class CategoryRestaurantService {
	private final CategoryRestaurantRepository categoryRestaurantRepository;
	private final CategoryService categoryService;
	
	public CategoryRestaurantService(CategoryRestaurantRepository categoryRestaurantRepository, CategoryService categoryService) {
		this.categoryRestaurantRepository = categoryRestaurantRepository;
		this.categoryService = categoryService;
	}
	
	//指定した店舗のカテゴリidをCategoryRestaurantエンティティのidが小さい順に並べ替えられた状態のリスト形式で取得
	public List<CategoryRestaurant> findCategoryIdsByRestaurantOrderByIdAsc(Restaurant restaurant) {
		return categoryRestaurantRepository.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
	}
	
	//フォームから送信されたカテゴリのidリストをもとに、category_restaurantテーブルデータにでーたを登録する
	public void createCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {

		for (Integer categoryId : categoryIds) {	//カテゴリのidのリストの要素数だけ繰り返し処理
			if (categoryIds != null) {
				Optional<Category> optionalCategory = categoryService.findCategoryById(categoryId);	//カテゴリidと一致するcategoryエンティティを取得
				
				if (optionalCategory.isPresent()) {	//カテゴリエンティティが存在すれば
					Category category = optionalCategory.get();	//カテゴリオブジェクトを取り出す
					
					//店舗とcategoryエンティティが紐づいたcategoryRestaurantエンティティを取得する
					Optional<CategoryRestaurant> optionalCurrentCategoryRestaurant = categoryRestaurantRepository.findByCategoryAndRestaurant(restaurant, category);
					
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
	public void syncCategoriesRestaurants(List<Integer> categoryIds, Restaurant restaurant) {
		
	}
}
