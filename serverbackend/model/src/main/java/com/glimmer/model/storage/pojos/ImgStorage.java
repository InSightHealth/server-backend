package com.glimmer.model.storage.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName(value = "img_storage")
public class ImgStorage implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 分类id
     */
    @TableField(value = "category_id")
    private Long categoryId;
    /**
     * 用户id
     */
    @TableField(value = "user_id")
    private Long userId;
    /**
     * 图像uri
     */
    @TableField(value = "image")
    private String image;

}
