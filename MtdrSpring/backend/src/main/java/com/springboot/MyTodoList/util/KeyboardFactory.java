/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.springboot.MyTodoList.util;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonDefault;


public class KeyboardFactory {
    
    public static ReplyKeyboardMarkup getEmployeeKeyboard() {
        
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add(BotLabels.MY_TODO_LIST.getLabel());
        row.add(BotLabels.ADD_ITEM.getLabel());
        keyboard.add(row);

//        row = new KeyboardRow();
//        row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
//        row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
//        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        
        return keyboardMarkup;
    }
}
