/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.springboot.MyTodoList.util;

import java.util.ArrayList;
import java.util.List;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;


public class BotCommandFactory {
    public static SetMyCommands getCommandsForManager(long chatId) {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand(BotCommands.ADD_ITEM.getCommand(), BotLabels.ADD_ITEM.getLabel()));
        botCommands.add(new BotCommand(BotCommands.TODO_LIST.getCommand(), BotLabels.MY_TODO_LIST.getLabel()));
        botCommands.add(new BotCommand(BotCommands.CANCEL.getCommand(), BotLabels.CANCEL.getLabel()));
        botCommands.add(new BotCommand(BotCommands.HIDE.getCommand(), BotLabels.HIDE_MAIN_SCREEN.getLabel()));
        botCommands.add(new BotCommand(BotCommands.LOGOUT.getCommand(), BotLabels.LOGOUT.getLabel()));
        return getCommandsForChat(chatId, botCommands);
    }
    
    public static SetMyCommands getCommandsForEmployee(long chatId) {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand(BotCommands.ADD_ITEM.getCommand(), BotLabels.ADD_ITEM.getLabel()));
        botCommands.add(new BotCommand(BotCommands.TODO_LIST.getCommand(), BotLabels.MY_TODO_LIST.getLabel()));  
        botCommands.add(new BotCommand(BotCommands.CANCEL.getCommand(), BotLabels.CANCEL.getLabel()));
        botCommands.add(new BotCommand(BotCommands.LOGOUT.getCommand(), BotLabels.LOGOUT.getLabel()));
        return getCommandsForChat(chatId, botCommands);
    }
    
    public static SetMyCommands getCommandsForNoMember(long chatId) {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand(BotCommands.LOGIN.getCommand(), BotLabels.LOGIN.getLabel()));
        return getCommandsForChat(chatId, botCommands);
    }
    
    private static SetMyCommands getCommandsForChat(long chatId, List<BotCommand> commands) {
        BotCommandScopeChat commandScope = new BotCommandScopeChat(Long.toString(chatId)); // Command available only in the specified group chat
        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        setMyCommands.setScope(commandScope);
        return setMyCommands;
    }
}