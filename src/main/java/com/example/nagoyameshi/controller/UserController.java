package com.example.nagoyameshi.controller;

import java.time.format.DateTimeFormatter;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {
	private final UserService userService;
	
	public UserController(UserService userService) {
		this.userService = userService;
	}
	
	//会員用の会員情報ページを表示する
	@GetMapping
	public String index(Model model, @AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		model.addAttribute("user", user);
		
		return "/user/index";
	}
	
	//会員用の会員情報編集ページを表示する
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {
		User user = userDetailsImpl.getUser();
		String dateTimeBirthday = null;
		
		if (user.getBirthday() != null) {
			dateTimeBirthday = user.getBirthday().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		} 
		
		UserEditForm userEditForm = new UserEditForm(user.getName(), user.getFurigana(), user.getPostalCode(),
										user.getAddress(), user.getPhoneNumber(), dateTimeBirthday,
										user.getOccupation(), user.getEmail());
		
		model.addAttribute("userEditForm", userEditForm);
		
		return "user/edit";
	}
	
	//フォームから送信された会員情報でデータベースを更新する
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult,
						RedirectAttributes redirectAttributes, Model model,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		
		if (userService.isEmailChanged(userEditForm, user) && userService.isEmailRegistered(userEditForm.getEmail())) {
			FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済のメールアドレスです。");
			bindingResult.addError(fieldError);
		}
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("userEditForm", userEditForm);
			
			return "user/edit";
		}
		
		userService.updateUser(userEditForm, user);
		redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
		
		return "redirect:/user";
	}
}
