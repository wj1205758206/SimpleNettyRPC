package com.example.simplenettyrpc.util.example.services;

import com.example.simplenettyrpc.domain.UserInfo;
import com.example.simplenettyrpc.util.annotation.EnableRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * server端实现public interface
 */
@EnableRpcService(UserInfoServices.class)
public class UserInfoServicesImpl implements UserInfoServices {
    private static final Logger logger = LoggerFactory.getLogger(UserInfoServicesImpl.class);

    private static final Map<String, UserInfo> userInfoMap = new HashMap<>(); // 存储user信息

    @Override
    public boolean addUser(UserInfo userInfo) {
        logger.info("add user: " + userInfo.toString());
        if (userInfo != null) {
            userInfoMap.put(userInfo.getId(), userInfo);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteUserById(String id) {
        logger.info("delete user, id: " + id);
        if (userInfoMap.containsKey(id)) {
            userInfoMap.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean updateUserInfoById(String id, UserInfo userInfo) {
        logger.info("update id: " + id + ", old userInfo :" + userInfoMap.get(id) + ", new userInfo: " + userInfo);
        if (userInfoMap.containsKey(id)) {
            userInfoMap.remove(id);
            userInfoMap.put(id, userInfo);
            return true;
        }
        return false;
    }

    @Override
    public UserInfo getUserInfoById(String id) {
        logger.info("get userInfo: " + userInfoMap.get(id));
        if (userInfoMap.containsKey(id)) {
            return userInfoMap.get(id);
        }
        return null;
    }

    @Override
    public List<UserInfo> getAll() {
        List<UserInfo> userInfoList = (List<UserInfo>) userInfoMap.values();
        logger.info("get all userInfo: " + userInfoList);
        return userInfoList;
    }
}
