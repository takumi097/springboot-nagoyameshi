package com.example.nagoyameshi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration  //設定用のクラス
@EnableWebSecurity  //認証・認可のルールやログイン・ログアウト処理などを各種設定を行える
@EnableMethodSecurity	//メソッドレベルでのセキュリティ機能を有効にする
public class WebSecurityConfig {
	@Bean	//インスタンスがDIコンテナに登録される
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
					//すべてのユーザーにアクセスを許可する
					.requestMatchers("/css/**", "/images/**", "/js/**", "/storage/**", "/", "/signup/**").permitAll()
					 // 管理者にのみアクセスを許可するURL
					.requestMatchers("/admin/**").hasRole("ADMIN") 
					//未ログインユーザー、無料会員、有料会員がアクセスを許可URL
					.requestMatchers("/restaurants/**").hasAnyRole("ANONYMOUS", "FREE_MEMBER", "PAID_MEMBER")
					//無料会員のみアクセスを許可するURL
					.requestMatchers("/subscription/register", "/subscription/create").hasRole("FREE_MEMBER")
					//有料会員のみアクセス許可するURL
					.requestMatchers("/subscription/edit", "/subscription/update", "/subscription/cancel", "/subscription/delete").hasRole("PAID_MEMBER")
					//上記以外のURLはログインが必要（会員または管理者のどちらでもOK）
					.anyRequest().authenticated()
					)
			.formLogin((form) -> form
					//ログインページのURL
					.loginPage("/login")
					//ログインフォームの送信先URL
					.loginProcessingUrl("/login")
					//ログイン成功時のリダイレクト先URL
					.defaultSuccessUrl("/?loggedIn")
					//ログイン失敗時のリダイレクト先URL
					.failureUrl("/login?error")
					.permitAll()
					)
			.logout((logout) -> logout
					//ログアウト時のリダイレクト先URL
					.logoutSuccessUrl("/?loggedOut")
					.permitAll()
					);
		
		return http.build();
	}
	
	//パスワードの暗号化
	@Bean
	public PasswordEncoder passwordencoder() {
		return new BCryptPasswordEncoder();	//BCryptはパスワード用のハッシュ値を生成してくれる
	}
}
