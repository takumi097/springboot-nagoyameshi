package com.example.nagoyameshi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.Term;
import com.example.nagoyameshi.form.TermEditForm;
import com.example.nagoyameshi.service.TermService;

@Controller
@RequestMapping("/admin/terms")
public class AdminTermController {
	private final TermService termService;
	
	public AdminTermController(TermService termService) {
		this.termService = termService;
	}
	
	//管理者用の利用規約ページを表示
	@GetMapping
	public String index(Model model) {
		Term term = termService.findFirstTermByOrderByIdDesc();
		
		model.addAttribute("term", term);
		return "admin/terms/index";
	}
	
	//管理者余の利用規約編集ページを表示
	@GetMapping("/edit")
	public String edit(Model model) {
		Term term = termService.findFirstTermByOrderByIdDesc();
		
		TermEditForm termEditForm = new TermEditForm(term.getContent());
		
		model.addAttribute("termEditForm", termEditForm);
		
		return "admin/terms/edit";
	}
	
	//フォームから送信された利用規約の内容でデータベースを更新する
	@PostMapping("/update")
	public String update(@ModelAttribute @Validated TermEditForm termEditForm, BindingResult bindingResult,
						RedirectAttributes redirectAttributes, Model model)
	{
		Term term = termService.findFirstTermByOrderByIdDesc();
		
		if (bindingResult.hasErrors()) {
			model.addAttribute("termEditForm", termEditForm);
			
			return "admin/terms/edit";
		}
		
		termService.updateTerm(termEditForm, term);
		redirectAttributes.addFlashAttribute("successMessage", "利用規約を編集しました。");
		
		return "redirect:/admin/terms";
	}
}
