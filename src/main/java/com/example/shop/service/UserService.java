package com.example.shop.service;


import com.example.shop.entity.User;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.shop.query.UserloginQuery;
import com.example.shop.vo.LoginResultVO;
import com.example.shop.vo.UserVO;

import java.rmi.server.ServerCloneException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ycshang
 * @since 2023-11-07
 */
public interface UserService extends IService<User> {

    LoginResultVO login(UserloginQuery query);

    User getUserInfo(Integer userId) throws ServerCloneException;

    UserVO editUserIfo(UserVO userVO);

    UserVO editUserInfo(UserVO userVO);
}