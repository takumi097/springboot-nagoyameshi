package com.example.nagoyameshi.controller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReviewEditForm;
import com.example.nagoyameshi.form.ReviewRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.RestaurantService;
import com.example.nagoyameshi.service.ReviewService;

@Controller
@RequestMapping("/restaurants/{restaurantId}/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final RestaurantService restaurantService;
	
	public ReviewController(ReviewService reviewService, RestaurantService restaurantService) {
		this.reviewService = reviewService;
		this.restaurantService = restaurantService;
	}
	
	//レビュー一覧ページ
	@GetMapping
	public String index(@PathVariable(name = "restaurantId") Integer restaurantId,
						@PageableDefault(page = 0, size = 5, sort = "id", direction = Direction.ASC) Pageable pageable,
						RedirectAttributes redirectAttributes, Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません");
			
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		User user = userDetailsImpl.getUser();
		String userRoleName = user.getRole().getName();
		
		Page<Review> reviewPage;
		
		if (userRoleName.equals("ROLE_PAID_MEMBER")) {
			reviewPage = reviewService.findReviewByRestaurantOrderByCreatedAtDesc(restaurant, pageable);
		} else {
			reviewPage = reviewService.findReviewByRestaurantOrderByCreatedAtDesc(restaurant, PageRequest.of(0, 3));
		}
		
		boolean hasUserAlreadyReviewed = reviewService.hasUserAlreadyReviewed(restaurant, user);
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("userRoleName", userRoleName);
		model.addAttribute("reviewPage", reviewPage);
		model.addAttribute("hasUserAlreadyReviewed", hasUserAlreadyReviewed);
	
		return "reviews/index";
	}
	
	//レビュー投稿ページ
	@GetMapping("/register")
	public String register(@PathVariable(name = "restaurantId") Integer restaurantId, RedirectAttributes redirectAttributes,
							@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/restaurants";
		}
		
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			
			return "redirect:/subscription/register";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		ReviewRegisterForm reviewRegisterForm = new ReviewRegisterForm();
		reviewRegisterForm.setScore(5);
	
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("reviewRegisterForm", reviewRegisterForm);
		
		return "reviews/register";
	}
	
	//フォームから送信さっれたレビューをデータベースに登録する
	@PostMapping("/create")
	public String create(@PathVariable(name = "restaurantId") Integer restaurantId, 
						@ModelAttribute @Validated ReviewRegisterForm reviewRegisterForm, BindingResult bindingResult,
						RedirectAttributes redirectAttributes, Model model,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/restaurants";
		}
		
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへ登録が必要です。");
			
			return "redirect:/subscription/register";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		
		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("restaurant", restaurant);
			redirectAttributes.addFlashAttribute("reviewRegisterForm", reviewRegisterForm);
			
			return "redirect:/restaurants";
		}
		
		reviewService.createReview(reviewRegisterForm, restaurant, user);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを投稿しました。");
		
		return "redirect:/restaurants/{restaurantId}";
	}
	
	//レビュー編集ページ
	@GetMapping("/{reviewId}/edit")
	public String edit(@PathVariable(name = "restaurantId") Integer restaurantId,
						@PathVariable(name = "reviewId") Integer reviewId, RedirectAttributes redirectAttributes,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) 
	{
		User user = userDetailsImpl.getUser();
	
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			
			return "redirect:/subscription/register";
		}
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		
		if (optionalRestaurant.isEmpty() && optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");
		
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		Review review = optionalReview.get();
		
		if (!review.getRestaurant().getId().equals(restaurantId) || !review.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("不正なアクセスです。");
			
			return "redirect:/restaurants/{restaurantId}";
		}
		
		ReviewEditForm reviewEditForm = new ReviewEditForm(review.getScore(), review.getContent());
			
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("review", review);
		model.addAttribute("reviewEditForm", reviewEditForm);
		
		return "reviews/edit";
	}
	
	//フォームから送信されたレビューでデータベースに更新する
	@PostMapping("/{reviewId}/update")
	public String update(@PathVariable(name = "restauarntId") Integer restaurantId,
						@PathVariable(name = "reviewId") Integer reviewId, RedirectAttributes redirectAttributes,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, BindingResult bindingResult,
						@ModelAttribute @Validated ReviewEditForm reviewEditForm) 
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するときは有料プランへの登録が必要です。");
			
			return "redirect:/subscription/register";
		}
		
		if (optionalRestaurant.isEmpty() && optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("指定されたページが見つかりません。");
			
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		Review review = optionalReview.get();
		
		if (!review.getRestaurant().getId().equals(restaurantId) || !review.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			
			return "redirect:/restaurants/{restaurantId}";
		}
		
		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute("restaurant", restaurant);
			redirectAttributes.addFlashAttribute("review", review);
			redirectAttributes.addFlashAttribute("reviewEditForm", reviewEditForm);
			
			return "redirect:/restaurants";
		}
		
		reviewService.updateReview(reviewEditForm, review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを編集しました。");
		
		return "redirect:/restaurants/{restaurantId}";
	}
	
	//レビューをデータベースから削除する
	public String delete(@PathVariable(name = "restaurantId") Integer restaurantId,
						@PathVariable(name = "reviewId") Integer reviewId, RedirectAttributes redirectAttributes,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) 
	{
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		Optional<Review> optionalReview = reviewService.findReviewById(reviewId);
		
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			
			return "redirect:/restaurants";
		}
		
		if (optionalRestaurant.isEmpty() && optionalReview.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "指定されたページが見つかりません。");
			
			return "redirect:/restaurants";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		Review review = optionalReview.get();
		
		if (!review.getRestaurant().getId().equals(restaurantId) || !review.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "不正なアクセスです。");
			
			return "redirect:/restaurants/{restaurantId}";
		}
		
		reviewService.deleteReview(review);
		redirectAttributes.addFlashAttribute("successMessage", "レビューを削除しました。");
		
		return "redirect:/restaurants/{restaurantId}";
	}
}
