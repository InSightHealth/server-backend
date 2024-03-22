package com.glimmer.controller.v1;

import com.glimmer.model.common.dtos.ResponseResult;
import com.glimmer.service.StorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Api(value = "app端数据存储", tags = "Storage", description = "app端数据存储API")
public class StorageController {
    @Autowired
    private StorageService storageService;
    @PostMapping("/uploadImg/move")
    @ApiOperation(value = "智能出行图片上传")
    public ResponseResult UploadImgMove(MultipartFile multipartFile){
        return storageService.UploadImg(multipartFile,1l);
    }

    @PostMapping("/uploadImg/photo")
    @ApiOperation(value = "拍照识图图片上传")
    public ResponseResult UploadImgPhoto(MultipartFile multipartFile){
        return storageService.UploadImg(multipartFile,2l);
    }

    @PostMapping("/uploadImg/health")
    @ApiOperation(value = "医疗健康图片上传")
    public ResponseResult UploadImgHealth(MultipartFile multipartFile){
        return storageService.UploadImg(multipartFile,3l);
    }

    @PostMapping("/uploadImg/read")
    @ApiOperation(value = "辅助阅读图片上传")
    public ResponseResult UploadImgRead(MultipartFile multipartFile){
        return storageService.UploadImg(multipartFile,4l);
    }

    @GetMapping("/{categoryId}")
    @ApiOperation(value = "根据分类获取上传图片")
    public ResponseResult GetPictures(@PathVariable("categoryId") Long categoryId){
        return storageService.GetPictures(categoryId);
    }

}
