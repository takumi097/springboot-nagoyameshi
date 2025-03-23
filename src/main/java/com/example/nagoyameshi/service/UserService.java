package com.example.nagoyameshi.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Role;
import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.RoleRepository;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	
	public UserService(UserRepository userRepository, RoleRepository roleRepository,
			PasswordEncoder passwordEncoder) 
	{
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public User createdUser(SignupForm signupForm) {
		User user = new User();
		Role role = roleRepository.findByName("ROLE_FREE_MEMBER");
		
		if (!signupForm.getBirthday().isEmpty()) {
			user.setBirthday(LocalDate.parse(signupForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		} else {
			user.setBirthday(null);
		}
		
		if (!signupForm.getOccupation().isEmpty()) {
			user.setOccupation(signupForm.getOccupation());
		} else {
			user.setOccupation(null);
		}
		
		user.setName(signupForm.getName());
		user.setFurigana(signupForm.getFurigana());
		user.setPostalCode(signupForm.getPostalCode());
		user.setAddress(signupForm.getAddress());
		user.setPhoneNumber(signupForm.getPhoneNumber());
		user.setEmail(signupForm.getEmail());
		user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
		user.setRole(role);
		user.setEnabled(false);
		
		return userRepository.save(user);
	}
	
	//メールアドレスが登録済かをチェックする
	public boolean isEmailRegistered(String email) {
		User user = userRepository.findByEmail(email);
		
		return user != null;
	}
	
	//パスワードを確認用の入力値が一致するかチェック
	public boolean isSamePassword(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}
	
	//ユーザーを有効にする
	@Transactional
	public void enableUser(User user) {
		user.setEnabled(true);
		userRepository.save(user);
	}
	
	//指定されたキーワードを氏名またはフリガナに含むユーザーを、ページングされた状態で取得する
	public Page<User>findUsersByNameLikeOrFuriganaLike(String nameKeyword, String furiganaKeyword, Pageable pageable) {
		return userRepository.findByNameLikeOrFuriganaLike("%" + nameKeyword + "%", "%" + furiganaKeyword + "%", pageable);
	}
	
	//すべてのユーザーをページングされた状態で取得する
	public Page<User> findAllUsers(Pageable pageable) {
		return userRepository.findAll(pageable);
	}
	
	//指定したIDを持つユーザーを取得する
	public Optional<User> findUserById(Integer userId) {
		return userRepository.findById(userId);
	}
	
	//フォームから送信された会員情報でデータベースを更新する
	public void updateUser(UserEditForm userEditForm, User user) {
		
		if (!userEditForm.getBirthday().isEmpty()) {
			user.setBirthday(LocalDate.parse(userEditForm.getBirthday(), DateTimeFormatter.ofPattern("yyyyMMdd")));
		} else {
			user.setBirthday(null);
		}
		
		if (!userEditForm.getOccupation().isEmpty()) {
			user.setOccupation(userEditForm.getOccupation());
		} else {
			user.setOccupation(null);
		}
		
		user.setName(userEditForm.getName());
		user.setFurigana(userEditForm.getFurigana());
		user.setPostalCode(userEditForm.getPostalCode());
		user.setAddress(userEditForm.getAddress());
		user.setPhoneNumber(userEditForm.getPhoneNumber());
		user.setEmail(userEditForm.getEmail());
		
		userRepository.save(user);;
	}
	
	//メールアドレスが変更されたかどうかチェック
	public boolean isEmailChanged(UserEditForm userEditForm, User user) {
		return !userEditForm.getEmail().equals(user.getEmail());
	}
	
	//指定したメールアドレスを持つユーザーを取得する
	public User findUserByEmail(String email) {
		return userRepository.findByEmail(email);
	}
}
