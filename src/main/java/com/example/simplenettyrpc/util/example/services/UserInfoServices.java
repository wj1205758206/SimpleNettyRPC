package com.example.simplenettyrpc.util.example.services;

import com.example.simplenettyrpc.domain.UserInfo;

import java.util.List;

/**
 * client和server公共接口
 */
public interface UserInfoServices {
    boolean addUser(UserInfo userInfo);

    boolean deleteUserById(String id);

    boolean updateUserInfoById(String id, UserInfo userInfo);

    UserInfo getUserInfoById(String id);

    List<UserInfo> getAll();
}
