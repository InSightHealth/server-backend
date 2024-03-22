package com.glimmer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glimmer.model.user.pojos.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
