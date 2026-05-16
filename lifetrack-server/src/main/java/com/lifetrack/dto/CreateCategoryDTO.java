package com.lifetrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCategoryDTO {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotNull(message = "类型不能为空")
    private Integer type;

    private String icon;
    private String color;
}
