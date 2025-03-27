package com.example.nagoyameshi.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.RestaurantRepository;

@Service
public class RestaurantService {
		private final RestaurantRepository restaurantRepository;
		private final CategoryRestaurantService categoryRestaurantService;
		private final RegularHolidayRestaurantService regularHolidayRestaurantService;
		
		public RestaurantService(RestaurantRepository restaurantRepository, CategoryRestaurantService categoryRestaurantService,
								RegularHolidayRestaurantService regularHolidayRestaurantService) {
			this.restaurantRepository = restaurantRepository;
			this.categoryRestaurantService = categoryRestaurantService;
			this.regularHolidayRestaurantService = regularHolidayRestaurantService;
		}
		
		//すべての店舗をページングされた状態で取得する
		public Page<Restaurant> findAllRestaurants(Pageable pageable) {
			return restaurantRepository.findAll(pageable);
		}
		
		//指定されたキーワードを店舗名に含む店舗をページングされた状態で取得する
		public Page <Restaurant> findRestaurantByNameLike(String keyword, Pageable pageable) {
			return restaurantRepository.findByNameLike(keyword, pageable);
		}
		
		//指定されたidを持つ店舗取得する
		public Optional<Restaurant> findRestaurantById(Integer id) {
			return restaurantRepository.findById(id);
		}
		
		//店舗のレコード数を取得する
		public long countRestaurants() {
			return restaurantRepository.count();
		}
		
		//idが最も大きい店舗を取得する
		public Restaurant findFirstRestaurantByOrderByIdDesc() {
			return restaurantRepository.findFirstByOrderByIdDesc();
		}
		
		//すべての店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得
		public Page<Restaurant> findAllRestaurantsByOrderByCreatedAtDesc(Pageable pageable) {
			return restaurantRepository.findAllByOrderByCreatedAtDesc(pageable);
		}
		
		//すべての店舗を最低価格が安い順に並べ替え、ページングされた状態で取得
		public Page<Restaurant> findAllRestaurantByOrderByLowestPriceAsc(Pageable pageable) {
			return restaurantRepository.findAllByOrderByLowestPriceAsc(pageable);
		}
		
		//指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得
		public Page<Restaurant> findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(String nameKeyword,
																												String addressKeyword,
																												String categoryNameKeyword,
																												Pageable pageable) {
			return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(nameKeyword, addressKeyword,
																											categoryNameKeyword, pageable);
		}
		
		//指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を最低価格が安い順に並べ替え、ページングされた状態で取得
		public Page<Restaurant> findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(String nameKeyword, 
																											String addressKeyword,
																											String categoryNameKeyword,
																											Pageable pageable) {
			return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(nameKeyword, addressKeyword,
																										categoryNameKeyword, pageable);
		}
		
		//指定されたidのかてごりが設定された店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得
		public Page<Restaurant> findRestaurantByCategoryIdOrderByCreatedAtDesc(Integer categoryId, Pageable pageable) {
			return restaurantRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
		}
		
		//指定されたidのカテゴリが設定された店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
		public Page<Restaurant> findRestaurantByCategoryIdOrderByLowestPriceAsc(Integer categoryId, Pageable pageable) {
			return restaurantRepository.findByCategoryIdOrderByLowestPriceAsc(categoryId, pageable);
		}
		
