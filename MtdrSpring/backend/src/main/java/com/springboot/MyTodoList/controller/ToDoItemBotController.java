package com.springboot.MyTodoList.controller;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
import java.util.stream.Collectors;

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
            handleCallbacks(chatId, callbackData);
        }
    }

    private void handleReplies(long chatId, long userId, String message) {
        TaskDto taskSessionDto = taskSessionService.getTaskSession(chatId);
        if (taskSessionDto != null) {
            logger.info("Task session: " + taskSessionDto.getTaskSessionId());
            handleTaskSession(chatId, taskSessionDto, message);
        } else {
            MemberDto memberDto = getMember(userId);
            handleAuthenticatedCommands(chatId, memberDto, message);
        }
    }

    private void handleCallbacks(long chatId, String data) {
        if (data.equals(BotCommands.TODO_LIST.getCommand())) {
            replyToListToDo(chatId, getMember(chatId));
        } else if (data.equals(BotCommands.ADD_ITEM.getCommand())) {
            replyToAddTask(chatId, getMember(chatId));
        } else if (data.equals(BotCommands.CANCEL.getCommand())) {
            cancelAction(chatId);
        }


        if (data.startsWith("Task")) {
            data = data.substring(4);
            handleTaskCallback(chatId, data);
        } else if (data.startsWith("Session")) {
            data = data.substring(7);
            handleTaskSessionCallback(chatId, data);
        } else if (data.startsWith("Edit")) {
            data = data.substring(4);
            handleTaskEdit(chatId, data);
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
                send(chatId, taskDto.toString());
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

    private void handleTaskEdit(long chatId, String data) {
        int taskId = Integer.parseInt(data.substring(5));
        TaskDto taskDto = taskService.getTaskById(taskId);
        if (taskDto == null) {
            return;
        }
        MemberDto memberDto = memberService.getMemberById(taskDto.getMemberId());
        if (data.startsWith("Name-")) {
            send(chatId, "Ingrese el nuevo nombre:");
            TaskDto newTaskSession = taskSessionService.createEmptyTask(chatId, memberDto, true);
            newTaskSession.setName(null);
            newTaskSession.setDescription(taskDto.getDescription());
            newTaskSession.setStartDate(taskDto.getStartDate());
            newTaskSession.setEndDate(taskDto.getEndDate());
            newTaskSession.setTaskId(taskId);
            taskSessionService.updateTask(chatId, newTaskSession);
        } else if (data.startsWith("Desc-")) {
            send(chatId, "Ingrese la nueva descripción:");
            TaskDto newTaskSession = taskSessionService.createEmptyTask(chatId, memberDto, true);
            newTaskSession.setName(taskDto.getName());
            newTaskSession.setDescription(null);
            newTaskSession.setStartDate(taskDto.getStartDate());
            newTaskSession.setEndDate(taskDto.getEndDate());
            newTaskSession.setTaskId(taskId);
            taskSessionService.updateTask(chatId, newTaskSession);
        } else if (data.startsWith("Done-")) {
            taskDto.setIsDone(true);
            taskService.updateTask(taskDto);
            send(chatId, "Tarea hecha");
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
        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText("Cancelar ❌");
        cancelButton.setCallbackData(BotCommands.CANCEL.getCommand());
        row2.add(cancelButton);
    
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        editKeyboardMarkup.setKeyboard(keyboardRows);
    
        String taskMessage = String.format(
            "*Nombre:* %s\n*Descripción:* %s",
            taskDto.getName(),
            taskDto.getDescription()
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
            replyToListToDo(chatId, memberDto);
        } else if (message.equals(BotCommands.ADD_ITEM.getCommand())) {
            replyToAddTask(chatId, memberDto);
        } else if (message.equals(BotCommands.CANCEL.getCommand())) {
            cancelAction(chatId);
        } else {
            replyToUnkownText(chatId);
        }
    }

    private void handleTaskSession(long chatId, TaskDto newTaskSession, String text) {
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
            taskSessionService.updateTask(chatId, newTaskSession);
        } else if (newTaskSession.getDescription() == null) {
            newTaskSession.setDescription(text);
            taskSessionService.updateTask(chatId, newTaskSession);
            send(chatId, "Nueva tarea:");
            InlineKeyboardMarkup infoKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton yesButton = new InlineKeyboardButton();
            yesButton.setText("Si");
            yesButton.setCallbackData("SessionTaskYes-" + Long.toString(chatId));
            InlineKeyboardButton noButton = new InlineKeyboardButton();
            noButton.setText("No");
            noButton.setCallbackData("SessionTaskNo-" + Long.toString(chatId));
            row.add(yesButton);
            row.add(noButton);
            keyboardRows.add(row);
            infoKeyboardMarkup.setKeyboard(keyboardRows);
            sendInlineKeyboard(chatId, newTaskSession.getName() + " " + newTaskSession.getDescription(), infoKeyboardMarkup);
        }
    }

    private void handleTaskSessionEdit(long chatId, TaskDto newTaskSession, String text) {
        logger.info("Editing task session");
        String updateText = "";
        if (newTaskSession.getName() == null) {
            newTaskSession.setName(text);
            updateText = "Nombre actualizado con éxito";
        } else if (newTaskSession.getDescription() == null) {
            newTaskSession.setDescription(text);
            updateText = "Descripción actualizada con éxito";
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
        sendWelcomeMessage(chatId);
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

    private void replyToListToDo(long chatId, MemberDto memberDto) {
        List<TaskDto> tasks = taskService.getAllByMember(memberDto);
        if (tasks.isEmpty()) {
            send(chatId, "No tienes tareas");
        } else {
            sendTasksList(chatId, "Lista de las tareas:", tasks, false);
            sendTasksList(chatId, "Lista de tareas hechas", tasks, true);
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
}