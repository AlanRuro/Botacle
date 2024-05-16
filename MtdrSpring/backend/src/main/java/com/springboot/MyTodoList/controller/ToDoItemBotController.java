package com.springboot.MyTodoList.controller;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.model.Member;
import com.springboot.MyTodoList.model.Task;
import com.springboot.MyTodoList.service.AuthService;
import com.springboot.MyTodoList.service.ChatSession;
import com.springboot.MyTodoList.service.MemberService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.util.BotCommandFactory;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotMessages;
import com.springboot.MyTodoList.util.BotState;
import java.util.HashMap;
import java.util.Map;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    private final TaskService taskService;
    private final ChatSession chatSession;
    private final AuthService authService;
    private final MemberService memberService;
    private final String botName;

    public ToDoItemBotController(String botToken, String botName, TaskService taskService, MemberService memberService, AuthService authService) {
        super(botToken);
        logger.info("Bot Token: " + botToken);
        logger.info("Bot name: " + botName);
        this.authService = authService;
        this.taskService = taskService;
        this.memberService = memberService;
        this.botName = botName;
        this.chatSession = new ChatSession();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String messageTextFromTelegram = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            long userId = update.getMessage().getFrom().getId();

            logger.info("userid: " + userId);

            handleReplies(chatId, userId, messageTextFromTelegram);

            //if(chatSession.getChatState(chatId) == null){
            //    send(chatId, "El estado del chat " + chatId + " es nulo");
            //}else{
            //    send(chatId, "Chat state: " + chatSession.getChatState(chatId));
            //}

        } else if (update.hasCallbackQuery()) {

            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callbackData = update.getCallbackQuery().getData();
            handleCallbacks(chatId, callbackData);
        }
    }

    private void handleReplies(long chatId, long userId, String message) {
        BotState currentState = chatSession.getChatState(chatId);
        if (currentState == null) {
            sendBasicCommands(chatId);
            if (message.equals(BotCommands.START.getCommand())) {
                send(chatId, BotMessages.HELLO_MYTODO_BOT.getMessage());
            } else if (message.equals(BotCommands.LOGIN.getCommand())) {
                send(chatId, "Ingresa tu username:");
                chatSession.createEmptyCredentials(chatId, userId);
                chatSession.setChatState(chatId, BotState.AWAITING_USERNAME);
            } else if (message.equals(BotCommands.CANCEL.getCommand())) {
                chatSession.removeCredentials(chatId);
            } else {
                replyToUnkownText(chatId);
            }
        } else {
            handleStates(chatId, userId, message, currentState);
        }
    }

    private void handleStates(long chatId, long userId, String message, BotState currentState) {
        switch (currentState) {
            case AUTHENTICATED:
                Member member = getMember(userId);
                handleAuthenticatedCommands(chatId, member, message);
                break;
            case AWAITING_USERNAME:
                chatSession.setUsername(chatId, message);
                chatSession.setChatState(chatId, BotState.AWAITING_PASSWORD);
                send(chatId, "Ingresa tu contraseña:");
                break;
            case AWAITING_PASSWORD:
                chatSession.setPassword(chatId, message);
                authenticateUser(chatId, userId);
                break;
            case AWAITING_TASK_NAME:
                chatSession.setTaskName(chatId, message);
                send(chatId, "Ingresa la descripcion");
                break;
            case AWAITING_TASK_DESCRIPTION:
                chatSession.setTaskDescription(chatId, message);
                TaskDto newTask = chatSession.getTaskInCreation(chatId);
                Task registeredTask = taskService.addTask(newTask);
                send(chatId, "Tarea: " + registeredTask.getName() + " registrada");

                chatSession.removeTask(chatId);
                chatSession.setChatState(chatId, BotState.AUTHENTICATED);
                break;
            default:
                send(chatId, "Estado desconocido");

        }
    }

    private void handleCallbacks(long chatId, String data) {
        if (data.startsWith("task-")) {
            int taskId = Integer.parseInt(data.substring(5));
            Task task = taskService.getTaskById(taskId);
            if (task != null) {
                send(chatId, task.toString());
            }
        }
    }

    private void authenticateUser(long chatId, long telegramId) {
        String username = chatSession.getUsername(chatId);
        String password = chatSession.getPassword(chatId);
        MemberDto memberDto = new MemberDto();
        memberDto.setTelegramId(telegramId);
        memberDto.setUsername(username);
        memberDto.setPassword(password);
        if (authService.isMemberAuthenticated(memberDto)) {
            onLoggedIn(chatId, telegramId);
        } else {
            send(chatId, "Autenticación fallida. Por favor, intenta de nuevo.");
            chatSession.removeState(chatId);
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
        } else if (message.equals(BotCommands.LOGOUT.getCommand())) {
            replyToLogOut(chatId);
        } else {
            replyToUnkownText(chatId);
        }
    }

    private void replyToLogOut(long chatId) {
        sendBasicCommands(chatId);
        chatSession.removeState(chatId);
        send(chatId, "Has cerrado sesión exitosamente.");
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

    private void onLoggedIn(long chatId, long telegramId) {
        chatSession.setChatState(chatId, BotState.AUTHENTICATED);
        SetMyCommands commands;
        Member member = getMember(telegramId);
        if (member.getIsManager()) {
            commands = BotCommandFactory.getCommandsForManager(chatId);
        } else {
            commands = BotCommandFactory.getCommandsForEmployee(chatId);
        }
        send(chatId, "Autenticación exitosa. Bienvenido!");
        sendCommandsToBot(commands);
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
        chatSession.createEmptyTask(chatId, member);
        chatSession.setChatState(chatId, BotState.AWAITING_TASK_NAME);
    }

    private void cancelAction(long chatId, Member member) {
        chatSession.removeAllFromChat(chatId);
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

    //--------
//    
//    // GET /todolist
//    public List<Task> getAllToDoItems() {
////		return taskService.findAll();
//        return null;
//    }
//
//    // GET BY ID /todolist/{id}
//    public ResponseEntity<Task> getToDoItemById(@PathVariable int id) {
//        try {
//            ResponseEntity<Task> responseEntity = taskService.getItemById(id);
//            return new ResponseEntity<>(responseEntity.getBody(), HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
//
//    // PUT /todolist
//    public ResponseEntity addToDoItem(@RequestBody TaskDto newTask) throws Exception {
//        Task td = taskService.addTask(newTask);
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.set("location", "" + td.getTaskId());
//        responseHeaders.set("Access-Control-Expose-Headers", "location");
//        // URI location = URI.create(""+td.getID())
//
//        return ResponseEntity.ok().headers(responseHeaders).build();
//    }
//
//    // UPDATE /todolist/{id}
//    public ResponseEntity updateToDoItem(@RequestBody TaskDto task, @PathVariable int id) {
//        try {
//            Task toDoItem1 = taskService.updateTask(id, task);
//            System.out.println(toDoItem1.toString());
//            return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
//        }
//    }
//
//    // DELETE todolist/{id}
//    public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id) {
//        Boolean flag = false;
//        try {
//            flag = taskService.deleteTask(id);
//            return new ResponseEntity<>(flag, HttpStatus.OK);
//        } catch (Exception e) {
//            logger.error(e.getLocalizedMessage(), e);
//            return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
//        }
//    }
}
