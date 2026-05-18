package com.lifetrack.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 开发阶段：仅记录重置链接到日志，不发送邮件
    public void sendResetPasswordEmail(String to, String resetLink) {
        log.info("===== 密码重置邮件（开发模式） =====");
        log.info("收件人: {}", to);
        log.info("重置链接: {}", resetLink);
        log.info("====================================");
    }
}
