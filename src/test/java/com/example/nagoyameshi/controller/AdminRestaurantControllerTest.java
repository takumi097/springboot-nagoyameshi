package com.example.nagoyameshi.controller;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.service.RestaurantService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminRestaurantControllerTest {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private RestaurantService restaurantService;
	
	@Test
	public void 未ログインの場合は管理者用の店舗一覧ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/restaurants"))
		 		.andExpect(status().is3xxRedirection())
		 		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用の店舗一覧ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/restaurants"))
				.andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の店舗一覧ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/restaurants"))
			 	.andExpect(status().isOk())
			 	.andExpect(view().name("admin/restaurants/index"));
	}
	
	@Test
	public void 未ログイン済みの場合は管理者用の店舗詳細ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/restaurants/1"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用の店舗詳細ページに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/restaurants/1"))
			 	.andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の店舗詳細ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/restaurants/1"))
				.andExpect(status().isOk())
				.andExpect(view().name("admin/restaurants/show"));
	}
	
	@Test
	public void 未ログインの場合は管理者用の店舗登録ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/restaurants/register"))
		 		.andExpect(status().is3xxRedirection())
		 		.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済みの場合は管理者用の店舗登録ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/restaurants/register"))
		 		.andExpect(status().isForbidden());
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の店舗登録ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/restaurants/register"))
		.andExpect(status().isOk())
		.andExpect(view().name("admin/restaurants/register"));
	}
	
	@Test
	@Transactional
	public void 未ログインの場合は店舗を登録せずにログインページにリダイレクトする() throws Exception {
		//テスト前のレコード数を取得する
		long countBefore = restaurantService.countRestaurants();
		
		//テスト用の画像ファイルを準備
		Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);
		
		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",	//フォームのname属性の値
				fileName,	//ファイル名
				fileType,	//ファイルの形式
				fileBytes	//ファイルのバイト配列
				);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/create").file(imageFile)
				.with(csrf())
				.param("name", "テスト店舗")
				.param("description", "テスト説明")
				.param("lowestParice", "1000")
				.param("highestParice", "5000")
				.param("postalCode", "0000000")
				.param("address", "テスト住所")
				.param("openingTime", "9:00")
				.param("closingTime", "17:00")
				.param("seatingCapacity", "10"))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("http://localhost/login"));
		
		//テスト後のレコード数を取得する
		long countAfter = restaurantService.countRestaurants();
		
		//レコード数が変わっていないことを検証する
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 一般ユーザーとしてログイン済みの場合は店舗を登録せずに403エラーが発生する() throws Exception {
		
		long countBefore = restaurantService.countRestaurants();
		
		Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);
		
		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes
				);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/create").file(imageFile)
				.with(csrf())
				.param("name", "テスト店舗名")
				.param("description", "テスト説明")
				.param("lowestPrice", "1000")
				.param("highestPrice", "5000")
				.param("postalCode", "0000000")
				.param("address", "テスト住所")
				.param("openingTime", "9:00")
				.param("clodingTime", "10:00")
				.param("seatingCapacity", "10"))
		.andExpect(status().isForbidden());
		
		long countAfter = restaurantService.countRestaurants();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者として老院炭の場合は店舗登録後に店舗一覧にリダイレクトする() throws Exception {
		
		long countBefore = restaurantService.countRestaurants();
		
		Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);
		
		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes
				);
				
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/create").file(imageFile)
				.with(csrf())
				.param("name", "テスト店舗")
				.param("description", "テスト説明")
				.param("lowestPrice", "1000")
				.param("highestPrice", "5000")
				.param("postalCode", "0000000")
				.param("address", "テスト住所")
				.param("openingTime", "10:00")
				.param("closingTime", "17:00")
				.param("seatingCapacity", "10"))
		 .andExpect(status().is3xxRedirection())
         .andExpect(redirectedUrl("/admin/restaurants"));
		
		long countAfter = restaurantService.countRestaurants();
		
		assertThat(countAfter).isEqualTo(countBefore + 1);
		
		Restaurant restaurant = restaurantService.findFirstRestaurantByOrderByIdDesc();
		assertThat(restaurant.getName()).isEqualTo("テスト店舗");
		assertThat(restaurant.getDescription()).isEqualTo("テスト説明");
		assertThat(restaurant.getLowestPrice()).isEqualTo(1000);
		assertThat(restaurant.getHighestPrice()).isEqualTo(5000);
		assertThat(restaurant.getPostalCode()).isEqualTo("0000000");
		assertThat(restaurant.getAddress()).isEqualTo("テスト住所");
		assertThat(restaurant.getOpeningTime()).isEqualTo("10:00");
		assertThat(restaurant.getClosingTime()).isEqualTo("17:00");
		assertThat(restaurant.getSeatingCapacity()).isEqualTo(10);
	}
	
	@Test
	public void 未ログインの場合は管理者用の店舗編集ページからログインページにリダイレクトする() throws Exception {
		mockMvc.perform(get("/admin/restaurants/1/edit"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	public void 一般ユーザーとしてログイン済の場合は管理者用の店舗編集ページが表示されずに403エラーが発生する() throws Exception {
		mockMvc.perform(get("/admin/restaurants/1/edit"))
				.andExpect(status().isForbidden());	
		}

	@Test
	@WithUserDetails("hanako.samurai@example.com")
	public void 管理者としてログイン済みの場合は管理者用の店舗編集ページが正しく表示される() throws Exception {
		mockMvc.perform(get("/admin/restaurants/1/edit"))
		 		.andExpect(status().isOk())
		 		.andExpect(view().name("admin/restaurants/edit"));
	}
	
	@Test
	@Transactional
	public void 未ログインの場合は店舗を更新せずにログインページにリダイレクトする() throws Exception {
		
		long countBefore = restaurantService.countRestaurants();
		
		Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);
		
		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes
				);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/1/update").file(imageFile)
				.with(csrf())
				.param("name", "テスト店舗")
				.param("description", "テスト説明")
				.param("lowestPrice", "1000")
				.param("highestPrice", "5000")
				.param("postalCode", "0000000")
				.param("address", "テスト住所")
				.param("openingTime", "09:00")
				.param("closingTime", "17:00")
				.param("seatingCapacity", "10"))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("http://localhost/login"));
		
		long countAfter = restaurantService.countRestaurants();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 一般ユーザーとしてログイン済みの場合は店舗を更新せずに403エラーが発生する() throws Exception {
		
		long countBefore = restaurantService.countRestaurants();
		
		Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);
		
		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes
				);
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurant/1/update").file(imageFile)
				.with(csrf())
				.param("name", "テスト店舗")
				.param("description", "テスト説明")
				.param("lowestPrice", "1000")
				.param("highestPrice", "5000")
				.param("postalCode", "0000000")
				.param("address", "テスト住所")
				.param("openingTime", "09:00")
				.param("closingTime", "17:00")
				.param("seatingCapacity", "10"))
		.andExpect(status().isForbidden());
		
		long countAfter = restaurantService.countRestaurants();
		
		assertThat(countAfter).isEqualTo(countBefore);
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合は店舗更新後に店舗一覧ページにリダイレクトする() throws Exception {
		
		Path filePath = Paths.get("src/main/resources/static/images/no_image.jpg");
		String fileName = filePath.getFileName().toString();
		String fileType = Files.probeContentType(filePath);
		byte[] fileBytes = Files.readAllBytes(filePath);
		
		MockMultipartFile imageFile = new MockMultipartFile(
				"imageFile",
				fileName,
				fileType,
				fileBytes
				);	
		
		mockMvc.perform(MockMvcRequestBuilders.multipart("/admin/restaurants/1/update").file(imageFile)
				.with(csrf())
				.param("name", "テスト店舗")
				.param("description", "テスト説明")
				.param("lowestPrice", "1000")
				.param("highestPrice", "5000")
				.param("postalCode", "0000000")
				.param("address", "テスト住所")
				.param("openingTime", "09:00")
				.param("closingTime", "17:00")
				.param("seatingCapacity", "10"))
		.andExpect(status().is3xxRedirection())
		.andExpect(redirectedUrl("/admin/restaurants"));
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
		assertThat(optionalRestaurant).isPresent();
		Restaurant restaurant = optionalRestaurant.get();
		
		assertThat(restaurant.getName()).isEqualTo("テスト店舗");
		assertThat(restaurant.getDescription()).isEqualTo("テスト説明");
		assertThat(restaurant.getLowestPrice()).isEqualTo(1000);
		assertThat(restaurant.getHighestPrice()).isEqualTo(5000);
		assertThat(restaurant.getPostalCode()).isEqualTo("0000000");
		assertThat(restaurant.getAddress()).isEqualTo("テスト住所");
		assertThat(restaurant.getOpeningTime()).isEqualTo("09:00");
		assertThat(restaurant.getClosingTime()).isEqualTo("17:00");
		assertThat(restaurant.getSeatingCapacity()).isEqualTo(10);
	}
	
	@Test
	@Transactional
	public void 未ログインの場合は店舗を削除せずにログインページにリダイレクトする() throws Exception {
		mockMvc.perform(post("/admin/restaurants/1/delete").with(csrf()))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost/login"));
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
		assertThat(optionalRestaurant).isPresent();
		}
	
	@Test
	@WithUserDetails("taro.samurai@example.com")
	@Transactional
	public void 一般ユーザーとしてログイン済みの場合は店舗を削除せずに403エラーが発生する() throws Exception {
		mockMvc.perform(post("/admin/restaurants/1/delete").with(csrf()))
			 	.andExpect(status().isForbidden());
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
		assertThat(optionalRestaurant).isPresent();
	}
	
	@Test
	@WithUserDetails("hanako.samurai@example.com")
	@Transactional
	public void 管理者としてログイン済みの場合は店舗削除後に店舗一覧ページにリダイレクトする() throws Exception {
		mockMvc.perform(post("/admin/restaurants/1/delete").with(csrf()))
		 		.andExpect(status().is3xxRedirection())
		 		.andExpect(redirectedUrl("/admin/restaurants"));
		
		Optional<Restaurant> optionalRestaurant = restaurantService.findRestaurantById(1);
		assertThat(optionalRestaurant).isEmpty();
		
	}
}

