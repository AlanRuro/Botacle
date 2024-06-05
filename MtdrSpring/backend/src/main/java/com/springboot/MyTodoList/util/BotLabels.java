package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	START("Start 🚀"),
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	ADD_ITEM("Add New Task ✏️"),
	DONE("DONE"),
	UNDO("UNDO"),
        CANCEL("Cancel action 🚫"),
	DELETE("DELETE"),
	MY_TODO_LIST("List all the tasks 📝"),
	MY_EMPLOYEES_LIST("MY EMPLOYEES LIST"),
        LOGIN("Login"),
	LOGOUT("Sign out"),
	DASH("-");

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}