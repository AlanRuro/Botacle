package com.springboot.MyTodoList.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.service.MemberService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.TaskSessionService;
import com.springboot.MyTodoList.util.BotCommandFactory;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    private final TaskService taskService;
    private final TaskSessionService taskSessionService;
    private final MemberService memberService;
    private final String botName;

    public ToDoItemBotController(String botToken, String botName, TaskService taskService, MemberService memberService, TaskSessionService taskSessionService) {
        super(botToken);
        logger.info("Bot Token: " + botToken);
        logger.info("Bot name: " + botName);
        this.taskSessionService = taskSessionService;
        this.taskService = taskService;
        this.memberService = memberService;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextFromTelegram = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            if (getMember(userId) == null) {
                send(chatId, "No eres miembro");
                SetMyCommands commands = BotCommandFactory.getCommandsForNoMember(chatId);
                sendCommandsToBot(commands);
            } else {
                handleReplies(chatId, userId, messageTextFromTelegram);
                SetMyCommands commands = BotCommandFactory.getCommandsForEmployee(chatId);
                sendCommandsToBot(commands);
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            handleCallbacks(chatId, callbackData, update);
        } 
    }

    private void handleReplies(long chatId, long userId, String message) {
        TaskDto taskSessionDto = taskSessionService.getTaskSession(chatId);
        if (taskSessionDto != null) {
            logger.info("Task session: " + taskSessionDto.getTaskSessionId());
            logger.info("Task session: " + taskSessionDto.getTaskSessionId());
            handleTaskSession(chatId, taskSessionDto, message);
        } else {
            MemberDto memberDto = getMember(userId);
            handleAuthenticatedCommands(chatId, memberDto, message);
        }
    }

    private void handleCallbacks(long chatId, String data, Update update) {
        Message message = (Message) update.getCallbackQuery().getMessage();
        if (message != null) {
            int messageId = message.getMessageId();

            if (data.equals(BotCommands.TODO_LIST.getCommand())) {
                sendTaskTypeOptions(chatId);
            } else if (data.equals(BotCommands.ADD_ITEM.getCommand())) {
                replyToAddTask(chatId, getMember(chatId));
            } else if (data.equals(BotCommands.CANCEL.getCommand())) {
                cancelAction(chatId);
            } else if (data.startsWith("TaskType-")) {
                handleTaskTypeCallback(chatId, data);
            } else if (data.startsWith("Task")) {
                data = data.substring(4);
                handleTaskCallback(chatId, data);
            } else if (data.startsWith("Session")) {
                data = data.substring(7);
                handleTaskSessionCallback(chatId, data);
            } else if (data.startsWith("Edit")) {
                data = data.substring(4);
                handleTaskEdit(chatId, data);
            } else if (data.startsWith("Employee-")) {
                handleEmployeeCallback(chatId, data);
            } else if (data.startsWith("Delete-")) {
                handleTaskDelete(chatId, data);
            }

            // Clear the inline keyboard after handling the callback
            clearInlineKeyboard(chatId, messageId);
        } else {
            logger.error("Message is inaccessible or message ID is null");
        }
    }

    private void handleTaskTypeCallback(long chatId, String data) {
        if (data.equals("TaskType-Undone")) {
            replyToListToDo(chatId, getMember(chatId), false);
        } else if (data.equals("TaskType-Done")) {
            replyToListToDo(chatId, getMember(chatId), true);
        }
    }

    private void handleTaskCallback(long chatId, String data) {
        if (data.startsWith("Active-")) {
            int taskId = Integer.parseInt(data.substring(7));
            TaskDto taskDto = taskService.getTaskById(taskId);
            if (taskDto != null) {
                sendEditMenu(chatId, taskDto);
            }
        } else if (data.startsWith("Done-")) {
            int taskId = Integer.parseInt(data.substring(5));
            TaskDto taskDto = taskService.getTaskById(taskId);
            if (taskDto != null) {
                String taskMessage = String.format(
                    "*Nombre:* %s\n" +
                    "*Descripción:* %s\n" +
                    "*Fecha de inicio:* %s\n" +
                    "*Fecha de fin:* %s",
                    taskDto.getName(),
                    taskDto.getDescription(),
                    taskDto.getStartDate() != null ? taskDto.getStartDate().toString() : "No especificada",
                    taskDto.getEndDate() != null ? taskDto.getEndDate().toString() : "No especificada"
                );                

                sendMarkdown(chatId, taskMessage);
            }
        }
    }

    private void handleTaskSessionCallback(long chatId, String data) {
        if (data.startsWith("TaskYes-")) {
            long id = Long.parseLong(data.substring(8));
            TaskDto taskDto = taskSessionService.getTaskSession(id);
            if (taskDto != null) {
                TaskDto newTaskDto = taskService.addTask(taskDto);
                logger.info("Task Session ID: " + taskDto.getTaskSessionId() + " Task ID: " + newTaskDto.getTaskId());
                taskDto.setTaskId(newTaskDto.getTaskId());
                taskSessionService.updateTask(chatId, taskDto);
                taskSessionService.confirmTaskSession(id);
                send(chatId, "Tarea agregada ✅");
            }
        } else if (data.startsWith("TaskNo-")) {
            long id = Long.parseLong(data.substring(7));
            taskSessionService.deleteTaskSession(id);
            send(chatId, "Tarea eliminada ❌");
        }
    }

    private void handleEmployeeCallback(long chatId, String data) {
        long telegramId = Long.parseLong(data.split("-")[1]);
        List<TaskDto> tasks = taskService.getTasksByTelegramId(telegramId);

        if (tasks.isEmpty()) {
            send(chatId, "El empleado no tiene tareas.");
        } else {
            sendTasksList(chatId, "Tareas del empleado:", tasks, false);
        }
    }

    private void handleTaskEdit(long chatId, String data) {
        if (data.startsWith("Name-")) {
            int taskId = Integer.parseInt(data.substring(5));
            TaskDto taskDto = taskService.getTaskById(taskId);
            if (taskDto != null) {
                send(chatId, "Ingrese el nuevo nombre:");
                TaskDto newTaskSession = taskSessionService.createEmptyTask(chatId, getMember(taskDto.getMemberId()), true);
                newTaskSession.setName(null);
                newTaskSession.setDescription(taskDto.getDescription());
                newTaskSession.setStartDate(taskDto.getStartDate());
                newTaskSession.setEndDate(taskDto.getEndDate());
                newTaskSession.setTaskId(taskId);
                taskSessionService.updateTask(chatId, newTaskSession);
            }
        } else if (data.startsWith("Desc-")) {
            int taskId = Integer.parseInt(data.substring(5));
            TaskDto taskDto = taskService.getTaskById(taskId);
            if (taskDto != null) {
                send(chatId, "Ingrese la nueva descripción:");
                TaskDto newTaskSession = taskSessionService.createEmptyTask(chatId, getMember(taskDto.getMemberId()), true);
                newTaskSession.setName(taskDto.getName());
                newTaskSession.setDescription(null);
                newTaskSession.setStartDate(taskDto.getStartDate());
                newTaskSession.setEndDate(taskDto.getEndDate());
                newTaskSession.setTaskId(taskId);
                taskSessionService.updateTask(chatId, newTaskSession);
            }
        } else if (data.startsWith("Done-")) {
            int taskId = Integer.parseInt(data.substring(5));
            TaskDto taskDto = taskService.getTaskById(taskId);
            if (taskDto != null) {
                taskDto.setIsDone(true);
                taskService.updateTask(taskDto);
                send(chatId, "Tarea hecha");
            }
        } else if (data.startsWith("Delete-")) {
            handleTaskDelete(chatId, data);
        }
    }    

    private void handleTaskDelete(long chatId, String data) {
        int taskId = Integer.parseInt(data.substring(7));
        logger.info("Attempting to delete task with ID: " + taskId);
        
        // Fetch task before deletion to verify it exists
        TaskDto taskBeforeDelete = taskService.getTaskById(taskId);
        if (taskBeforeDelete == null) {
            logger.warn("Task with ID " + taskId + " does not exist.");
            send(chatId, "Tarea no encontrada 😕");
            return;
        }
        
        boolean deleted = taskService.deleteTask(taskId);
        
        // Double-check if the task is still in the database
        TaskDto taskAfterDelete = taskService.getTaskById(taskId);
        logger.info("Task after deletion: " + (taskAfterDelete == null ? "null" : taskAfterDelete.getTaskId()));
        
        if (deleted && taskAfterDelete == null) {
            logger.info("Task deleted successfully: " + taskId);
            send(chatId, "Tarea eliminada 🗑️");
        } else {
            logger.warn("Failed to delete task: " + taskId);
            send(chatId, "Tarea no eliminada 😕");
        }
    }
    

    private void sendEditMenu(long chatId, TaskDto taskDto) {
        InlineKeyboardMarkup editKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
    
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton nameButton = new InlineKeyboardButton();
        nameButton.setText("Editar nombre ✏️");
        nameButton.setCallbackData("EditName-" + taskDto.getTaskId());
        InlineKeyboardButton descButton = new InlineKeyboardButton();
        descButton.setText("Editar descripción 💬");
        descButton.setCallbackData("EditDesc-" + taskDto.getTaskId());
        row1.add(nameButton);
        row1.add(descButton);
    
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton doneButton = new InlineKeyboardButton();
        doneButton.setText("Marcar como hecho ✅");
        doneButton.setCallbackData("EditDone-" + taskDto.getTaskId());
        row2.add(doneButton);
    
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText("Eliminar tarea 🗑️");
        deleteButton.setCallbackData("Delete-" + taskDto.getTaskId());
        row3.add(deleteButton);
    
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Cancelar ❌");
        cancelButton.setCallbackData(BotCommands.CANCEL.getCommand());
        row4.add(cancelButton);
    
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        keyboardRows.add(row4);
        editKeyboardMarkup.setKeyboard(keyboardRows);
    
        String taskMessage = String.format(
            "*Nombre:* %s\n" +
            "*Descripción:* %s\n" +
            "*Fecha de inicio:* %s\n" +
            "*Fecha de fin:* %s",
            taskDto.getName(),
            taskDto.getDescription(),
            taskDto.getStartDate() != null ? taskDto.getStartDate().toString() : "No especificada",
            taskDto.getEndDate() != null ? taskDto.getEndDate().toString() : "No especificada"
        );
        

        sendMarkdown(chatId, taskMessage);
        sendInlineKeyboard(chatId, "Acciones ⚙️", editKeyboardMarkup);
    }
    

    
    private void sendMarkdown(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void handleAuthenticatedCommands(long chatId, MemberDto memberDto, String message) {
        if (message.equals(BotCommands.START.getCommand())) {
            replyToStart(chatId);
        } else if (message.equals(BotCommands.TODO_LIST.getCommand())) {
            sendTaskTypeOptions(chatId);
        } else if (message.equals(BotCommands.ADD_ITEM.getCommand())) {
            replyToAddTask(chatId, memberDto);
        } else if (message.equals(BotCommands.EMPLOYEES_LIST.getCommand())) {
            if (memberDto.getIsManager()) {
                replyToEmployeesList(chatId, memberDto.getTelegramId());
            } else {
                send(chatId, "No tienes permisos para ver esta lista.");
            }
        } else if (message.equals(BotCommands.CANCEL.getCommand())) {
            cancelAction(chatId);
        } else {
            replyToUnkownText(chatId);
        }
    }

    private void handleTaskSession(long chatId, TaskDto newTaskSession, String text) {
        logger.info("isEdit? " + newTaskSession.getIsEdit());
        if (newTaskSession.getIsEdit()) {
            handleTaskSessionEdit(chatId, newTaskSession, text);
        } else {
            handleTaskSessionAdd(chatId, newTaskSession, text);
        }
    }

    private void handleTaskSessionAdd(long chatId, TaskDto newTaskSession, String text) {
        if (newTaskSession.getName() == null) {
            newTaskSession.setName(text);
            send(chatId, "Ingresa la descripcion");
        } else if (newTaskSession.getDescription() == null) {
            newTaskSession.setDescription(text);
            send(chatId, "Ingresa la fecha de inicio (YYYY-MM-DD):");
        } else if (newTaskSession.getStartDate() == null) {
            if (isValidDate(text)) {
                newTaskSession.setStartDate(LocalDate.parse(text));
                send(chatId, "Ingresa la fecha de fin (YYYY-MM-DD):");
            } else {
                send(chatId, "Fecha inválida. Ingresa la fecha de inicio (YYYY-MM-DD):");
            }
        } else if (newTaskSession.getEndDate() == null) {
            if (isValidDate(text)) {
                newTaskSession.setEndDate(LocalDate.parse(text));
                taskSessionService.updateTask(chatId, newTaskSession);
                sendTaskConfirmation(chatId, newTaskSession);
            } else {
                send(chatId, "Fecha inválida. Ingresa la fecha de fin (YYYY-MM-DD):");
            }
        }
        taskSessionService.updateTask(chatId, newTaskSession);
    }

    private void sendTaskConfirmation(long chatId, TaskDto newTaskSession) {
        send(chatId, "Nueva tarea:");
        InlineKeyboardMarkup infoKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Confirmar");
        yesButton.setCallbackData("SessionTaskYes-" + Long.toString(chatId));
        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("Cancelar");
        noButton.setCallbackData("SessionTaskNo-" + Long.toString(chatId));
        row.add(yesButton);
        row.add(noButton);
        keyboardRows.add(row);
        infoKeyboardMarkup.setKeyboard(keyboardRows);
        sendInlineKeyboard(chatId, newTaskSession.getName() + " " + newTaskSession.getDescription(), infoKeyboardMarkup);
    }

    private void handleTaskSessionEdit(long chatId, TaskDto newTaskSession, String text) {
        logger.info("Editing task session");
        String updateText = "";
        if (newTaskSession.getName() == null) {
            newTaskSession.setName(text);
            updateText = "Nombre actualizado con éxito! ✅";
        } else if (newTaskSession.getDescription() == null) {
            newTaskSession.setDescription(text);
            updateText = "Descripción actualizada con éxito ✅";
        }
        taskSessionService.updateTask(chatId, newTaskSession);
        taskSessionService.confirmTaskSession(chatId);
        taskService.updateTask(newTaskSession);
        send(chatId, updateText);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    private void sendBasicCommands(long chatId) {
        SetMyCommands commands = BotCommandFactory.getCommandsForNoMember(chatId);
        sendCommandsToBot(commands);
    }

    private void replyToUnkownText(long chatId) {
        send(chatId, BotMessages.UNKNOWN_TEXT.getMessage());
    }

    private void sendWelcomManagerMessage(long chatId) {
        String welcomeMessage = "🤖 ¡Hola! Soy Botacle, tu bot de lista de tareas. Aquí están los comandos que puedes usar:\n\n" +
                "📝 /start - Iniciar y obtener un resumen\n" +
                "📋 /todolist - Ver tu lista de tareas\n" +
                "➕ /additem - Añadir una nueva tarea\n" +
                "👥 /employeeslist - Ver las tareas de tus empleados\n" +
                "❌ /cancel - Cancelar la acción actual\n\n" +
                "¡Espero ayudarte a mantenerte organizado!";

        send(chatId, welcomeMessage);
    }

    private void sendWelcomManagerMessage(long chatId) {
        String welcomeMessage = "🤖 ¡Hola! Soy Botacle, tu bot de lista de tareas. Aquí están los comandos que puedes usar:\n\n" +
                "📝 /start - Iniciar y obtener un resumen\n" +
                "📋 /todolist - Ver tu lista de tareas\n" +
                "➕ /additem - Añadir una nueva tarea\n" +
                "👥 /employeeslist - Ver las tareas de tus empleados\n" +
                "❌ /cancel - Cancelar la acción actual\n\n" +
                "¡Espero ayudarte a mantenerte organizado!";

        send(chatId, welcomeMessage);
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeMessage = "🤖 ¡Hola! Soy Botacle, tu bot de lista de tareas. Aquí están los comandos que puedes usar:\n\n" +
                "📝 /start - Iniciar y obtener un resumen\n" +
                "📋 /todolist - Ver tu lista de tareas\n" +
                "➕ /additem - Añadir una nueva tarea\n" +
                "❌ /cancel - Cancelar la acción actual\n\n" +
                "¡Espero ayudarte a mantenerte organizado!";

        send(chatId, welcomeMessage);
    }

    private void replyToStart(long chatId) {

        MemberDto memberDto = getMember(chatId);
        if (memberDto.getIsManager()) {
            sendWelcomManagerMessage(chatId);
        } else {
            sendWelcomeMessage(chatId);
        }        


        MemberDto memberDto = getMember(chatId);
        if (memberDto.getIsManager()) {
            sendWelcomManagerMessage(chatId);
        } else {
            sendWelcomeMessage(chatId);
        }        

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton todolistButton = new InlineKeyboardButton();
        todolistButton.setText("Ver tareas 📝");
        todolistButton.setCallbackData(BotCommands.TODO_LIST.getCommand());
        InlineKeyboardButton addItemButton = new InlineKeyboardButton();
        addItemButton.setText("Agregar tarea ✏️");
        addItemButton.setCallbackData(BotCommands.ADD_ITEM.getCommand());
        row.add(todolistButton);
        row.add(addItemButton);
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        sendInlineKeyboard(chatId, "Selecciona una de las siguientes opciones:", keyboardMarkup);
    }

    private void sendTaskTypeOptions(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton undoneTasksButton = new InlineKeyboardButton();
        undoneTasksButton.setText("Tareas pendientes 📝");
        undoneTasksButton.setCallbackData("TaskType-Undone");
        row1.add(undoneTasksButton);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton doneTasksButton = new InlineKeyboardButton();
        doneTasksButton.setText("Tareas completadas ✅");
        doneTasksButton.setCallbackData("TaskType-Done");
        row2.add(doneTasksButton);

        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardMarkup.setKeyboard(keyboardRows);

        sendInlineKeyboard(chatId, "Selecciona el tipo de tareas que quieres ver:", keyboardMarkup);
    }

    private void replyToListToDo(long chatId, MemberDto memberDto, boolean isDone) {
        List<TaskDto> tasks = taskService.getAllByMember(memberDto);
        if (tasks.isEmpty()) {
            send(chatId, "No tienes tareas");
        } else {
            if (isDone) {
                sendTasksList(chatId, "Tareas completadas ✅", tasks, true);
            } else {
                sendTasksList(chatId, "Tareas pendientes 📝", tasks, false);
            }
        }
    }

    private void sendTasksList(long chatId, String header, List<TaskDto> tasks, boolean isDone) {
        List<TaskDto> filteredTasks = tasks.stream()
                .filter(task -> task.getIsDone() == isDone)
                .collect(Collectors.toList());
        InlineKeyboardMarkup keyboardMarkup = createTasksKeyboard(filteredTasks, isDone);
        sendInlineKeyboard(chatId, header, keyboardMarkup);
    }

    private InlineKeyboardMarkup createTasksKeyboard(List<TaskDto> tasks, boolean isDone) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (TaskDto task : tasks) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton taskButton = new InlineKeyboardButton();
            taskButton.setText(task.getName());
            taskButton.setCallbackData((isDone ? "TaskDone-" : "TaskActive-") + task.getTaskId());
            row.add(taskButton);
            if (!isDone) {
                InlineKeyboardButton doneButton = new InlineKeyboardButton();
                doneButton.setText("Completar ✅");
                doneButton.setCallbackData("EditDone-" + task.getTaskId());
                row.add(doneButton);
            }
            rows.add(row);
        }
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private void replyToAddTask(long chatId, MemberDto memberDto) {
        send(chatId, BotMessages.TYPE_NEW_TODO_ITEM.getMessage());
        taskSessionService.createEmptyTask(chatId, memberDto, false);
    }

    private void cancelAction(long chatId) {
        taskSessionService.deleteTaskSession(chatId);
        send(chatId, "Accion cancelada");
    }

    private void replyToEmployeesList(long chatId, long telegramId) {
        List<MemberDto> employees = memberService.getEmployeesByTelegramId(telegramId);
        if (employees.isEmpty()) {
            send(chatId, "No tienes empleados.");
        } else {
            sendEmployeeList(chatId, employees);
        }
    }

    private void sendEmployeeList(long chatId, List<MemberDto> employees) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (MemberDto employee : employees) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(employee.getName());
            button.setCallbackData("Employee-" + employee.getTelegramId());
            row.add(button);
            rows.add(row);
        }

        keyboardMarkup.setKeyboard(rows);
        sendInlineKeyboard(chatId, "Lista de empleados:", keyboardMarkup);
    }

    private MemberDto getMember(long userId) {
        return memberService.getMemberByTelegramId(userId);
    }

    private void send(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void sendCommandsToBot(SetMyCommands commands) {
        try {
            execute(commands);
        } catch (TelegramApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void sendInlineKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboard);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void clearInlineKeyboard(long chatId, int messageId) {
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(null);
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }
}