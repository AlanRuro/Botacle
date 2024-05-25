package com.springboot.MyTodoList.util;

public enum BotMessages {
	
	HELLO_MYTODO_BOT("¡Hola! Soy un bot para que los empleados de Oracle puedan organizarse mejor."),
    SUMMARY("Puedes usar estos comandos:\n/todolist - Ver tus tareas\n/additem - Agregar una tarea\n/logout - Cerrar sesión"),
	BOT_REGISTERED_STARTED("¡Bot registrado y iniciado con éxito!"),
    LOGIN("Por favor, usa /login para iniciar sesión."),
	ITEM_DONE("¡Tarea completada! Usa /todolist para volver a la lista de tareas o /start para ir a la pantalla principal."), 
	ITEM_UNDONE("¡Tarea marcada como pendiente! Usa /todolist para volver a la lista de tareas o /start para ir a la pantalla principal."), 
	ITEM_DELETED("¡Tarea eliminada! Usa /todolist para volver a la lista de tareas o /start para ir a la pantalla principal."),
	TYPE_NEW_TODO_ITEM("Ingresa el nombre de la tarea"),
	NEW_ITEM_ADDED("¡Nueva tarea añadida! Usa /todolist para volver a la lista de tareas o /start para ir a la pantalla principal."),
    UNKNOWN_TEXT("No entiendo lo que quieres decir. Usa /start."),
    NOT_MEMBER("No eres usuario autorizado."),
	BYE("¡Adiós! Usa /start para continuar.");

	private String message;

	BotMessages(String enumMessage) {
		this.message = enumMessage;
	}

	public String getMessage() {
		return message;
	}

}
