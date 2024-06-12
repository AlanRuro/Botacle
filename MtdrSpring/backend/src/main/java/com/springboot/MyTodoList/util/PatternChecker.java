package com.springboot.MyTodoList.util;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternChecker {

    public static boolean hasSpecialCharacters(String text) {
        Pattern p = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]/~]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        return m.find();
    }
    
    public static boolean isUTF8(String str) { 
        try { 
            byte[] utf8Bytes = str.getBytes(StandardCharsets.UTF_8); 
            String roundTripped = new String(utf8Bytes, StandardCharsets.UTF_8); 
            return str.equals(roundTripped); 
        } catch (Exception e) { 
            return false;
        } 
    } 
}
