package cn.ithzp.shiro.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class CommonController {
    @RequestMapping("/bridge")
    public String bridge(Model model) {
        log.info("进入权限跳转页面");
        model.addAttribute("name", "权限跳转页面");
        return "/bridge";
    }

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
}
