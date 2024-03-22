package com.glimmer.model.user.vos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户名
     */
    @ApiModelProperty("username")
    private String username;
    /**
     * 手机号
     */
    @ApiModelProperty("phone")
    private String phone;
    /**
     0正常
     1锁定
     */
    @ApiModelProperty("status")
    private Integer status;
    /**
     * 头像uri
     */
    @ApiModelProperty("image_uri")
    private String imageUri;
    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "nickname")
    private String nickname;
    /**
     * 性别
     */
    @ApiModelProperty(value = "gender")
    private String gender;
    /**
     * 城市
     */
    @ApiModelProperty(value = "city")
    private String city;
    /**
     * 生日
     */
    @ApiModelProperty(value = "birthday")
    private String birthday;
    /**
     * 身高
     */
    @ApiModelProperty(value = "height")
    private Integer height;
    /**
     * 体重
     */
    @ApiModelProperty(value = "weight")
    private Integer weight;
}
