package com.example.nagoyameshi.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.service.CategoryService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
@RequestMapping("/restaurants")
public class RestaurantController {
	private final RestaurantService restaurantService;
	private final CategoryService categoryService;
	
	public RestaurantController(RestaurantService restaurantService, CategoryService categoryService) {
		this.restaurantService = restaurantService;
		this.categoryService = categoryService;
	}
	
	@GetMapping
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
										@RequestParam(name = "keyword", required = false) String keyword, 
										@RequestParam(name = "categoryId", required = false) Integer categoryId, 
										@RequestParam(name = "price", required = false) Integer price, 
										@RequestParam(name = "order", required = false) String order,
										Model model) 
	{
		Page<Restaurant> restaurantPage;
		List<Category> categories = categoryService.findAllByCategories();
		
		if (keyword != null && !keyword.isEmpty()) {
			if (order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByLowestPriceAsc(keyword, keyword, keyword, pageable);
			} else if (order != null && order.equals("ratingDesc")) {
				restaurantPage = restaurantService.findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByAverageScoreDesc(keyword, keyword, keyword, pageable);
			} else {
				restaurantPage = restaurantService.findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(keyword, keyword, keyword, pageable);
			}
		} else if (categoryId != null) {
			if (order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantByCategoryIdOrderByLowestPriceAsc(categoryId, pageable);
			} else if (order != null && order.equals("ratingDesc")) {
				restaurantPage = restaurantService.findRestaurantByCategoryIdOrderByAverageScoreDesc(categoryId, pageable);
			} else {
				restaurantPage = restaurantService.findRestaurantByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
				}
		} else if (price != null) {
			if (order != null && order.equals("lowestPriceASC")) {
				restaurantPage = restaurantService.findRestaurantByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
			} else if (order != null && order.equals("ratingDesc")) {
				restaurantPage = restaurantService.findRestaurantByLowestPriceLessThanEqualOrderByAverageScoreDesc(price, pageable);
			} else {
				restaurantPage = restaurantService.findRestaurantByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		} else {
			if (order != null && !order.equals("lowestPriceASC")) {
				restaurantPage = restaurantService.findAllRestaurantByOrderByLowestPriceAsc(pageable);
			} else if (order != null && order.equals("ratingDesc")) {
				restaurantPage = restaurantService.findAllRestaurantByOrderByAverageScoreDesc(pageable);
			} else {
				restaurantPage = restaurantService.findAllRestaurantsByOrderByCreatedAtDesc(pageable);
			}
		}
		
		model.addAttribute("restaurantPage", restaurantPage);
		model.addAttribute("categories", categories);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		model.addAttribute("price", price);
		model.addAttribute("order", order);
		
		return "restaurants/index";
	}
	
	@GetMapping("/{id}")
	public String show(@PathVariable(name = "id") Integer id, RedirectAttributes redirectAttributes, Model model) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(id);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect://restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		model.addAttribute("restaurant", restaurant);
		
		return "restaurants/show";
	}
}
