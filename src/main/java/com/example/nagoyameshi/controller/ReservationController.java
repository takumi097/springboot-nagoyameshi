package com.example.nagoyameshi.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.ReservationService;
import com.example.nagoyameshi.service.RestaurantService;

@Controller
public class ReservationController {
	private final ReservationService reservationService;
	private final RestaurantService restaurantService;
	
	public ReservationController(ReservationService reservationService, RestaurantService restaurantService) {
		this.reservationService = reservationService;
		this.restaurantService = restaurantService;
	}
	
	//予約一覧ページ
	@GetMapping("/reservations")
	public String index(@PageableDefault(page = 0, size = 15, sort = "id", direction = Direction.ASC) Pageable pageable,
						RedirectAttributes redirectAttributes, Model model,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) 
	{
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			
			return "redirect:/subscription/register";
		}
		
		Page<Reservation> reservationPage = reservationService.findReservationByUserOrderByReservedDateTimeDesc(user, pageable);
		LocalDateTime currentDateTime = LocalDateTime.now();
		
		model.addAttribute("reservationPage", reservationPage);
		model.addAttribute("currentDateTime", currentDateTime);
		
		return "reservations/index";
	}
	
	//予約ページ
	@GetMapping("/restaurants/{restaurantId}/reservations/register")
	public String register(@PathVariable(name = "restaurantId") Integer restaurantId,
							RedirectAttributes redirectAttributes, 
							@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) 
	{
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			
			return "redirect:/subscription/register";
		}
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/reservations";
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		List<Integer> restaurantRegularHolidays = restaurantService.findDayIndexesByRestaurantId(restaurantId);
		ReservationRegisterForm reservationRegisterForm = new ReservationRegisterForm();
		
		model.addAttribute("restaurant", restaurant);
		model.addAttribute("restaurantRegularholidays", restaurantRegularHolidays);
		model.addAttribute("reservationRegisterForm", reservationRegisterForm);
		
		return "reservations/register";
	}
	
	//フォームから送信された予約情報をデータベースに登録
	@PostMapping("/restaurants/{restaurantId}/reservations/create")
	public String create(@PathVariable(name = "restaurantId") Integer restaurantId, RedirectAttributes redirectAttributes,
						@ModelAttribute @Validated ReservationRegisterForm reservationRegisterForm, BindingResult bindingResult,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) 
	{
		User user = userDetailsImpl.getUser();
		
		if (user.getRole().getName().equals("ROLE_FREE_MEMBER")) {
			redirectAttributes.addFlashAttribute("subscriptionMessage", "この機能を利用するには有料プランへの登録が必要です。");
			
			return "redirect:/reservations";
		}
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(restaurantId);
		
		if (optionalRestaurant.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "店舗が存在しません。");
			
			return "redirect:/reservations";
		}
		
		LocalDate reservationDate = reservationRegisterForm.getReservationDate();
		LocalTime reservationTime = reservationRegisterForm.getReservationTime();
		
		if (reservationDate != null && reservationTime != null) {
			LocalDateTime reservationDaTime = LocalDateTime.of(reservationDate, reservationTime);
			
			if (!reservationService.isAtLeastTwoHoursInFuture(reservationDaTime)) {
				FieldError fieldError = new FieldError(bindingResult.getObjectName(), "reservationTime", "2時間前にお願いします。");
				bindingResult.addError(fieldError);
				
			}
		}
		
		Restaurant restaurant = optionalRestaurant.get();
		
		if (bindingResult.hasErrors()) {
			List<Integer> restaurantRegularHolidays = restaurantService.findDayIndexesByRestaurantId(restaurantId);
			
			model.addAttribute("restaurant", restaurant);
			model.addAttribute("restaurantRegulayHolidays", restaurantRegularHolidays);
			model.addAttribute("reservationRegisterForm", reservationRegisterForm);
			
			return "reservation/register";
		}
		
		reservationService.createReservation(reservationRegisterForm, restaurant, user);
		redirectAttributes.addFlashAttribute("successMessage", "予約が完了しました。");
		
		return "redirect:/reservations";
	}
}
