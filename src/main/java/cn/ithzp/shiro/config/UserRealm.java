package cn.ithzp.shiro.config;

import cn.ithzp.shiro.domain.UserDomain;
import cn.ithzp.shiro.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.UserDataHandler;

@Slf4j
public class UserRealm extends AuthorizingRealm {
    @Autowired
    private UserService userService;

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("执行授权逻辑");
//        // 对资源进行授权
//        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
////        info.addStringPermission("user:add");
//        info.addStringPermission("user:update");
//        return info;

        // 对资源进行授权
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        // 获取subject对象
        Subject subject = SecurityUtils.getSubject();
        UserDomain loginUser = (UserDomain) subject.getPrincipal();
        UserDomain dbUser = userService.findById(loginUser.getId());
        info.addStringPermission(dbUser.getPerms());
        return info;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        log.info("执行认证逻辑");
        // 1 用户名验证
//        String userName = "a";
//        String password = "b";
//        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
//        if (!StringUtils.equals(userName, token.getUsername())) {
//            log.info("用户不存在");
//            // 会抛出UnknownAccountException
//            return null;
//        }
//        // 2 密码验证
//        return new SimpleAuthenticationInfo("", password, "");
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
        return new SimpleAuthenticationInfo(dbUser, dbUser.getPasswd(), "");
    }
}
