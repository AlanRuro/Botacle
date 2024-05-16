/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.springboot.MyTodoList.util;

public enum BotState {
    AWAITING_USERNAME,
    AWAITING_PASSWORD,
    AUTHENTICATED,
    AWAITING_TASK_NAME, 
    AWAITING_TASK_DESCRIPTION, 
    AWAITING_TASK_START_DATE, 
    AWAITING_TASK_END_DATE;
}

