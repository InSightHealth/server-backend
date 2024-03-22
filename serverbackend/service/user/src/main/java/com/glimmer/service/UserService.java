package com.glimmer.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.glimmer.model.common.dtos.ResponseResult;
import com.glimmer.model.user.dtos.LoginDto;
import com.glimmer.model.user.dtos.SendCodeDto;
import com.glimmer.model.user.dtos.UpdateDto;
import com.glimmer.model.user.pojos.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param dto
     * @return
     */
    ResponseResult Login(LoginDto dto);

    /**
     * 发送验证码
     * @param dto
     * @return
     */
    ResponseResult SendCode(SendCodeDto dto);
    /**
     * 用户头像上传
     * @param multipartFile
     * @return
     */
    ResponseResult UploadPicture(MultipartFile multipartFile);

    /**
     * 获取用户信息
     * @return
     */
    ResponseResult Get();

    /**
     * 测试用旧方法
     * @param multipartFile
     * @return
     */
    ResponseResult UploadImg(MultipartFile multipartFile);

    /**
     * 清除缓存
     * @return
     */
    ResponseResult Clean();

    /**
     * 更新用户数据信息
     * @param dto
     * @return
     */
    ResponseResult Update(UpdateDto dto);

    /**
     * 账户注销
     * @return
     */
    ResponseResult Delete();
}
