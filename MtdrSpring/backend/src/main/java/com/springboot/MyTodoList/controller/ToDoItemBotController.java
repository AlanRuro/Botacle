package com.springboot.MyTodoList.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.AuthService;
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
    private final AuthService authService;
    private final String botName;

    private Map<Long, MemberDto> loginSessions = new HashMap<>();

    public ToDoItemBotController(String botToken, String botName, TaskService taskService, MemberService memberService, TaskSessionService taskSessionService, AuthService authService) {
        super(botToken);
        logger.info("Bot Token: " + botToken);
        logger.info("Bot name: " + botName);
        this.taskSessionService = taskSessionService;
        this.taskService = taskService;
        this.memberService = memberService;
        this.authService = authService;
        this.botName = botName;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageTextFromTelegram = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            // Manejar comandos de inicio y cierre de sesión
            if (messageTextFromTelegram.equals("/start")) {
                handleStart(chatId, userId);
            } else if (messageTextFromTelegram.equals("/login")) {
                handleLoginCommand(chatId, userId);
            } else if (messageTextFromTelegram.equals("/logout")) {
                handleLogout(chatId, userId);
            } else if (loginSessions.containsKey(chatId)) {
                handleLoginProcess(chatId, userId, messageTextFromTelegram);
            } else {
                handleReplies(chatId, userId, messageTextFromTelegram);
            }
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            handleCallbacks(chatId, callbackData);
        }
    }

    private void handleStart(long chatId, long userId) {
        send(chatId, BotMessages.HELLO_MYTODO_BOT.getMessage());
        if (authService.isUserLoggedIn(userId)) {
            send(chatId, BotMessages.SUMMARY.getMessage());
        } else {
            send(chatId, BotMessages.LOGIN.getMessage());
        }
    }

    private void handleLoginCommand(long chatId, long userId) {
        if (authService.isUserLoggedIn(userId)) {
            send(chatId, "Ya estás loggeado.");
        } else {
            send(chatId, "Por favor, proporciona tu nombre de usuario:");
            loginSessions.put(chatId, new MemberDto());
        }
    }

    private void handleLoginProcess(long chatId, long userId, String messageText) {
        MemberDto memberDto = loginSessions.get(chatId);
        if (memberDto.getUsername() == null) {
            memberDto.setTelegramId(userId);
            memberDto.setUsername(messageText);
            send(chatId, "Ahora, por favor, proporciona tu contraseña:");
        } else if (memberDto.getPassword() == null) {
            memberDto.setPassword(messageText);
            if (authService.login(memberDto)) {
                send(chatId, "Inicio de sesión exitoso.");
                send(chatId, BotMessages.SUMMARY.getMessage());
                loginSessions.remove(chatId);
            } else {
                send(chatId, "Nombre de usuario o contraseña incorrectos. Por favor, inténtalo de nuevo.");
                loginSessions.remove(chatId);
            }
        }
    }

    private void handleLogout(long chatId, long userId) {
        Member member = getMember(userId);
        if (member != null) {
            authService.logout(member);
            send(chatId, "Sesión cerrada correctamente");
        } else {
            send(chatId, "No estás autenticado");
        }
    }

    private void handleReplies(long chatId, long userId, String message) {
        if (!authService.isUserLoggedIn(userId)) {
            send(chatId, "Por favor, inicia sesión primero usando el comando /login");
            return;
        }

        TaskDto taskSessionDto = taskSessionService.getTaskSession(chatId);
        if (taskSessionDto != null) {
            handleTaskSession(chatId, taskSessionDto, message);
        } else {
            Member member = getMember(userId);
            handleAuthenticatedCommands(chatId, member, message);
        }
    }

    private void handleCallbacks(long chatId, String data) {
        if (!authService.isUserLoggedIn(chatId)) {
            send(chatId, "Por favor, inicia sesión primero usando el comando /login");
            return;
        }

        if (data.startsWith("task-")) {
            int taskId = Integer.parseInt(data.substring(5));
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                send(chatId, task.toString());
            }
        } else if (data.startsWith("taskSessionYes-")) {
            long id = Long.parseLong(data.substring(15));
            TaskDto task = taskSessionService.getTaskSession(id);
            if (task != null) {
                taskSessionService.confirmTaskSession(id);
                taskService.addTask(task);
                send(chatId, "Tarea agregada");
            }
        } else if (data.startsWith("taskSessionNo-")) {
            long id = Long.parseLong(data.substring(14));
            taskSessionService.deleteTaskSession(id);
            send(chatId, "Tarea eliminada");
        }
    }

    private void handleAuthenticatedCommands(long chatId, Member member, String message) {
        if (message.equals(BotCommands.START.getCommand())) {
            replyToStart(chatId);
        } else if (message.equals(BotCommands.TODO_LIST.getCommand())) {
            replyToListToDo(chatId, member);
        } else if (message.equals(BotCommands.ADD_ITEM.getCommand())) {
            replyToAddTask(chatId, member);
        } else if (message.equals(BotCommands.CANCEL.getCommand())) {
            cancelAction(chatId, member);
        } else {
            replyToUnkownText(chatId);
        }
    }

    private void handleTaskSession(long chatId, TaskDto newTask, String text) {
        if (newTask.getName() == null) {
            newTask.setName(text);
            send(chatId, "Ingresa la descripcion");
            taskSessionService.updateTask(chatId, newTask);
        } else if (newTask.getDescription() == null) {
            newTask.setDescription(text);
            taskSessionService.updateTask(chatId, newTask);

            send(chatId, "Nueva tarea:");
            InlineKeyboardMarkup infoKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton yesButton = new InlineKeyboardButton();
            yesButton.setText("Si");
            yesButton.setCallbackData("taskSessionYes-" + Long.toString(chatId));
            InlineKeyboardButton noButton = new InlineKeyboardButton();
            noButton.setText("No");
            noButton.setCallbackData("taskSessionNo-" + Long.toString(chatId));
            row.add(yesButton);
            row.add(noButton);
            keyboardRows.add(row);
            infoKeyboardMarkup.setKeyboard(keyboardRows);
            sendInlineKeyboard(chatId, newTask.getName() + " " + newTask.getDescription(), infoKeyboardMarkup);
        }
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

    private void replyToStart(long chatId) {
        send(chatId, BotMessages.SUMMARY.getMessage());
    }

    private void replyToListToDo(long chatId, Member member) {
        List<Task> tasks = taskService.getAllByMember(member);

        String tasksText;
        InlineKeyboardMarkup tasksKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        if (tasks.isEmpty()) {
            tasksText = "No tienes tareas";
            send(chatId, tasksText);
        } else {
            tasksText = "Lista de las tareas:";
            List<InlineKeyboardButton> row = new ArrayList<>();
            for (Task task : tasks) {
                InlineKeyboardButton taskButton = new InlineKeyboardButton();
                taskButton.setText(task.getName());
                taskButton.setCallbackData("task-" + Integer.toString(task.getTaskId()));
                row.add(taskButton);
            }
            keyboardRows.add(row);
            tasksKeyboardMarkup.setKeyboard(keyboardRows);
            sendInlineKeyboard(chatId, tasksText, tasksKeyboardMarkup);
        }
    }

    private void replyToAddTask(long chatId, Member member) {
        send(chatId, BotMessages.TYPE_NEW_TODO_ITEM.getMessage());
        taskSessionService.createEmptyTask(chatId, member);
    }

    private void cancelAction(long chatId, Member member) {
        taskSessionService.deleteTaskSession(chatId);
        send(chatId, "Accion cancelada");
    }

    private Member getMember(long userId) {
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
