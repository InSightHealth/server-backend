package com.glimmer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.glimmer.model.common.dtos.ResponseResult;
import com.glimmer.model.storage.pojos.ImgStorage;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService extends IService<ImgStorage> {

    ResponseResult UploadImg(MultipartFile multipartFile,Long categoryId);

    ResponseResult GetPictures(Long categoryId);
}
