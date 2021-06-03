//package com.youlu.server.task.controller;
//
//import com.youlu.server.task.controller.vo.ResultVO;
//import com.youlu.server.task.controller.param.UserLoginWebParam;
//import com.youlu.server.task.controller.vo.UserInfoVO;
//import com.youlu.server.task.controller.vo.UserLoginVO;
//import com.youlu.server.task.entity.UserInfo;
//import com.youlu.server.task.service.UserService;
//import com.youlu.server.task.util.ResultVOUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//
///**
// * @author yangyang.duan
// * @Description
// * @date 2021/4/17
// */
//@Slf4j
//@RestController
//@RequestMapping("/user")
//public class UserController {
//
//    @Autowired
//    private UserService userService;
//
//
//
////    @PostMapping("/login_test")
////    public ResultVO<UserLoginVO> loginTest(@RequestBody UserLoginWebParam webParam){
////        log.info("login be called with webParam:{}", webParam.toString());
////        String username = webParam.getUsername();
////        UserLoginVO userLoginVO = new UserLoginVO();
////        userLoginVO.setToken(username + "-token");
////        return ResultVOUtil.success(userLoginVO);
////    }
//
//    @PostMapping("/login")
//    public ResultVO<UserLoginVO> login(@RequestBody UserLoginWebParam webParam){
//        log.info("login be called with webParam:{}", webParam.toString());
//        String username = webParam.getUsername();
//        String password = webParam.getPassword();
//        String token = userService.login(username, password);
//        if(token != null){
//            //登录成功，返回token
//            UserLoginVO userLoginVO = new UserLoginVO();
//            userLoginVO.setToken(token);
//            return ResultVOUtil.success(userLoginVO);
//        }else {
//            //登录失败
//            return ResultVOUtil.error(60204,"Account and password are incorrect.");
//        }
//    }
//
//    @GetMapping("/info")
//    public ResultVO<UserInfoVO> getUserInfo(@RequestParam("token") String token){
//        log.info("login be called with token:{}", token);
//        UserInfo userInfo = userService.getUserInfo(token);
//        UserInfoVO vo = new UserInfoVO();
//        vo.setName(userInfo.getUsername());
//        return ResultVOUtil.success(vo);
//    }
//
//    @PostMapping("/logout")
//    public ResultVO<Boolean> logout(){
//        log.info("logout be called ");
//        return ResultVOUtil.success(true);
//    }
//}
//
//
//
//
//
//
//
