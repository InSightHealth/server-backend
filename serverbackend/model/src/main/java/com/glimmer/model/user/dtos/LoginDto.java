package com.glimmer.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginDto {
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;
    /**
     * 验证码
     */
    @ApiModelProperty(value = "验证码")
    private Integer code;
}
