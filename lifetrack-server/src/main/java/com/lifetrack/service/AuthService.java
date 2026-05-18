package com.lifetrack.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifetrack.common.exception.BusinessException;
import com.lifetrack.config.JwtUtils;
import com.lifetrack.dto.*;
import com.lifetrack.entity.User;
import com.lifetrack.mapper.UserMapper;
import com.lifetrack.vo.LoginVO;
import com.lifetrack.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final EmailService emailService;

    public void register(RegisterDTO dto) {
        User existing = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (existing != null) {
            throw new BusinessException("该邮箱已注册");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getEmail().split("@")[0]);
        userMapper.insert(user);
    }

    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            throw new BusinessException(401, "邮箱或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(401, "邮箱或密码错误");
        }

        String token = jwtUtils.generateToken(user.getId());
        long expire = 604800; // 7 days
        return new LoginVO(token, expire, token);
    }

    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setHeight(user.getHeight());
        vo.setTargetWeight(user.getTargetWeight());
        vo.setEmail(user.getEmail());
        vo.setNotifyEmail(user.getNotifyEmail());
        return vo;
    }

    public void updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getHeight() != null) user.setHeight(dto.getHeight());
        if (dto.getTargetWeight() != null) user.setTargetWeight(dto.getTargetWeight());
        if (dto.getNotifyEmail() != null) user.setNotifyEmail(dto.getNotifyEmail());
        userMapper.updateById(user);
    }

    public void forgotPassword(ForgotPasswordDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            return; // 静默返回，防枚举攻击
        }

        String rawToken = UUID.randomUUID().toString().replace("-", "");
        user.setResetToken(passwordEncoder.encode(rawToken));
        user.setResetTokenExp(LocalDateTime.now().plusHours(24));
        userMapper.updateById(user);

        String resetLink = "http://localhost:3000/auth/reset-password?token=" + rawToken + "&email=" + dto.getEmail();
        emailService.sendResetPasswordEmail(dto.getEmail(), resetLink);
    }

    public void resetPassword(ResetPasswordDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null || user.getResetToken() == null) {
            throw new BusinessException(400, "重置链接无效或已过期");
        }
        if (user.getResetTokenExp().isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "重置链接已过期，请重新申请");
        }
        if (!passwordEncoder.matches(dto.getToken(), user.getResetToken())) {
            throw new BusinessException(400, "重置链接无效");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExp(null);
        userMapper.updateById(user);
    }

    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
            throw new BusinessException(422, "当前密码不正确");
        }
        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }
}
