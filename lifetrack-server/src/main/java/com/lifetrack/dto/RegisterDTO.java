package com.lifetrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterDTO {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度8-20位")
    private String password;

    @Size(min = 2, max = 20, message = "昵称2-20个字符")
    private String nickname;
}
