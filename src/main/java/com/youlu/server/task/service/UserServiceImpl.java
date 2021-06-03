//package com.youlu.server.task.service;
//
//import com.youlu.server.task.entity.UserInfo;
//import com.youlu.server.task.util.JwtUtil;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * @author yangyang.duan
// * @Description
// * @date 2021/5/7
// */
//@Service
//public class UserServiceImpl implements UserService {
//
//    private List<UserInfo> userInfoList = new ArrayList<>();
//
//    {
//        UserInfo userInfo = new UserInfo();
//        userInfo.setUsername("admin");
//        userInfo.setPassword("111111");
//        userInfo.setToken("");
//        userInfoList.add(userInfo);
//    }
//
////    @Autowired
////    private UserMapper userMapper;
//
//    @Override
//    public synchronized String login(String username, String password) {
//        String token = null;
//        for(UserInfo userInfo : userInfoList){
//            if(userInfo.getUsername().equals(username)){
//                String pwd = userInfo.getPassword();
//                if(pwd != null && pwd.equals(password)){
//                    token = JwtUtil.createJWT("1", username);
//                    userInfo.setToken(token);
//                }
//            }
//        }
//        return token;
//    }
//
//    @Override
//    public synchronized boolean logout(String token) {
//        UserInfo userInfo = getUserInfo(token);
//        userInfo.setToken("");
//        return true;
//    }
//
//    @Override
//    public UserInfo getUserInfo(String token) {
////        UserInfoVO userInfoVO = new UserInfoVO();
////        JwtCheckResult jwtCheckResult = JwtUtil.validateJWT(token);
////        if(jwtCheckResult.getSuccess()){
////            Claims claims = jwtCheckResult.getClaims();
////            String username = claims.getSubject();
////            userInfoVO.setName(username);
////            return userInfoVO;
////        }
//        for (UserInfo userInfo : userInfoList) {
//            if(userInfo.getToken().equals(token)){
//                return userInfo;
//            }
//        }
//        return null;
//    }
//}
