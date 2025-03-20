package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Category;
import com.example.nagoyameshi.service.CategoryService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminCategoryControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private CategoryService categoryService;
	
	@Test
	public void 未ログインの場合は管理者用のカテゴリ一覧ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/categories"))
		 		.andExpect(status().is3xxRedirection())
		 		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用のカテゴリ一覧ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/categories"))
			 	.andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用のカテゴリ一覧ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/categories"))
		 		.andExpect(status().isOk())
		 		.andExpect(view().name("admin/categories/index"));
	}
	
	@Test
	@Transactional
	public void 未ログインの場合はカテゴリを登録せずにログインページにリダイレクトする() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/create")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 一般ユーザーとしてログイン済みの場合はカテゴリを登録せずに403エラーが発生する() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/create")
				.with(csrf())
				.param("name", "テスト"))
		 		.andExpect(status().isForbidden());
		
		
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済の場合はカテゴリ登録後にカテゴリ一覧にリダイレクトする() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/create")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/categories"));
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore + 1);
		
		Category category = categoryService.findFirstCategoryByOrderByIdDesc();
		assertThat(category.getName()).isEqualTo("テスト");
	}
	
	@Test
	@Transactional
	public void 未ログインの場合はカテゴリを更新せずにログインページにリダイレクトする() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/1/update")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 一般ユーザーとしてログイン済の場合はカテゴリを更新せずに403エラーが発生する() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/1/update")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().isForbidden());
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はカテゴリ更新後にカテゴリ一覧にリダイレクトする() throws Exception {
		mockMvc.perform(post("/admin/categories/1/update")
				.with(csrf())
				.param("name", "テスト"))
		 		.andExpect(status().is3xxRedirection())
		 		.andExpect(redirectedUrl("/admin/categories"));
		
		Optional<Category> optionalCategory = categoryService.findCategoryById(1);
		assertThat(optionalCategory).isPresent();
		Category category = optionalCategory.get();
		
		assertThat(category.getName()).isEqualTo("テスト");
	}
	
	@Test
	@Transactional
	public void 未ログイン済みの場合はカテゴリを削除せずにログインページにリダイレクトする() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/1/delete")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 一般ユーザーとしてログイン済みの場合はカテゴリを削除せずに403エラーが発生する() throws Exception {
		long countBefore = categoryService.countCategories();
		
		mockMvc.perform(post("/admin/categories/1/delete")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().isForbidden());
		
		long countAfter = categoryService.countCategories();
		
		assertThat(countAfter).isEqualTo(countBefore);
		}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はカテゴリを削除後にカテゴリ一覧にリダイレクトする() throws Exception {
		mockMvc.perform(post("/admin/categories/1/delete")
				.with(csrf())
				.param("name", "テスト"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/admin/categories"));
		
		Optional<Category> optionalCategory = categoryService.findCategoryById(1);
		assertThat(optionalCategory).isEmpty();
	}
}
