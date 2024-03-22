package com.glimmer.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.glimmer.model.storage.pojos.ImgStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StorageMapper extends BaseMapper<ImgStorage> {
}
