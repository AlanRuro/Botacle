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
            } else {
                handleReplies(chatId, userId, messageTextFromTelegram);
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
            handleTaskSession(chatId, taskSessionDto, message);
        } else {
            MemberDto memberDto = getMember(userId);
            handleAuthenticatedCommands(chatId, memberDto, message);
        }
    }

    private void handleCallbacks(long chatId, String data) {

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
                send(chatId, taskDto.toString());
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
                taskSessionService.confirmTaskSession(id);
                taskService.addTask(taskDto);
                send(chatId, "Tarea agregada");
            }
        } else if (data.startsWith("TaskNo-")) {
            long id = Long.parseLong(data.substring(7));
            taskSessionService.deleteTaskSession(id);
            send(chatId, "Tarea eliminada");
        }
    }

    private void handleTaskEdit(long chatId, String data) {
        int taskId = Integer.parseInt(data.substring(5));
        TaskDto taskDto = taskService.getTaskById(taskId);
        if (taskDto == null) {
            return;
        }
        if (data.startsWith("Name-")) {
            
        } else if (data.startsWith("Desc-")) {
            
        } else if (data.startsWith("Done-")) {
            taskDto.setIsDone(true);
            taskService.updateTask(taskDto);
            send(chatId, "Tarea hecha");
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
            yesButton.setCallbackData("SessionTaskYes-" + Long.toString(chatId));
            InlineKeyboardButton noButton = new InlineKeyboardButton();
            noButton.setText("No");
            noButton.setCallbackData("SessionTaskNo-" + Long.toString(chatId));
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
                doneButton.setText("done");
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
        taskSessionService.createEmptyTask(chatId, memberDto);
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
