package com.glimmer.controller.v1;


import com.glimmer.model.common.dtos.ResponseResult;
import com.glimmer.model.user.dtos.LoginDto;
import com.glimmer.model.user.dtos.SendCodeDto;
import com.glimmer.model.user.dtos.UpdateDto;
import com.glimmer.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@Api(value = "app端账户", tags = "User", description = "app端账户相关API")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    @ApiOperation("账户登录")
    public ResponseResult Login(@RequestBody LoginDto dto){
        return userService.Login(dto);
    }

    @PostMapping("/sendCode")
    @ApiOperation(value = "验证码发送")
    public ResponseResult SendCode(@RequestBody SendCodeDto dto){
        return userService.SendCode(dto);
    }

    @PostMapping("/uploadPicture")
    @ApiOperation(value = "账户头像图片上传")
    public ResponseResult UploadPicture(MultipartFile multipartFile){
        return userService.UploadPicture(multipartFile);
    }

    @PostMapping("/uploadImg")
    @ApiOperation(value = "其他图片上传")
    public ResponseResult UploadImg(MultipartFile multipartFile){
        return userService.UploadImg(multipartFile);
    }

    @GetMapping
    @ApiOperation(value = "获取用户数据")
    public ResponseResult Get(){
        return userService.Get();
    }

    @DeleteMapping("/clean")
    @ApiOperation(value = "清除缓存")
    public ResponseResult Clean(){
        return userService.Clean();
    }

    @PutMapping
    @ApiOperation(value = "更新用户信息")
    public ResponseResult Update(@RequestBody UpdateDto dto){
        return userService.Update(dto);
    }

    @DeleteMapping("/logoff")
    @ApiOperation(value = "账户注销")
    public ResponseResult Delete(){
        return userService.Delete();
    }
}
