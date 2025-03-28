package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Review;
import com.example.nagoyameshi.service.ReviewService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReviewControllerTest {
	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	ReviewService reviewService;
	
	@Test
	public void 未ログインの場合はレビュー一覧ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員賭してログイン済みの場合はレビュー一覧が正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
			 	.andExpect(status().isOk())
			 	.andExpect(view().name("reviews/index"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員でログイン済みの場合はレビュー一覧ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
		 		.andExpect(status().isOk())
		 		.andExpect(view().name("reviews/index"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー一覧ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	public void 未ログインの場合はレビュー投稿ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
		 		.andExpect(status().is3xxRedirection())
		 		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー投稿ページから有料プラン登録ページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
			 	.andExpect(status().is3xxRedirection())
			 	.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合はレビュー投稿ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
		 		.andExpect(status().isOk())
		 		.andExpect(view().name("reviews/register"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー投稿ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/register"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@Transactional
	public void 未ログインの場合はレビューを登録せずにログインページにリダイレクトする() throws Exception {
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create")
				.with(csrf())
				.param("score", "3")
				.param("content", "テストコメント"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
		
		long countAfter = reviewService.countReviews();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 無料会員としてログイン済みの場合はレビューを投稿せずに有料プラン登録ページにリダイレクトする() throws Exception {
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create")
				.with(csrf())
				.param("score", "3")
				.param("content", "テストコメント"))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/subscription/register"));
		
		long countAfter = reviewService.countReviews();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	@Transactional
	public void 有料会員としてログイン済みの場合はレビュー投稿後に店舗詳細ページにリダイレクトする() throws Exception {
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create")
				.with(csrf())
				.param("score", "3")
				.param("content", "テストコメント"))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/restaurants/1"));
		
		long countAfter = reviewService.countReviews();
		
		assertThat(countAfter).isEqualTo(countBefore + 1);
		
		Review review = reviewService.findFirstReviewsByOrderByIdDesc();
		assertThat(review.getScore()).isEqualTo(3);
		assertThat(review.getContent()).isEqualTo("テストコメント");
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合はレビューを投稿せずに403エラーが発生する() throws Exception {
		long countBefore = reviewService.countReviews();
		
		mockMvc.perform(post("/restaurants/1/reviews/create")
				.with(csrf())
				.param("score", "3")
				.param("content", "テストコメント"))
		.andExpect(status().isForbidden());
				
		long countAfter = reviewService.countReviews();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	public void 未ログインの場合はレビュー編集ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/1/reviews/1/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 無料会員としてログイン済みの場合はレビュー編集ページから有料プラン登録ページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/2/reviews/1/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/subscription/register"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合は自身のレビュー編集ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/restaurants/2/reviews/1/edit"))
				.andExpect(status().isOk())
				.andExpect(view().name("reviews/edit"));
	}
	
	@Test
	@WithUserDetails("jiro.samurai@example.com")
	public void 有料会員としてログイン済みの場合は他人のレビュー編集ページから店舗詳細ページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/restaurants/2/reviews/2/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/restaurants/2"));
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合はレビュー編集ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/restaurants/2/revviews/1/edit"))
				.andExpect(status().isForbidden());
	}
}
