package com.example.nagoyameshi.controller;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.service.CategoryRestaurantService;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/admin/restaurants")
public class AdminRestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	private final CategoryRestaurantService categoryRestaurantService;
	
	public AdminRestaurantController(RestaurantService restaurantService, CategoryService categoryService,
									CategoryRestaurantService categoryRestaurantService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
		this.categoryRestaurantService = categoryRestaurantService;
	}
	
	//店舗一覧ページ
	@GetMapping
	public String index(@RequestParam(name = "keyword", required = false) String keyword,
			@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
			Model model) 
	{
		Page<Restaurant> restaurantPage;
		
		//キーワードが存在する場合は検索結果を、存在しない場合は全件取得
		if (keyword != null && !keyword.isEmpty()) {
			restaurantPage = restaurantService.findRestaurantByNameLike(keyword, pageable);
		} else {
			restaurantPage = restaurantService.findAllRestaurants(pageable);
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("keyword", keyword);
		
		return "admin/restaurants/index";
	}
	
	//店舗詳細ページ
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id,	//	URLからIdを取得
					RedirectAttributes redirectAttributes, 	//リダイレクト先のフラッシュメッセージ
					Model model) {
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "指定した店舗が存在しません。");
			
			return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		model.addAttribute("restaurant", restaurant);
		
		return "admin/restaurants/show";
	}
	
	//店舗登録
	@GetMapping("/register")
	public String register(Model model) {
		List<Category> categories = categoryService.findAllByCategories();
		
		model.addAttribute("restaurantRegisterForm", new RestaurantRegisterForm());
		model.addAttribute("categories", categories);
		
		return "admin/restaurants/register";
	}
	
	//フォームから送信された店舗情報をデータベースに登録
	@PostMapping("/create")
	public String create(@ModelAttribute @Validated RestaurantRegisterForm restaurantRegisterForm,	//フォームから送信されたデータを受け取る
			BindingResult bindingResult,	//バリデーションの結果を格納
			RedirectAttributes redirectAttributes,	//リダイレクト
			Model model) 
	{
		
		//最高価格と最低価格を取得
		Integer lowestPrice = restaurantRegisterForm.getLowestPrice();
		Integer highestPrice = restaurantRegisterForm.getHighestPrice();
		
		//開店時間と閉店時間を取得
		LocalTime openingTime = restaurantRegisterForm.getOpeningTime();
		LocalTime closingTime = restaurantRegisterForm.getClosingTime();
		
		//最高価格が最低価格より低く設定していないか
		if (lowestPrice != null && highestPrice != null && !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
			FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最低価格は最高価格よりも低く設定してください。");
			FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最高価格は最低価格よりも高く設定してください。");
			bindingResult.addError(lowestPriceError);
			bindingResult.addError(highestPriceError);		}
		
		//開店時間が閉店時間よりも前に設定しているか
		if (openingTime != null && closingTime != null && !restaurantService.isValidBusinessHours(openingTime, closingTime)) {
			FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間が閉店時間よりも前に設定してくいださい。");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間よりも後に設定してください。");
			bindingResult.addError(openingTimeError);
			bindingResult.addError(closingTimeError);		}
		
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryService.findAllByCategories();
	           model.addAttribute("restaurantRegisterForm", restaurantRegisterForm);
	           model.addAttribute("categories", categories);

	           return "admin/restaurants/register";
	       }
		
		 restaurantService.createRestaurant(restaurantRegisterForm);
	     redirectAttributes.addFlashAttribute("successMessage", "店舗を登録しました。");

	       return "redirect:/admin/restaurants";
	}
	
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable(name = "id") Integer id,
					RedirectAttributes redirectAttributes,
					Model model) 
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
	
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
		return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		List<Integer> categoryIds = categoryRestaurantService.findCategoryIdsByRestaurantOrderByIdAsc(restaurant);
		RestaurantEditForm restaurantEditForm = new RestaurantEditForm(restaurant.getName(), null, restaurant.getDescription(),
																		restaurant.getLowestPrice(), restaurant.getHighestPrice(),
																		restaurant.getPostalCode(), restaurant.getAddress(),
																		restaurant.getOpeningTime(), restaurant.getClosingTime(),
																		restaurant.getSeatingCapacity(), categoryIds);
		
		List<Category> categories = categoryService.findAllByCategories();
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantEditForm", restaurantEditForm);
		model.addAttribute("categories", categories);
		
		return "admin/restaurants/edit";
	}
	
	@PostMapping("/{id}/update")
	public String update(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, Model model,
						@ModelAttribute @Validated RestaurantEditForm restaurantEditForm, BindingResult bindingResult) 
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/admin/restaurants";
		}
		
		//最高価格と最低価格を取得
		Integer lowestPrice = restaurantEditForm.getLowestPrice();
		Integer highestPrice = restaurantEditForm.getHighestPrice();
				
		//開店時間と閉店時間を取得
		LocalTime openingTime = restaurantEditForm.getOpeningTime();
		LocalTime closingTime = restaurantEditForm.getClosingTime();
				
		//最高価格が最低価格より低く設定していないか
		if (lowestPrice != null && highestPrice != null && !restaurantService.isValidPrices(lowestPrice, highestPrice)) {
			FieldError lowestPriceError = new FieldError(bindingResult.getObjectName(), "lowestPrice", "最低価格は最高価格よりも低く設定してください。");
			FieldError highestPriceError = new FieldError(bindingResult.getObjectName(), "highestPrice", "最高価格は最低価格よりも高く設定してください。");
			bindingResult.addError(lowestPriceError);
			bindingResult.addError(highestPriceError);		}
				
		//開店時間が閉店時間よりも前に設定しているか
		if (openingTime != null && closingTime != null && !restaurantService.isValidBusinessHours(openingTime, closingTime)) {
			FieldError openingTimeError = new FieldError(bindingResult.getObjectName(), "openingTime", "開店時間が閉店時間よりも前に設定してくいださい。");
			FieldError closingTimeError = new FieldError(bindingResult.getObjectName(), "closingTime", "閉店時間は開店時間よりも後に設定してください。");
			bindingResult.addError(openingTimeError);
			bindingResult.addError(closingTimeError);		}
		
		Restaurant restaurant = optionalRestaurant.get();	
		
		if (bindingResult.hasErrors()) {
			List<Category> categories = categoryService.findAllByCategories();
	       model.addAttribute("restaurant", restaurant);
	       model.addAttribute("restaurantEditForm", restaurantEditForm);
	       model.addAttribute("categories", categories);
	       
	       return "admin/restaurants/edit";
	    }
		
		restaurantService.updateRestaurant(restaurantEditForm, restaurant);
	    redirectAttributes.addFlashAttribute("successMessage", "店舗を編集しました。");

	    return "redirect:/admin/restaurants";
	}
	
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/admin/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		
		restaurantService.deleteRestaurant(restaurant);
		redirectAttributes.addFlashAttribute("successMessage", "店舗を削除しました。");
		
		return "redirect:/admin/restaurants";
	}
}
