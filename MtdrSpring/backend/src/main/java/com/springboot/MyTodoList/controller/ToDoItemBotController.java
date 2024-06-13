package com.springboot.MyTodoList.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.facade.TaskManagementFacade;
import com.springboot.MyTodoList.util.BotCommandFactory;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    private final TaskManagementFacade taskFacade;
    private final String botName;

    public ToDoItemBotController(String botToken, String botName, TaskManagementFacade taskFacade) {
        super(botToken);
        logger.info("Bot Token: " + botToken);
        logger.info("Bot name: " + botName);
        this.botName = botName;
        this.taskFacade = taskFacade;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextFromTelegram = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();
            if (taskFacade.getMember(userId) == null) {
                BotHelper.send(chatId, "No eres miembro", this);
                SetMyCommands commands = BotCommandFactory.getCommandsForNoMember(chatId);
                BotHelper.sendCommandsToBot(commands, this);
            } else {
                handleReplies(chatId, userId, messageTextFromTelegram);
                SetMyCommands commands = BotCommandFactory.getCommandsForEmployee(chatId);
                BotHelper.sendCommandsToBot(commands, this);
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            handleCallbacks(chatId, callbackData, update);
        }
    }

    private void handleReplies(long chatId, long userId, String message) {
        if (taskFacade.taskSessionExist(chatId)) {            
            handleTaskSession(chatId, message);
        } else {
            MemberDto memberDto = taskFacade.getMember(userId);
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
                replyToAddTask(chatId, taskFacade.getMember(chatId));
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
                data = data.substring(7);
                handleTaskDelete(chatId, data);
            }
            BotHelper.clearInlineKeyboard(chatId, messageId, this);
        } else {
            logger.error("Message is inaccessible or message ID is null");
        }
    }

    private void handleTaskTypeCallback(long chatId, String data) {
        if (data.equals("TaskType-Undone")) {
            replyToListToDo(chatId, taskFacade.getMember(chatId), false);
        } else if (data.equals("TaskType-Done")) {
            replyToListToDo(chatId, taskFacade.getMember(chatId), true);
        }
    }

    public void handleTaskCallback(long chatId, String data) {
        if (data.startsWith("Active-")) {
            int taskId = Integer.parseInt(data.substring(7));
            TaskDto taskDto = taskFacade.getTaskById(taskId);
            if (taskDto != null) {
                sendEditMenu(chatId, taskDto);
            }
        } else if (data.startsWith("Done-")) {
            int taskId = Integer.parseInt(data.substring(5));
            TaskDto taskDto = taskFacade.getTaskById(taskId);
            if (taskDto != null) {
                String taskMessage = String.format(
                        "*Nombre:* %s\n"
                        + "*Descripción:* %s\n"
                        + "*Fecha de inicio:* %s\n"
                        + "*Fecha de fin:* %s",
                        taskDto.getName(),
                        taskDto.getDescription(),
                        taskDto.getStartDate() != null ? taskDto.getStartDate().toString() : "No especificada",
                        taskDto.getEndDate() != null ? taskDto.getEndDate().toString() : "No especificada"
                );
                sendMarkdown(chatId, taskMessage);
                taskFacade.updateTask(taskDto);
            }
        }
    }

    private void handleTaskSessionCallback(long chatId, String data) {
        if (data.startsWith("TaskYes-")) {
            long id = Long.parseLong(data.substring(8));
            boolean isConfirmed = taskFacade.confirmNewTask(id);
            if (isConfirmed) {
                BotHelper.send(chatId, "Tarea agregada ✅", this);
            } else {
                BotHelper.send(chatId, "La tarea no se pudo agregar ❌", this);
            }
        } else if (data.startsWith("TaskNo-")) {
            long id = Long.parseLong(data.substring(7));
            boolean isCanceled = taskFacade.cancelNewTask(id);
            if (isCanceled) {
                BotHelper.send(chatId, "Tarea eliminada ❌", this);
            } else {
                BotHelper.send(chatId, "La tarea no se pudo eliminar ❌", this);
            }
        }
    }

    private void handleEmployeeCallback(long chatId, String data) {
        long telegramId = Long.parseLong(data.split("-")[1]);
        List<TaskDto> tasks = taskFacade.getTasksByTelegramId(telegramId);
        if (tasks.isEmpty()) {
            BotHelper.send(chatId, "El empleado no tiene tareas.", this);
        } else {
            sendTasksList(chatId, "Tareas del empleado:", tasks, false);
        }
    }

    private void handleTaskEdit(long chatId, String data) {
        int taskId = Integer.parseInt(data.substring(5));
        if (data.startsWith("Name-")) {
            taskFacade.prepareEditNameOfTask(chatId, taskId);
            BotHelper.send(chatId, "Ingrese el nuevo nombre:", this);
        } else if (data.startsWith("Desc-")) {
            taskFacade.prepareEditDescOfTask(chatId, taskId);
            BotHelper.send(chatId, "Ingrese la nueva descripción:", this);
        } else if (data.startsWith("Done-")) {
            taskFacade.setTaskAsDone(taskId);
            BotHelper.send(chatId, "Tarea hecha", this);
        }
    }

    private void handleTaskDelete(long chatId, String data) {
        int taskId = Integer.parseInt(data);
        boolean isDeleted = taskFacade.deleteTask(taskId);
        if (isDeleted) {
            BotHelper.send(chatId, "Tarea eliminada 🗑️", this);
        } else {
            BotHelper.send(chatId, "Tarea no eliminada 😕", this);
        }
    }

    private void sendEditMenu(long chatId, TaskDto taskDto) {
        InlineKeyboardMarkup editKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        InlineKeyboardButton nameButton = BotHelper.createButton("Editar nombre ✏️", "EditName-" + taskDto.getTaskId());
        InlineKeyboardButton descButton = BotHelper.createButton("Editar descripción 💬", "EditDesc-" + taskDto.getTaskId());
        InlineKeyboardButton doneButton = BotHelper.createButton("Marcar como hecho ✅", "EditDone-" + taskDto.getTaskId());
        InlineKeyboardButton deleteButton = BotHelper.createButton("Eliminar tarea 🗑️", "Delete-" + taskDto.getTaskId());
        InlineKeyboardButton cancelButton = BotHelper.createButton("Cancelar ❌", BotCommands.CANCEL.getCommand());

        keyboardRows.add(BotHelper.createRow(nameButton, descButton));
        keyboardRows.add(BotHelper.createRow(doneButton));
        keyboardRows.add(BotHelper.createRow(deleteButton));
        keyboardRows.add(BotHelper.createRow(cancelButton));

        editKeyboardMarkup.setKeyboard(keyboardRows);

        String taskMessage = String.format(
                "*Nombre:* %s\n"
                + "*Descripción:* %s\n"
                + "*Fecha de inicio:* %s\n"
                + "*Fecha de fin:* %s",
                taskDto.getName(),
                taskDto.getDescription(),
                taskDto.getStartDate() != null ? taskDto.getStartDate().toString() : "No especificada",
                taskDto.getEndDate() != null ? taskDto.getEndDate().toString() : "No especificada"
        );

        sendMarkdown(chatId, taskMessage);
        BotHelper.sendInlineKeyboard(chatId, "Acciones ⚙️", editKeyboardMarkup, this);
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
                replyToEmployeesList(chatId, memberDto);
            } else {
                BotHelper.send(chatId, "No tienes permisos para ver esta lista.", this);
            }
        } else if (message.equals(BotCommands.CANCEL.getCommand())) {
            BotHelper.send(chatId, "No hay nada para cancelar", this);
        } else {
            replyToUnkownText(chatId);
        }
    }

    private void handleTaskSession(long chatId, String text) {
        if (text.equals(BotCommands.CANCEL.getCommand())) {
            cancelAction(chatId);
            return;
        }

        if (!taskFacade.isInputValid(text)) {
            BotHelper.send(chatId, "Texto invalido. Vuelva a intentarlo", this);
            return;
        }

        TaskDto session = taskFacade.getActiveTaskSession(chatId);
        if (session.getIsEdit()) {
            handleTaskSessionEdit(chatId, text);
        } else {
            handleTaskSessionAdd(chatId, text);
        }
    }

    private void handleTaskSessionAdd(long chatId, String text) {
        TaskDto newTask = taskFacade.getActiveTaskSession(chatId);
        if (newTask.getName() == null) {
            handleNameInput(chatId, newTask, text, "Ingresa la descripción");
        } else if (newTask.getDescription() == null) {
            handleDescriptionInput(chatId, newTask, text, "Ingresa la fecha de inicio (YYYY-MM-DD):");
        } else if (newTask.getStartDate() == null) {
            handleStartDateInput(chatId, newTask, text);
        } else if (newTask.getEndDate() == null) {
            handleEndDateInput(chatId, newTask, text);
        }
    }

    private void handleNameInput(long chatId, TaskDto task, String text, String message) {
        if (taskFacade.isTaskNameValid(text)) {
            task.setName(text);
            taskFacade.updateTaskSession(chatId, task);
            BotHelper.send(chatId, message, this);
        } else {
            BotHelper.send(chatId, "Texto demasiado largo", this);
        }
    }

    private void handleDescriptionInput(long chatId, TaskDto task, String text, String message) {
        if (taskFacade.isTaskDescriptionValid(text)) {
            task.setDescription(text);
            taskFacade.updateTaskSession(chatId, task);
            BotHelper.send(chatId, message, this);
        } else {
            BotHelper.send(chatId, "Texto demasiado largo", this);
        }
    }

    private void handleStartDateInput(long chatId, TaskDto task, String text) {
        if (taskFacade.isDateValid(text)) {
            task.setStartDate(LocalDate.parse(text));
            taskFacade.updateTaskSession(chatId, task);
            BotHelper.send(chatId, "Ingresa la fecha de fin (YYYY-MM-DD):", this);
        } else {
            BotHelper.send(chatId, "Fecha inválida. Ingresa la fecha de inicio (YYYY-MM-DD):", this);
        }
    }

    private void handleEndDateInput(long chatId, TaskDto task, String text) {
        if (taskFacade.isDateValid(text)) {
            task.setEndDate(LocalDate.parse(text));
            taskFacade.updateTaskSession(chatId, task);
            sendTaskConfirmation(chatId, task);
        } else {
            BotHelper.send(chatId, "Fecha inválida. Ingresa la fecha de fin (YYYY-MM-DD):", this);
        }
    }

    private void sendTaskConfirmation(long chatId, TaskDto newTaskSession) {
        BotHelper.send(chatId, "Nueva tarea:", this);
        InlineKeyboardMarkup infoKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        
        InlineKeyboardButton yesButton = BotHelper.createButton("Confirmar", "SessionTaskYes-" + Long.toString(chatId));
        InlineKeyboardButton noButton = BotHelper.createButton("Cancelar", "SessionTaskNo-" + Long.toString(chatId));
        keyboardRows.add(BotHelper.createRow(yesButton, noButton));
        infoKeyboardMarkup.setKeyboard(keyboardRows);

        String taskMessage = String.format(
                "*Nombre:* %s\n"
                + "*Descripción:* %s\n"
                + "*Fecha de inicio:* %s\n"
                + "*Fecha de fin:* %s",
                newTaskSession.getName(),
                newTaskSession.getDescription(),
                newTaskSession.getStartDate() != null ? newTaskSession.getStartDate().toString() : "No especificada",
                newTaskSession.getEndDate() != null ? newTaskSession.getEndDate().toString() : "No especificada"
        );

        sendMarkdown(chatId, taskMessage);
        BotHelper.sendInlineKeyboard(chatId, "Acciones ⚙️", infoKeyboardMarkup, this);
    }

    private void handleTaskSessionEdit(long chatId, String text) {
        TaskDto editedTask = taskFacade.getActiveTaskSession(chatId);
        if (editedTask.getName() == null) {
            handleNameInput(chatId, editedTask, text, "Nombre actualizado con éxito! ✅");
        } else if (editedTask.getDescription() == null) {
            handleDescriptionInput(chatId, editedTask, text, "Descripción actualizada con éxito ✅");
        }
        taskFacade.confirmEditTaskSession(chatId, editedTask);
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    private void replyToUnkownText(long chatId) {
        BotHelper.send(chatId, BotMessages.UNKNOWN_TEXT.getMessage(), this);
    }

    private void replyToStart(long chatId) {
        MemberDto memberDto = taskFacade.getMember(chatId);
        if (memberDto.getIsManager()) {
            BotHelper.send(chatId, BotMessages.HELLO_MANAGER.getMessage(), this);
        } else {
            BotHelper.send(chatId, BotMessages.HELLO_MEMBER.getMessage(), this);
        }
        
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        InlineKeyboardButton todolistButton = BotHelper.createButton("Ver tareas 📝", BotCommands.TODO_LIST.getCommand());
        InlineKeyboardButton addItemButton = BotHelper.createButton("Agregar tarea ✏", BotCommands.ADD_ITEM.getCommand());
        keyboardRows.add(BotHelper.createRow(todolistButton, addItemButton));
        keyboardMarkup.setKeyboard(keyboardRows);
        BotHelper.sendInlineKeyboard(chatId, "Selecciona una de las siguientes opciones:", keyboardMarkup, this);
    }

    private void sendTaskTypeOptions(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();

        InlineKeyboardButton undoneTasksButton = BotHelper.createButton("Tareas pendientes 📝", "TaskType-Undone");
        InlineKeyboardButton doneTasksButton = BotHelper.createButton("Tareas completadas ✅", "TaskType-Done");
        keyboardRows.add(BotHelper.createRow(undoneTasksButton));
        keyboardRows.add(BotHelper.createRow(doneTasksButton));
        keyboardMarkup.setKeyboard(keyboardRows);

        BotHelper.sendInlineKeyboard(chatId, "Selecciona el tipo de tareas que quieres ver:", keyboardMarkup, this);
    }

    private void replyToListToDo(long chatId, MemberDto memberDto, boolean isDone) {
        List<TaskDto> tasks = taskFacade.getTasksByMember(memberDto);
        if (tasks.isEmpty()) {
            BotHelper.send(chatId, "No tienes tareas", this);
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
        BotHelper.sendInlineKeyboard(chatId, header, keyboardMarkup, this);
    }

    private InlineKeyboardMarkup createTasksKeyboard(List<TaskDto> tasks, boolean isDone) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        for (TaskDto task : tasks) {
            InlineKeyboardButton taskButton = BotHelper.createButton(task.getName(), (isDone ? "TaskDone-" : "TaskActive-") + task.getTaskId());           
            if (!isDone) {
                InlineKeyboardButton doneButton = BotHelper.createButton("Completar ✅", "EditDone-" + task.getTaskId());
                keyboardRows.add(BotHelper.createRow(taskButton, doneButton));
            } else {
                InlineKeyboardButton deleteButton = BotHelper.createButton("Eliminar 🗑️", "Delete-" + task.getTaskId());
                keyboardRows.add(BotHelper.createRow(taskButton, deleteButton));
            }
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private void replyToAddTask(long chatId, MemberDto memberDto) {
        taskFacade.prepareForNewTask(chatId, memberDto);
        BotHelper.send(chatId, BotMessages.TYPE_NEW_TODO_ITEM.getMessage(), this);
    }

    private void cancelAction(long chatId) {
        taskFacade.cancelNewTask(chatId);
        BotHelper.send(chatId, "Accion cancelada", this);
    }

    private void replyToEmployeesList(long chatId, MemberDto member) {
        List<MemberDto> employees = taskFacade.getAllMembersOfManager(member.getId());
        if (employees.isEmpty()) {
            BotHelper.send(chatId, "No tienes empleados.", this);
        } else {
            sendEmployeeList(chatId, employees);
        }
    }

    private void sendEmployeeList(long chatId, List<MemberDto> employees) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        for (MemberDto employee : employees) {
            InlineKeyboardButton button = BotHelper.createButton(employee.getName(), "Employee-" + employee.getTelegramId());
            keyboardRows.add(BotHelper.createRow(button));
        }
        keyboardMarkup.setKeyboard(keyboardRows);
        BotHelper.sendInlineKeyboard(chatId, "Lista de empleados:", keyboardMarkup, this);
    }
}
