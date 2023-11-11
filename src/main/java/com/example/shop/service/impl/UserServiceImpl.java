package com.example.shop.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shop.common.exception.ServerException;
import com.example.shop.common.utils.GeneratorCodeUtils;
import com.example.shop.common.utils.JWTUtils;
import com.example.shop.convert.UserConvert;
import com.example.shop.entity.User;
import com.example.shop.mapper.UserMapper;
import com.example.shop.query.UserloginQuery;
import com.example.shop.service.RedisService;
import com.example.shop.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.shop.vo.LoginResultVO;
import com.example.shop.vo.UserTokenVO;
import com.example.shop.vo.UserVO;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.rmi.server.ServerCloneException;

import static com.example.shop.Constant.APIConstant.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author ycshang
 * @since 2023-11-07
 */
@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private  final RedisService redisService;
        @Override
        public LoginResultVO login(UserloginQuery query){
    //  1、获取openId
    String url = "https://api.weixin.qq.com/sns/jscode2session?" +
            "appid=" + APP_ID +
            "&secret=" + APP_SECRET +
            "&js_code=" + query.getCode() +
            "&grant_type=authorization_code";
            RestTemplate restTemplate=new RestTemplate();
            String openIdResult=restTemplate.getForObject(url,String.class);
            if(StringUtils.contains(openIdResult,WX_ERR_CODE)){
                throw new ServerException("openid获取失败"+openIdResult);
            }
//
                JSONObject jsonObject =JSON.parseObject(openIdResult);
                 String openid=jsonObject.getString(WX_OPENID);
                 User user= baseMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getOpenId,openid));
                 //
                    if(user == null) {
                        user = new User();
                        String account = "用户" + GeneratorCodeUtils.generateCode();
                        user.setAccount(account);
                        user.setAvatar(DEFAULT_AVATAR);
                        user.setNickname(account);
                        user.setOpenId(openid);
                        user.setMobile(".");
                        baseMapper.insert(user);
                    }
                    LoginResultVO userVO= UserConvert.INSTANCE.convertToLoginResultVO(user);

                    //
                    UserTokenVO tokenVO=new UserTokenVO(userVO.getId());
                    String token= JWTUtils.generateToken(JWT_SECRET,tokenVO.toMap());
                    redisService.set(APP_NAME+userVO.getId(),token,APP_TOKEN_EXPIRE_TIME);
                    userVO.setToken(token);
                    return userVO;

                    }

    @Override
    public User getUserInfo(Integer userId) throws ServerCloneException {
        User user= baseMapper.selectById(userId);
        if(user==null){
            throw new ServerCloneException("用户不存在");
        }
        return user;
    }
            @Override
    public UserVO editUserIfo(UserVO userVO){
            User user= baseMapper.selectById(userVO.getId());
            if(user == null){
                throw new ServerException("用户不存在");
            }
            User userConvert=UserConvert.INSTANCE.convert(userVO);
            updateById(userConvert);
            return userVO;
 }

    @Override
    public UserVO editUserInfo(UserVO userVO) {
        User user= baseMapper.selectById(userVO.getId());
        if(user == null){
            throw new ServerException("用户不存在");
        }
        User userConvert=UserConvert.INSTANCE.convert(userVO);
        updateById(userConvert);
        return userVO;
    }


}

