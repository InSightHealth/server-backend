package com.glimmer.service.impl;

import com.aliyun.tea.TeaException;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.glimmer.file.service.FileStorageService;
import com.glimmer.mapper.UserMapper;
import com.glimmer.model.common.dtos.ResponseResult;
import com.glimmer.model.common.enums.AppHttpCodeEnum;
import com.glimmer.model.storage.pojos.ImgStorage;
import com.glimmer.model.user.dtos.LoginDto;
import com.glimmer.model.user.dtos.SendCodeDto;
import com.glimmer.model.user.dtos.UpdateDto;
import com.glimmer.model.user.pojos.User;
import com.glimmer.model.user.vos.UserVO;
import com.glimmer.service.UserService;
import com.glimmer.utils.common.AppJwtUtil;
import com.glimmer.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.ws.Response;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public ResponseResult Login(LoginDto dto) {
        if(dto == null || dto.getCode() == null || dto.getPhone() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        User user = null;
        if(!StringUtils.isBlank(dto.getPhone())) {
            user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, dto.getPhone()));
            if(user == null) {
                //表明未注册，注册应当在发送验证码时进行
                return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
            }
        }
        //校验验证码
        if(!Objects.equals(dto.getCode(), user.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_CODE_ERROR);
        }
        //校验验证码是否有效,3分钟过期
        if(user.getCreatedTime().getTime() + 3*60*1000 < System.currentTimeMillis()){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_CODE_INVALID,"验证码已过期");
        }
        //校验账户状态,1为正常,0锁定
        if(user.getStatus()!=1){
            return ResponseResult.errorResult(AppHttpCodeEnum.ACCOUNT_LOCKED);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("token", AppJwtUtil.getToken(user.getId().longValue()));
        return ResponseResult.okResult(map);
    }

    @Override
    public ResponseResult SendCode(SendCodeDto dto) {
        if(dto ==null || dto.getPhone() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        // 确保代码运行环境设置了环境变量 ALIBABA_CLOUD_ACCESS_KEY_ID 和 ALIBABA_CLOUD_ACCESS_KEY_SECRET。
        com.aliyun.dysmsapi20170525.Client client = null;
        try {
            client = createClient(System.getenv("ALIBABA_CLOUD_ACCESS_KEY_ID"), System.getenv("ALIBABA_CLOUD_ACCESS_KEY_SECRET"));
        } catch (Exception e) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
        }
        Random r = new Random();
        Integer code = r.nextInt(900000) + 100000;
        com.aliyun.dysmsapi20170525.models.SendSmsRequest sendSmsRequest = new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                .setSignName("App短信验证码")
                .setTemplateCode("SMS_465331482")
                .setPhoneNumbers(dto.getPhone())
                .setTemplateParam("{\"code\":\""+code+"\"}");
        com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
        try {
            client.sendSmsWithOptions(sendSmsRequest, runtime);
        } catch (TeaException error) {
            // 错误 message
            log.error(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
            return ResponseResult.errorResult(AppHttpCodeEnum.ACCOUNT_SEND_CODE_FAILED);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            // 错误 message
            System.out.println(error.getMessage());
            // 诊断地址
            System.out.println(error.getData().get("Recommend"));
            com.aliyun.teautil.Common.assertAsString(error.message);
            return ResponseResult.errorResult(AppHttpCodeEnum.ACCOUNT_SEND_CODE_FAILED);
        }
        User one = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, dto.getPhone()));
        if(one ==null){
            //未注册，就先保存注册数据
            one = new User();
            one.setCode(code);
            one.setPhone(dto.getPhone());
            one.setStatus(1);
            one.setUsername(getRandomString(10));
            one.setCreatedTime(new Date());
            save(one);
        }else{
            one.setCode(code);
            one.setCreatedTime(new Date());
            updateById(one);
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult UploadPicture(MultipartFile multipartFile) {
        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
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
            log.error("UserServiceImpl-上传文件失败");
        }

        //3.保存到数据库中
        User user = new User();
        user.setId(AppThreadLocalUtil.getUser().getId());
        user.setImageUri(fileId);
        updateById(user);

        //清理缓存数据
        String key = "user_" + user.getId();
        cleanCache(key);
        //4.返回结果
        Map<String, String> map = new HashMap();
        map.put("imageUri", user.getImageUri());
        return ResponseResult.okResult(map);
    }

    @Override
    public ResponseResult Get() {
        User user = AppThreadLocalUtil.getUser();
        if( user == null || user.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        //构造redis中的key，规则：user_用户id
        String key = "user_" + user.getId();
        //查询redis中是否存在用户数据
        UserVO user_ = (UserVO) redisTemplate.opsForValue().get(key);
        if(user_ != null){
            //如果存在，直接返回，无须查询数据库
            return ResponseResult.okResult(user_);
        }
        //如果不存在，查询数据库，将查询到的数据放入redis中
        User one = getOne(Wrappers.<User>lambdaQuery().eq(User::getId, user.getId()));
        if(one == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(one,userVO);
        //存在日期格式转换
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String format = sdf.format(one.getBirthday());
        userVO.setBirthday(format);
        redisTemplate.opsForValue().set(key, userVO);
        return ResponseResult.okResult(userVO);
    }

    /**
     * 旧方法，用于测试
     * @param multipartFile
     * @return
     */
    @Override
    public ResponseResult UploadImg(MultipartFile multipartFile) {
        //1.检查参数
        if (multipartFile == null || multipartFile.getSize() == 0) {
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
            log.error("UserServiceImpl-上传文件失败");
        }
        //4.返回结果
        Map<String, String> map = new HashMap();
        map.put("image", fileId);
        return ResponseResult.okResult(map);
    }

    @Override
    public ResponseResult Clean() {
        cleanCache("*");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult Update(UpdateDto dto) {
        User user = AppThreadLocalUtil.getUser();
        if( user == null || user.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        User one = getOne(Wrappers.<User>lambdaQuery().eq(User::getId, user.getId()));
        if(one == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        if(StringUtils.isNotBlank(dto.getCity())){
            one.setCity(dto.getCity());
        }
        if(StringUtils.isNotBlank(dto.getPhone())){
            one.setPhone(dto.getPhone());
        }
        if(StringUtils.isNotBlank(dto.getGender())){
            one.setGender(dto.getGender());
        }
        if(StringUtils.isNotBlank(dto.getBirthday())){
            //存在日期格式转换
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = sdf.parse(dto.getBirthday());
            } catch (ParseException e) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
            }
            one.setBirthday(date);
        }
        if(StringUtils.isNotBlank(dto.getNickname())){
            one.setNickname(dto.getNickname());
        }
        if(dto.getHeight() < 0 || dto.getHeight() > 200 || dto.getWeight() < 0 || dto.getWeight() > 200){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        one.setHeight(dto.getHeight());
        one.setWeight(dto.getWeight());
        updateById(one);
        cleanCache("*");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult Delete() {
        User user = AppThreadLocalUtil.getUser();
        if(user ==null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        if(!removeById(user.getId())){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        cleanCache("*");
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult LoginFast(SendCodeDto dto) {
        if(dto == null || dto.getPhone() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        User user = null;
        if(!StringUtils.isBlank(dto.getPhone())) {
            user = getOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, dto.getPhone()));
            if(user == null) {
                //表明之前未注册
                user = new User();
                user.setPhone(dto.getPhone());
                user.setStatus(1);
                user.setUsername(getRandomString(10));
                save(user);
            }
        }
        //校验账户状态,1为正常,0锁定
        if(user.getStatus()!=1){
            return ResponseResult.errorResult(AppHttpCodeEnum.ACCOUNT_LOCKED);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("token", AppJwtUtil.getToken(user.getId().longValue()));
        return ResponseResult.okResult(map);
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 使用AK&SK初始化账号Client
     *
     * @param accessKeyId
     * @param accessKeySecret
     * @return Client
     * @throws Exception
     */
    public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                // 必填，您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 必填，您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // Endpoint 请参考 https://api.aliyun.com/product/Dysmsapi
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
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
