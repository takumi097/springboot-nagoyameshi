package com.example.nagoyameshi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserService;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.Subscription;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {
	@Value("${stripe.premium-plan-price-id}")
	private String premiumPlanPriceId;
	
	private final StripeService stripeService;
	private final UserService userService;
	
	public SubscriptionController(StripeService stripeService, UserService userService) {
		this.stripeService = stripeService;
		this.userService = userService;
	}
	
	//有料プランの登録ページ
	@GetMapping("/register")
	public String register() {
		return "subscription/register";
	}
	
	/*現在ログイン中のユーザーを顧客都市て作成し、フォームから送信されたクレジットカード情報をデフォルトの支払い方法として設定する
	また、顧客のサブスクリプションを作成しロールを更新する*/
	@PostMapping("/create")
	public String create(@RequestParam(name = "paymentMethodId") String paymentMethodId, RedirectAttributes redirectAttributes,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		
		if (user.getStripeCustomerId() == null) {
			try {
				//顧客の（Customerオブジェクト)を作成
				Customer customer = stripeService.createCustomer(user);
				
				//stripeCustomerIdフィールドに顧客Idを保存
				userService.saveStripeCustomerId(user, customer.getId());
			} catch (StripeException e) {
				redirectAttributes.addFlashAttribute("errorMessage", "有料プランへの登録に失敗しました。");
				
				return "redirect:/";
			}
		}
		
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			//フォームから送信された支払い方法（PaymentMethodオブジェクト）を顧客に紐づける
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			
			//フォームから送信された支払い方法を顧客のデフォルトの支払いに設定する。
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			
			//サブスクリプション（Subscriptionオブジェクト）を作成
			stripeService.createSubscription(stripeCustomerId, premiumPlanPriceId);
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランへ登録に失敗しました。");
			
			return "redirect:/";
		}
		
		//ユーザーのロールを更新する
		userService.updateRole(user, "ROLE_PAID_MEMBER");
		userService.refreshAuthenticationByRole("ROLE_PAID_MEMBER");
		
		redirectAttributes.addFlashAttribute("successMessage", "有料プランへ登録が完了しました。");
		
		return "redirect:/";
	}
	
	//お支払い方法の編集ページを表示する
	@GetMapping("/edit")
	public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, RedirectAttributes redirectAttributes, Model model) {
		User user = userDetailsImpl.getUser();
		
		try {
			//顧客のデフォルト支払い方法（PPymentMethodオブジェク）を取得する
			PaymentMethod paymentMethod = stripeService.getDefaultPaymentMethod(user.getStripeCustomerId());
			
			model.addAttribute("card", paymentMethod.getCard());
			model.addAttribute("cardHolderName", paymentMethod.getBillingDetails().getName());
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "お支払方法を取得できませんでした。");
		
			return "redirect:/";
		}
		
		return "subscription/edit";
	}
	
	//顧客の絵フォルト支払い方法を更新する
	@PostMapping("/update")
	public String update(@RequestParam String paymentMethodId, RedirectAttributes redirectAttributes,
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		String stripeCustomerId = user.getStripeCustomerId();
		
		try {
			//現在のデフォルトの支払い方法（PaymentMethodオブジェクト）のIdを取得
			String currentDefaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(stripeCustomerId);
			
			//フォームから送信された支払い方法を顧客（Customerオブジェクト）に紐づける
			stripeService.attachPaymentMethodToCustomer(paymentMethodId, stripeCustomerId);
			
			//フォームから送信された支払い方法を顧客（Customerオブジェクト）に紐づける
			stripeService.setDefaultPaymentMethod(paymentMethodId, stripeCustomerId);
			
			//以前のデフォルトの支払い方法と顧客を紐付ける
			stripeService.detachPaymentMethodFromCustomer(currentDefaultPaymentMethodId);		
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "推し八頼方法の変更に失敗しました。");
			
			return "redirect:/";
		}
		
		redirectAttributes.addFlashAttribute("successMessage", "お支払方法を変更しました。");
		
		return "redirect:/";
	}
	
	//有料プラン契約ページを表示する
	@GetMapping("/cancel")
	public String cancel() {
		return "subscription/cancel";
	}
	
	/*顧客のサブスクリプションをキャンセルし、デフォルトの支払い方法と顧客の紐づけを解除する
	また、ロールを更新する*/
	@PostMapping("/delete")
	public String delete(RedirectAttributes redirectAttributes, 
						@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
		User user = userDetailsImpl.getUser();
		
		try {
			//顧客が契約中のサブスクリプションを取得
			List<Subscription> subscriptions = stripeService.getSubscriptions(user.getStripeCustomerId());
			
			//顧客が契約中のサブスクリプションをキャンセルする
			stripeService.cancelSubscriptions(subscriptions);
			
			//デフォルトの支払い方法のIDを取得する
			String defaultPaymentMethodId = stripeService.getDefaultPaymentMethodId(user.getStripeCustomerId());
			
			//デフォルト支払い方法と顧客を紐づけを解除する
			stripeService.detachPaymentMethodFromCustomer(defaultPaymentMethodId);
			
		} catch (StripeException e) {
			redirectAttributes.addFlashAttribute("errorMessage", "有料プランの解約に失敗しました。再度お試しください。");
			
			return "redirect:/";
		}
		
		userService.updateRole(user, "ROLE_FREE_MEMBER");
		userService.refreshAuthenticationByRole("ROLE_FREE_MEMBER");
		
		redirectAttributes.addFlashAttribute("successMessage", "有料プランを解約しました。");
		
		return "redirect:/";
	}
			
}
