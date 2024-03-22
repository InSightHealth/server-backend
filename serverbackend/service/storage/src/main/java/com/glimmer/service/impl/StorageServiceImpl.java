package com.glimmer.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.glimmer.file.service.FileStorageService;
import com.glimmer.mapper.StorageMapper;
import com.glimmer.model.common.dtos.ResponseResult;
import com.glimmer.model.common.enums.AppHttpCodeEnum;
import com.glimmer.model.storage.pojos.ImgStorage;
import com.glimmer.model.user.pojos.User;
import com.glimmer.service.StorageService;
import com.glimmer.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
@Transactional
public class StorageServiceImpl extends ServiceImpl<StorageMapper, ImgStorage> implements StorageService {
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private StorageMapper storageMapper;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Override
    public ResponseResult UploadImg(MultipartFile multipartFile, Long categoryId) {
        //1.检查参数
        User user = AppThreadLocalUtil.getUser();
        if (multipartFile == null || multipartFile.getSize() == 0 || user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.上传图片到minIO中
        String fileName = UUID.randomUUID().toString().replace("-", "");
        //aa.jpg
        String originalFilename = multipartFile.getOriginalFilename();
        String postfix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileId = null;
        try {
            fileId = fileStorageService.uploadImgFile("", fileName + postfix, multipartFile.getInputStream());
            log.info("上传图片到MinIO中，fileId:{}", fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("StorageServiceImpl-上传文件失败");
        }
        //3.存储到数据库中
        ImgStorage is = new ImgStorage();
        is.setCategoryId(categoryId);
        is.setImage(fileId);
        is.setUserId(user.getId());
        save(is);
        //清理缓存数据
        String key = "img_" + is.getUserId() + "_" + is.getCategoryId();
        cleanCache(key);
        //4.返回结果
        Map<String, String> map = new HashMap();
        map.put("image", fileId);
        return ResponseResult.okResult(map);

    }

    @Override
    public ResponseResult GetPictures(Long categoryId) {
        //categoryId:1-智能出行，2-拍照识图，3-医疗健康，4-辅助阅读
        User user = AppThreadLocalUtil.getUser();
        if(user == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        //构造redis中的key，规则：img_用户id_分类id
        String key = "img_" + user.getId() + "_" + categoryId;
        //查询redis中是否存在图片数据
        List<ImgStorage> list = (List<ImgStorage>) redisTemplate.opsForValue().get(key);
        if(list != null && list.size() > 0){
            //如果存在，直接返回，无须查询数据库
            return ResponseResult.okResult(list);
        }
        //如果不存在，查询数据库，将查询到的数据放入redis中
        List<ImgStorage> imgStorages = storageMapper.selectList(Wrappers.<ImgStorage>lambdaQuery().eq(ImgStorage::getUserId,user.getId()).eq(ImgStorage::getCategoryId, categoryId));
        List<String> images = new ArrayList<>();
        for (ImgStorage imgStorage : imgStorages) {
            images.add(imgStorage.getImage());
        }
        redisTemplate.opsForValue().set(key, images);
        return ResponseResult.okResult(images);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
