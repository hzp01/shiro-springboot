[TOC]
# 1 shiro简介
```
subject: 用户主体
SecurityManager:安全管理器
Realm:Shiro连接数据的桥梁
```

# 2 简单使用
## 2.1 业务说明
- 业务需求：实现登录认证和资源访问的授权
- 技术选型：
```
spring boot:2.3.0.RELEASE
jdk:1.8
shiro:1.5.3
mysql:5.5.27
```

## 2.2 shiro集成boot
### 2.2.1 建立项目shiro
- 1 新建spring项目，开发工具选择lombok
- 2 修改pom，增加shiro依赖
`org.apache.shiro.shiro-spring.version:1.5.3`
- 3 增加实现了AuthorizingRealm的具体realm类
```
package cn.ithzp.shiro.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

@Slf4j
public class UserRealm extends AuthorizingRealm {
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("执行授权逻辑");
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        log.info("执行认证逻辑");
        return null;
    }
}
```
- 4 增加shiro配置类
```
package cn.ithzp.shiro.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Configuration
public class ShiroConfig {
    @Bean("userRealm")
    public UserRealm getRealm() {
        log.info("获取继承了AuthorizingRealm的具体realm对象");
        return new UserRealm();
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager getDefaultWebSecurityManager(
            @Qualifier("userRealm") UserRealm userRealm
    ) {
        log.info("获取关联具体realm的securityManager安全管理器");
        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();
        defaultWebSecurityManager.setRealm(userRealm);
        return defaultWebSecurityManager;
    }

    @Bean
    public ShiroFilterFactoryBean getShiroFilterFactoryBean(
            @Qualifier("securityManager") DefaultWebSecurityManager securityManager
    ) {
        log.info("获取shiro内置过滤器");
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        /**
         * shiro内置过滤器包含anon/authc/user/perms/role
         * anon:无需认证
         * authc:必须认证
         * user：如果使用rememberme可以直接访问
         * perms：必须有资源权限才可以访问
         * role：必须有角色权限才可以放
         */
        Map<String, String> filterMap = new LinkedHashMap<>();
        filterMap.put("/*", "authc");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterMap);
        shiroFilterFactoryBean.setSecurityManager(securityManager);
        return shiroFilterFactoryBean;
    }
}
```
### 2.2.2 简单演示authc
- 1 修改pom文件，增加web和thymeleaf依赖
`spring-boot-starter-web和spring-boot-starter-thymeleaf`
- 2 增加权限演示类CommonController
```
package cn.ithzp.shiro.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class CommonController {
    @RequestMapping("/bridge")
    public String bridge(Model model) {
        log.info("进入权限跳转页面");
        model.addAttribute("name", "权限跳转页面");
        return "bridge";
    }
}
```
- 3 增加前端页面resources/templates/bridge.html
```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>权限跳转页面</title>
</head>
<body>
<h3 th:text="${name}"></h3>
<hr/>
进入用户添加功能：<a href="add">用户添加</a></br>
进入用户更新功能：<a href="update">用户更新</a>
</body>
</html>
```
- 4 测试验证
```
访问http://localhost:8080/bridge
- 结果1：ShiroConfig中取消注解@Configuration，可以正常访问
- 结果2：ShiroConfig中增加注解@Configuration，页面404，跳转到http://localhost:8080/login.jsp
```
### 2.2.3 编写登录逻辑
- 1 修改ShiroConfig,增加登录权限,增加访问失败后跳转登录页面
```
        // 需要放到authc的前面
        filterMap.put("/login", "anon");
        filterMap.put("/*", "authc");
        shiroFilterFactoryBean.setLoginUrl("/toLogin");
```
- 2 修改CommonController，增加跳转登录页方法和登录验证方法
```
     @RequestMapping("/toLogin")
     public String toLogin() {
         log.info("跳转登录页");
         return "/login";
     }
 
     @RequestMapping("/login")
     public String login(String userName, String passwd, Model model) {
         log.info("进入登录操作，参数userName={}，passwd={}", userName, passwd);
         // 1 获取用户主体
         Subject subject = SecurityUtils.getSubject();
         // 2 封装用户数据
         UsernamePasswordToken token = new UsernamePasswordToken(userName, passwd);
         // 3 执行登录
         try {
             subject.login(token);
             log.info("登录成功");
             return "redirect:bridge";
         } catch (UnknownAccountException u) {
             model.addAttribute("msg", "用户不存在");
             return "login";
         } catch (IncorrectCredentialsException i) {
             log.info("密码错误");
             model.addAttribute("msg", "密码错误");
             return "login";
         }
     }
```
- 3 修改UserRealm，修改登录认证
```
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        log.info("执行认证逻辑");
        String userName = "a";
        String password = "b";
        // 1 用户名验证
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        if (!StringUtils.equals(userName, token.getUsername())) {
            log.info("用户不存在");
            // 会抛出UnknownAccountException
            return null;
        }
        // 2 密码验证
        return new SimpleAuthenticationInfo("", password, "");
    }
```
- 4 增加前端页面resources/templates/login.html
```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>登录页面</title>
</head>
<h3>登录</h3>
<h3 th:text="${msg}" style="color:red"></h3>
<body>
<form method="post" action="/login">
    用户名：<input type="text" name="userName" /></br>
    密码：<input type="password" name="passwd"></br>
    <input type="submit" value="登录"/>
</form>
</body>
</html>
```
- 5 测试验证
```
1 浏览器访问http://localhost:8080/bridge
- 结果跳转到http://localhost:8080/toLogin
2 输入错误用户名提示用户不存在
3 输入错误密码提示密码错误
4 输入正确用户名密码，页面重定向到http://localhost:8080/bridge
```
### 2.2.4 编写授权逻辑
- 1 修改ShiroConfig，增加资源权限，设置未授权跳转页
```
        filterMap.put("/add", "perms[user:add]");
        filterMap.put("/update", "perms[user:update]");
        filterMap.put("/*", "authc");
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauth");
```
- 2 修改CommonControler，增加方法add/update/unauth
```
    @RequestMapping("/add")
    public String add() {
        log.info("执行用户添加");
        return "/user/add";
    }

    @RequestMapping("/update")
    public String update() {
        log.info("执行用户更新");
        return "/user/update";
    }

    @RequestMapping("/unauth")
    @ResponseBody
    public String unauth() {
        log.info("未授权");
        return "亲，您还未被授权";
    }
```
- 3 增加前端页面/templates/user/add.html和update.html
```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8"/>
    <title>用户添加页面</title>
</head>
<body>
用户添加
</body>
</html>
```
- 4 修改UserRealm，增加授权
```
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("执行授权逻辑");
        // 对资源进行授权
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
//        info.addStringPermission("user:add");
        info.addStringPermission("user:update");
        return info;
    }
```
- 5 测试
```
1 访问http://localhost:8080/toLogin后执行登录操作
- 结果会跳转到http://localhost:8080/bridge
2 点击页面中用户添加，跳转http://localhost:8080/unauth，提示未授权
3 点击页面中用户更新，跳转http://localhost:8080/update，提示更新
```
## 2.3 shiro动态数据(boot集成mybatis)
### 2.3.1 基础搭建
- 1 修改pom引入mybatis
```
        <com.alibaba.druid.version>1.1.22</com.alibaba.druid.version>
        <mysql-connector-java.version>5.1.42</mysql-connector-java.version>
        <mybatis-spring-boot-starter>1.1.1</mybatis-spring-boot-starter>
```
- 2 增加配置/resources/application.properties
```
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/shiro?useUnicode=true&characterEncoding=utf-8&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root

spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
mybatis.typeAliasesPackage=cn.ithzp.shiro.domain
mybatis.mapper-locations=classpath:mapper/*.xml
# 打印sql日志
loggin.level.cn.ithzp.shiro.mapper=debug
```
- 3 创建表/增加实体类
```
package cn.ithzp.shiro.domain;

import lombok.Data;

@Data
public class UserDomain {
    private int id;
    private String userName;
    private String passwd;
    private String perms;
}
```
- 4 增加持久层cn.ithzp.shiro.mapper.UserMapper
```
package cn.ithzp.shiro.mapper;

import cn.ithzp.shiro.domain.UserDomain;

public interface UserMapper {
    UserDomain findByUserName(String userName);
}
```
- 5 增加xml映射src/main/resources/mapper/UserMapper.xml
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="cn.ithzp.shiro.mapper.UserMapper">
    <select id="findByUserName" parameterType="string" resultType="UserDomain">
        select id, user_name as userName, passwd from user where user_name=#{value}
    </select>
