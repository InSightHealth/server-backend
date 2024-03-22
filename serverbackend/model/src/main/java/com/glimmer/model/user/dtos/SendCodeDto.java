package com.glimmer.model.user.dtos;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendCodeDto {
    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;
}
