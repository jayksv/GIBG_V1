package com.Auton.GIBGMain.myfuntion;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@Service
public class IdGeneratorService {
    public String generateUserId() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomChars = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 5; i++) { // เพิ่มจำนวนตัวอักษรตามที่คุณต้องการ
            randomChars.append(characters.charAt(random.nextInt(characters.length())));
        }

        return randomChars.toString();
    }
}
