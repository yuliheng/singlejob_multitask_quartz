package com.youlu.server.task.util;

import com.youlu.server.task.entity.ConstantValue;
import com.youlu.server.task.entity.JwtCheckResult;
import io.jsonwebtoken.*;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;

/**
 * @author yangyang.duan
 * @Description
 * @date 2021/5/7
 */
public class JwtUtil {


    public static String createJWT(String id, String username) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Key secretKey = generalKey();
        JwtBuilder builder = Jwts.builder()
                .setId(id)
                .setSubject(username)   // 主题
                .setIssuer("user")     // 签发者
                .setIssuedAt(now)      // 签发时间
                .signWith(SignatureAlgorithm.HS256, secretKey); // 签名算法以及密匙
        long expMillis = nowMillis + ConstantValue.JWT_TTL;
        Date expDate = new Date(expMillis);
        builder.setExpiration(expDate); // 过期时间
        return builder.compact();
    }

    public static JwtCheckResult validateJWT(String jwtStr) {
        JwtCheckResult jwtCheckResult = new JwtCheckResult();
        Claims claims = null;
        try {
            claims = parseJWT(jwtStr);
            jwtCheckResult.setSuccess(true);
            jwtCheckResult.setClaims(claims);
        } catch (ExpiredJwtException e) {
            jwtCheckResult.setErrCode(ConstantValue.JWT_ERRCODE_EXPIRE);
            jwtCheckResult.setSuccess(false);
        } catch (SignatureException e) {
            jwtCheckResult.setErrCode(ConstantValue.JWT_ERRCODE_FAIL);
            jwtCheckResult.setSuccess(false);
        } catch (Exception e) {
            jwtCheckResult.setErrCode(ConstantValue.JWT_ERRCODE_FAIL);
            jwtCheckResult.setSuccess(false);
        }
        return jwtCheckResult;
    }


    private static Key generalKey() {
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary("bankgl");
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());
        return signingKey;
    }

    private static Claims parseJWT(String jwt) throws Exception {
        Key secretKey = generalKey();
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(jwt)
                .getBody();
    }
}
