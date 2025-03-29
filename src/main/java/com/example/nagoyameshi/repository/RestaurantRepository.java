package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	//店舗名で店舗を検索し、ページングられた状態で取得する
	public Page<Restaurant> findByNameLike(String keyword, Pageable pageable);
	
	//id最も大きい店舗を取得する。最初の１件
	public Restaurant findFirstByOrderByIdDesc();
	
	//すべての店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得する
	public Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable);
	
	//すべての店舗を最低価格が安い順に並び替え
	public Page<Restaurant> findAllByOrderByLowestPriceAsc(Pageable pageable);
	
	//指定したキーワードを店舗名または住所またはカテゴリ名に含む店舗を作成日時が新しい順に並び替えページングされた状態で取得する
	@Query("SELECT DISTINCT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"ORDER BY r.createdAt DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(@Param("name") String nameKeyword, 
																						@Param("address") String addressKeyword,
																						@Param("categoryName") String categoryNameKeyword,
																						Pageable pageable);
	
	//指定したキーワードを店舗名または住所またはカテゴリ名に含む店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT DISTINCT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"ORDER BY r.lowestPrice ASC ")
	public Page <Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(@Param("name") String nameKeyword, 
																							@Param("address") String addressKeyword,
																							@Param("categoryName") String categoryNameKeyword,
																							Pageable pageable);
	
	//指定したidのカテゴリが設定された店舗を作成日時が新しい順に並べ替え、ページングされえた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"WHERE cr.category.id = :categoryId " +
			"ORDER BY r.createdAt DESC ")
	public Page<Restaurant> findByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Integer categoryId, Pageable pageable);
	
	//指定されたidのカテゴリが設定された店舗を最低価格が安い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"WHERE cr.category.id = :categoryId " +
			"ORDER BY r.lowestPrice ASC ")
	public Page<Restaurant> findByCategoryIdOrderByLowestPriceAsc(@Param("categoryId") Integer categoryId, Pageable pageable);
	
	//指定された最低価格以下の店舗を作成日時が新しい順に並べ替え、ページングされた状態で取得
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByCreatedAtDesc(Integer price, Pageable pageable);
	
	//指定された最低価格以下の店舗を最低価格が安い順に並べ替え、ページングされた状態で取得
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByLowestPriceAsc(Integer price, Pageable pageable);
	
	//すべての店舗を平均評価が高い順に並べ替え、ページングされた状態で取得
	@Query("SELECT r FROM Restaurant r LEFT JOIN r.reviews rv GROUP BY r.id ORDER BY AVG(rv.score) DESC")
	public Page<Restaurant> findAllByOrderByAverageScoreDesc(Pageable pageable);
	
	//指定されたキーラードを店舗名または住所またはカテゴリ名に含む店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
	 @Query("SELECT r FROM Restaurant r " +
	           "LEFT JOIN r.categoriesRestaurants cr " +
	           "LEFT JOIN r.reviews rev " +
	           "WHERE r.name LIKE %:name% " +
	           "OR r.address LIKE %:address% " +
	           "OR cr.category.name LIKE %:categoryName% " +
	           "GROUP BY r.id " +
	           "ORDER BY AVG(rev.score) DESC")
	    public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(@Param("name") String nameKeyword,
	                                                                                                 @Param("address") String addressKeyword,
	                                                                                                 @Param("categoryName") String categoryNameKeyword,
	                                                                                                 Pageable pageable);
	
	//指定されたidのカテゴリが設定された店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"LEFT JOIN r.reviews rv " +
			"WHERE cr.category.id = :categoryId " +
			"GROUP BY r.id " +
			"ORDER BY AVG(rv.score) DESC")
	public Page<Restaurant> findByCategoryIdOrderByAverageScoreDesc(@Param("categoryId") Integer categoryId, Pageable pageable);
	
	//指定された最低価格以下の店舗を平均評価が高い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
	           "LEFT JOIN r.reviews rev " +
	           "WHERE r.lowestPrice <= :price " +
	           "GROUP BY r.id " +
	           "ORDER BY AVG(rev.score) DESC")
	    public Page<Restaurant> findByLowestPriceLessThanEqualOrderByAverageScoreDesc(@Param("price") Integer price, Pageable pageable);
	
	//すべての店舗を予約数が多い順に並べ替えページング絵された状態で取得
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reservations rv " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(rv) DESC")
	public Page<Restaurant> findAllByOrderByReservationCountDesc(Pageable pageable);
	
	//指定されたキーワードを店舗名または住所またはカテゴリ名に含み店舗を予約数が多い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.categoriesRestaurants cr" +
			"LEDT JOIN r.reservations rv " +
			"WHERE r.name LIKE %:name% " +
			"OR r.address LIKE %:address% " +
			"OR cr.category.name LIKE %:categoryName% " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(DISTINCT rv.id) DESC")
	public Page<Restaurant> findByNameLikeOrAddressLikeOrCategoryNameLikeOrderByReservationCountDesc(@Param("name") String nameKeyword,
																								@Param("address") String addressKeyword,
																								@Param("categoryName") String categoryNameKeyword,
																								Pageable pageable);
	
	//指定されたidのカテゴリが設定された店舗を予約数が多い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"INNER JOIN r.categoriesRestaurants cr " +
			"LEFT JOIN r.reservation rv " +
			"WHERE cr.category.id = :categoryId " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(rv) DESC")
	public Page<Restaurant> findByCategoryIdOrderByReservationCountDesc(@Param("categoryId") Integer categoryId, Pageable pageable);
	
	//指定された最低価格の店舗を予約数が多い順に並べ替え、ページングされた状態で取得する
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN r.reservations rv " +
			"WHERE r.lowestPrice <= :price " +
			"GROUP BY r.id " +
			"ORDER BY COUNT(rv) DESC")
	public Page<Restaurant> findByLowestPriceLessThanEqualOrderByReservationCountDesc(@Param("price") Integer price, Pageable pageable);
	
	//指定された店舗の定休日のday_indexフィールドの値をリストで取得する
	@Query("SELECT rh.dayIndex FROM RegularHoliday rh " +
			"INNER JOIN rh.regularHolidaysRestaurants rhr " +
			"INNER JOIN rhr.restaurant r " +
			"WHERE r.id = :restaurantId")
	public List<Integer> findDayIndexesByRestaurantId(@Param("restaurantId") Integer restaurantId);
}
