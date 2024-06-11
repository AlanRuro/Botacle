package com.springboot.MyTodoList.util;

public enum BotMessages {
	
	HELLO_MYTODO_BOT("Hola! Soy un bot para que los empleados de Oracle puedan organizarse mejor!\nPara usarme por favor usa el comando /login."),
        SUMMARY("Puedes usar estos comandos:\n/todolist para ver tus tareas\n/additem para agregar una tarea\n/logout para cerrar sesion"),
	BOT_REGISTERED_STARTED("Bot registered and started succesfully!"),
        LOGIN("Por favor, usa /login para iniciar sesión."),
	ITEM_DONE("Item done! Select /todolist to return to the list of todo items, or /start to go to the main screen."), 
	ITEM_UNDONE("Item undone! Select /todolist to return to the list of todo items, or /start to go to the main screen."), 
	ITEM_DELETED("Item deleted! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
	TYPE_NEW_TODO_ITEM("Ingresa el nombre de la tarea"),
	NEW_ITEM_ADDED("New item added! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
        UNKNOWN_TEXT("Favor de ingresar un comando válido. Usa /start para ver los comandos disponibles."),
        NOT_MEMBER("No eres usuario"),
	BYE("Bye! Select /start to resume!");

	private String message;

	BotMessages(String enumMessage) {
		this.message = enumMessage;
	}

	public String getMessage() {
		return message;
	}

}