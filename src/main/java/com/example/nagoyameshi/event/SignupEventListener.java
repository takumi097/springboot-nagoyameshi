package com.example.nagoyameshi.event;

import java.util.UUID;

import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.service.VerificationTokenService;

@Component
public class SignupEventListener {
	private final VerificationTokenService verificationTokenService;
	private final JavaMailSender javaMailSender;
	
	public SignupEventListener(VerificationTokenService verificationTokenService, JavaMailSender javaMailSender) {
		this.verificationTokenService = verificationTokenService;
		this.javaMailSender = javaMailSender;
	}
	
	@EventListener
	//SignupEventクラスから通知を受けた時に実行される処理
	private void onSignupEvent(SignupEvent signupEvent) {
		User user = signupEvent.getUser();
		String token = UUID.randomUUID().toString();
		verificationTokenService.createVerificationToken(user, token);
		
		String senderAddress = "igataku97614@gmail.com";
		String recipientAddress = user.getEmail();
		String subject = "メール認証";
		String confirmationUrl = signupEvent.getRequestUrl() + "/verify?token=" + token;
		String message = "以下のリンクをクリックして会員登録を完了してください。";
		
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		//送信元のメールアドレス
		mailMessage.setFrom(senderAddress);
		//送信先のメールアドレス
		mailMessage.setTo(recipientAddress);
		//件名をセット
		mailMessage.setSubject(subject);
		//本文をセット
		mailMessage.setText(message + "\n" + confirmationUrl);
		//メールを送信
		javaMailSender.send(mailMessage);
	}
}
