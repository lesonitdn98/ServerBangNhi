package com.bangnhi.note.utils;

import com.bangnhi.note.data.repository.JWTRepository;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AppUtils {

    public static String convertByteToHex1(byte[] data) {
        BigInteger number = new BigInteger(1, data);
        StringBuilder hashText = new StringBuilder(number.toString(16));
        while (hashText.length() < 32) {
            hashText.insert(0, "0");
        }
        return hashText.toString();
    }

    public static String getMD5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            return convertByteToHex1(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Boolean validateAuthToken(String token, JWTRepository jwtRepository) {
        return StringUtils.hasText(token) &&
                JwtTokenUtils.validateToken(token) &&
                (jwtRepository.findByToken(token) != null);
    }
}