		//指定された最低価格以下の店舗を作成日時が新しい順に並べ替え、ページングされた状態で」取得する
		public Page<Restaurant> findRestaurantByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable) {
			return restaurantRepository.findByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
		}
		
		//指定された最低価格以下の店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
		public Page<Restaurant> findRestaurantByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer price, Pageable pageable) {
			return restaurantRepository.findByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
		}
		
		//店舗の登録
		 @Transactional
		 public void createRestaurant(RestaurantRegisterForm restaurantRegisterForm) {
		    Restaurant restaurant = new Restaurant();
		    MultipartFile imageFile = restaurantRegisterForm.getImageFile();
		    List<Integer> categoryIds = restaurantRegisterForm.getCategoryIds();
		    List<Integer> regularHolidayIds = restaurantRegisterForm.getRegularHolidayIds();
			
			//画像のファイル名
			if (!imageFile.isEmpty()) {
				String imageName = imageFile.getOriginalFilename();	//元のファイル名を取得
				String hashedImageName = generateNewFileName(imageName);	//ファイル名をUUIDを使って別名に変更
				Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
				copyImageFile(imageFile, filePath);
				restaurant.setImage(hashedImageName);
			}
			
			restaurant.setName(restaurantRegisterForm.getName());
	        restaurant.setDescription(restaurantRegisterForm.getDescription());
	        restaurant.setLowestPrice(restaurantRegisterForm.getLowestPrice());
	        restaurant.setHighestPrice(restaurantRegisterForm.getHighestPrice());
	        restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
	        restaurant.setAddress(restaurantRegisterForm.getAddress());
	        restaurant.setOpeningTime(restaurantRegisterForm.getOpeningTime());
	        restaurant.setClosingTime(restaurantRegisterForm.getClosingTime());
	        restaurant.setSeatingCapacity(restaurantRegisterForm.getSeatingCapacity());
			
	        restaurantRepository.save(restaurant);
			
			if (categoryIds != null) {
				categoryRestaurantService.createCategoriesRestaurants(categoryIds, restaurant);
			}
			
			if (regularHolidayIds != null) {
				regularHolidayRestaurantService.createRegularHolidaysRestaurants(regularHolidayIds, restaurant);
			}
		}
		
		//UUIDを使って生成したファイル名を返す
		public String generateNewFileName(String fileName) {
			String[] fileNames = fileName.split("\\.");
			
			for (int i = 0; i < fileNames.length -1; i++) {
				fileNames[i] = UUID.randomUUID().toString();
			}
			
			String hashedFileName = String.join(".", fileNames);
			
			return hashedFileName;
		}
		
		//画像ファイルを指定したファイルをコピーする
		public void copyImageFile(MultipartFile imageFile, Path filePath) {
			try {
				Files.copy(imageFile.getInputStream(), filePath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		//店舗の編集
		@Transactional
		public void updateRestaurant(RestaurantEditForm restaurantEditForm, Restaurant restaurant) {
			MultipartFile imageFile = restaurantEditForm.getImageFile();
			List<Integer> categoryIds = restaurantEditForm.getCategoryIds();
			List<Integer> regularHolidayIds = restaurantEditForm.getRegularHolidayIds();
			
			if (!imageFile.isEmpty()) {
				String imageName = imageFile.getOriginalFilename();
				String hashedImageName = generateNewFileName(imageName);
				Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
				copyImageFile(imageFile, filePath);
				restaurant.setImage(hashedImageName);			
				}
			
			restaurant.setName(restaurantEditForm.getName());
			restaurant.setDescription(restaurantEditForm.getDescription());
			restaurant.setLowestPrice(restaurantEditForm.getLowestPrice());
			restaurant.setHighestPrice(restaurantEditForm.getHighestPrice());
			restaurant.setPostalCode(restaurantEditForm.getPostalCode());
			restaurant.setAddress(restaurantEditForm.getAddress());
			restaurant.setOpeningTime(restaurantEditForm.getOpeningTime());
			restaurant.setClosingTime(restaurantEditForm.getClosingTime());
			restaurant.setSeatingCapacity(restaurantEditForm.getSeatingCapacity());
			
			restaurantRepository.save(restaurant);
			
			categoryRestaurantService.syncCategoriesRestaurants(categoryIds, restaurant);
			regularHolidayRestaurantService.syncRegularHolidaysRestaurants(regularHolidayIds, restaurant);
		}
		
		//店舗の削除
		@Transactional
		public void deleteRestaurant(Restaurant restaurant) {
			restaurantRepository.delete(restaurant);
		}
		
		//価格が正しく設定されているか確認	
		public boolean isValidPrices(Integer lowestPrice, Integer highestPrice) {
			return highestPrice >= lowestPrice;
		}
		
		//閉店時間が開店時間よりも後がどうか
		public boolean isValidBusinessHours(LocalTime openingTime, LocalTime closingTime) {
			return closingTime.isAfter(openingTime);
		}
		
		//すべての店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
		public Page<Restaurant> findAllRestaurantByOrderByAverageScoreDesc(Pageable pageable) {
			return restaurantRepository.findAllByOrderByAverageScoreDesc(pageable);
		}
		
		//指定されたキーワードを店舗名または住所またはカテゴリ名に含む店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
		public Page<Restaurant> findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(String nameKeyword, 
																												String addressKeyword, 
																												String categoryNameKeyword, 
																												Pageable pageable) 
		{
			return restaurantRepository.findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(nameKeyword, addressKeyword, categoryNameKeyword, pageable);
		}
		
		//指定されたidのカテゴリが設定された連保を平均評価が高い順に並べ替え、ページングされた状態で取得する
		public Page<Restaurant> findRestaurantByCategoryIdOrderByAverageScoreDesc(Integer categoryId, Pageable pageable) {
			return restaurantRepository.findByCategoryIdOrderByAverageScoreDesc(categoryId, pageable);
		}
		
		//指定された最低価格以下の店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
		public Page<Restaurant> findRestaurantByLowestPriceLessThanEqualOrderByAverageScoreDesc(Integer price, Pageable pageable) {
			return restaurantRepository.findByLowestPriceLessThanEqualOrderByAverageScoreDesc(price, pageable);
		}
}
