package cn.ithzp.shiro.domain;

import lombok.Data;

@Data
public class UserDomain {
    private int id;
    private String userName;
    private String passwd;
    private String perms;
}
