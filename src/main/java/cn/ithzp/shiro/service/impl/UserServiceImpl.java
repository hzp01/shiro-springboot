package cn.ithzp.shiro.service.impl;

import cn.ithzp.shiro.domain.UserDomain;
import cn.ithzp.shiro.mapper.UserMapper;
import cn.ithzp.shiro.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDomain findByUserName(String userName) {
        return userMapper.findByUserName(userName);
    }

    @Override
    public UserDomain findById(Integer id) {
        return userMapper.findById(id);
    }
}
