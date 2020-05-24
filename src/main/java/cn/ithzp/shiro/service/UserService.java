package cn.ithzp.shiro.service;

import cn.ithzp.shiro.domain.UserDomain;

public interface UserService {
    UserDomain findByUserName(String userName);

    UserDomain findById(Integer id);
}
