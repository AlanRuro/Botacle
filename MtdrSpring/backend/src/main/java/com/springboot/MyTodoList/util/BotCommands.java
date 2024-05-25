package com.springboot.MyTodoList.util;

public enum BotCommands {

	START("/start"), 
	HIDE("/hide"), 
    LOGIN("/login"),
    LOGOUT("/logout"),
	TODO_LIST("/todolist"),
	ADD_ITEM("/additem"),
    CANCEL("/cancel"),
	EMPLOYEES_LIST("/employeeslist");

	private String command;

	BotCommands(String enumCommand) {
		this.command = enumCommand;
	}

	public String getCommand() {
		return command;
	}
}