</mapper>
```
- 6 增加业务处理service和serviceImpl
```
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
}
```
- 7 启动类增加mapper的扫描
```
@SpringBootApplication
@MapperScan("cn.ithzp.shiro.mapper")
```

### 2.3.2 采用数据库登录验证
- 1 修改UserRealm的登录验证方法
```
 // 1 用户名验证
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        UserDomain dbUser = userService.findByUserName(token.getUsername());
        log.info("数据库用户信息为：{}", dbUser);
        if (dbUser == null) {
            log.info("用户不存在");
            // 会抛出UnknownAccountException
            return null;
        }
        // 2 密码验证
        return new SimpleAuthenticationInfo("", dbUser.getPasswd(), "");
```
- 2 测试验证
```
访问http://localhost:8080/toLogin后执行登录操作，输入数据库用户名和密码可以正常跳转
```

### 2.3.3 采用数据库进行授权操作
- 1 修改UserRealm，修改登录认证后密码验证，将对象传递到授权方法中
```
        // 2 密码验证
        return new SimpleAuthenticationInfo(dbUser , dbUser.getPasswd(), "");
```
- 2 在service/impl/mapper/xml中增加查询授权方法
```
    <select id="findById" parameterType="string" resultType="UserDomain">
        select id, user_name as userName, passwd, perms from user where id=${value}
    </select>
