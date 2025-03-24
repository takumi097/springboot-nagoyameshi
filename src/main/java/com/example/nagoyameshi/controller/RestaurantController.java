package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
			} else {
				restaurantPage = restaurantService.findRestaurantByNameLikeOrAddressLikeOrCategoryNameLikeOrderByCreatedAtDesc(keyword, keyword, keyword, pageable);
			}
		} else if (categoryId != null) {
			if (order != null && order.equals("lowestPriceAsc")) {
				restaurantPage = restaurantService.findRestaurantByCategoryIdOrderByLowestPriceAsc(categoryId, pageable);
			} else {
				restaurantPage = restaurantService.findRestaurantByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
				}
		} else if (price != null) {
			if (order != null && order.equals("lowestPriceASC")) {
				restaurantPage = restaurantService.findRestaurantByLowestPriceLessThanEqualOrderByLowestPriceAsc(price, pageable);
			} else {
				restaurantPage = restaurantService.findRestaurantByLowestPriceLessThanEqualOrderByCreatedAtDesc(price, pageable);
			}
		} else {
			if (order != null && !order.equals("lowestPriceASC")) {
				restaurantPage = restaurantService.findAllRestaurantByOrderByLowestPriceAsc(pageable);
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
}
