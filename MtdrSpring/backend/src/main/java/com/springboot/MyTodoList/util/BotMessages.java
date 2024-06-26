package com.springboot.MyTodoList.util;

public enum BotMessages {
	
	HELLO_MANAGER("🤖 ¡Hola! Soy Botacle, tu bot de lista de tareas. Aquí están los comandos que puedes usar:\n\n"
                + "📝 /start - Iniciar y obtener un resumen\n"
                + "📋 /todolist - Ver tu lista de tareas\n"
                + "➕ /additem - Añadir una nueva tarea\n"
                + "👥 /employeeslist - Ver las tareas de tus empleados\n"
                + "❌ /cancel - Cancelar la acción actual\n\n"
                + "!Estoy aquí para ayudarte a gestionar tus tareas de manera más eficiente!"),
        HELLO_MEMBER("🤖 ¡Hola! Soy Botacle, tu bot de lista de tareas. Aquí están los comandos que puedes usar:\n\n"
                + "📝 /start - Iniciar y obtener un resumen\n"
                + "📋 /todolist - Ver tu lista de tareas\n"
                + "➕ /additem - Añadir una nueva tarea\n"
                + "❌ /cancel - Cancelar la acción actual\n\n"
                + "!Estoy aquí para ayudarte a gestionar tus tareas de manera más eficiente!"),
        SUMMARY("Puedes usar estos comandos:\n/todolist para ver tus tareas\n/additem para agregar una tarea\n"),
	BOT_REGISTERED_STARTED("Bot registered and started succesfully!"),
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