```
- 3 修改UserRealm，修改授权方法，权限从数据库动态获取
```
        // 对资源进行授权
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 获取subject对象
        Subject subject = SecurityUtils.getSubject();
        UserDomain loginUser = (UserDomain) subject.getPrincipal();
        UserDomain dbUser = userService.findById(loginUser.getId());
        info.addStringPermission(dbUser.getPerms());
        return info;
```
- 4 测试验证
```
1 访问http://localhost:8080/toLogin后采用jack登录，
- 跳转http://localhost:8080/bridge
2 点击“用户添加”，跳转到http://localhost:8080/add，提示用户添加
3 点击“用户更新”，跳转到http://localhost:8080/unauth，提示未授权
```

## 2.4 thymeleaf整合shiro
- 1 修改pom，增加依赖
```
<com.github.theborakompanioni.thymeleaf-extras-shiro>2.0.0</com.github.theborakompanioni.thymeleaf-extras-shiro>
```
- 2 修改ShiroConfig，增加配置
```
    /**
     * 配置ShiroDialect，用于thymeleaf和shiro标签配合使用
     */
    @Bean
    public ShiroDialect getShiroDialect() {
        return new ShiroDialect();
    }
```
- 3 修改Bridge.html，增加shiro权限
```
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org"
      xmlns:shiro="http://www.w3.org/1999/xhtml">
<head>
    <meta charset="UTF-8">
    <title>权限跳转页面</title>
</head>
<body>
<h3 th:text="${name}"></h3>
<hr/>
<div shiro:hasPermission="user:add">
    进入用户添加功能：<a href="add">用户添加</a></br>
</div>
<div shiro:hasPermission="user:update">
    进入用户更新功能：<a href="update">用户更新</a>
</div>
</body>
</html>
```
- 4 测试
```
访问登录页http://localhost:8080/toLogin
- 采用jack登录，只能查看用户添加
- 采用tom登录，只能查看用户更新
```
