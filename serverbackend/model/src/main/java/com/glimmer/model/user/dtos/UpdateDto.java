package com.glimmer.model.user.dtos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class UpdateDto {
    /**
     * 手机号
     */
    @ApiModelProperty("手机号")
    private String phone;
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String nickname;
    /**
     * 性别
     */
    @ApiModelProperty(value = "性别")
    private String gender;
    /**
     * 城市
     */
    @ApiModelProperty(value = "城市")
    private String city;
    /**
     * 生日
     */
    @ApiModelProperty(value = "生日")
    private String birthday;
    /**
     * 身高
     */
    @ApiModelProperty(value = "身高")
    private Integer height;
    /**
     * 体重
     */
    @ApiModelProperty(value = "体重")
    private Integer weight;
}
