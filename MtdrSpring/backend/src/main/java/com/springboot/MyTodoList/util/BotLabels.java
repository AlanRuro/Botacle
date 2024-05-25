package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	ADD_ITEM("Add New Item"),
	DONE("DONE"),
	UNDO("UNDO"),
    CANCEL("Cancel action"),
	DELETE("DELETE"),
	MY_TODO_LIST("MY TODO LIST"),
	MY_EMPLOYEES_LIST("MY EMPLOYEES LIST"),
    LOGIN("Login"),
	LOGOUT("Logout"),
	DASH("-");

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}