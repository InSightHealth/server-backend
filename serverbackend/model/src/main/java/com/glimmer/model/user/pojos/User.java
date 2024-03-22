package com.glimmer.model.user.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName(value = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户名
     */
    @TableField("username")
    private String username;
    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;
    /**
     * 当前获取的验证码，实时更新
     */
    @TableField("code")
    private Integer code;
    /**
     0正常
     1锁定
     */
    @TableField("status")
    private Integer status;
    /**
     * 头像uri
     */
    @TableField("image_uri")
    private String imageUri;
    /**
     * 验证码创建时间
     */
    @TableField("created_time")
    private Date createdTime;
    /**
     * 用户昵称
     */
    @TableField(value = "nickname")
    private String nickname;
    /**
     * 性别
     */
    @TableField(value = "gender")
    private String gender;
    /**
     * 城市
     */
    @TableField(value = "city")
    private String city;
    /**
     * 生日
     */
    @TableField(value = "birthday")
    private Date birthday;
    /**
     * 身高
     */
    @TableField(value = "height")
    private Integer height;
    /**
     * 体重
     */
    @TableField(value = "weight")
    private Integer weight;
}
