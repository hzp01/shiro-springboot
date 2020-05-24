package cn.ithzp.shiro.mapper;

import cn.ithzp.shiro.domain.UserDomain;

public interface UserMapper {
    UserDomain findByUserName(String userName);

    UserDomain findById(Integer id);
}
