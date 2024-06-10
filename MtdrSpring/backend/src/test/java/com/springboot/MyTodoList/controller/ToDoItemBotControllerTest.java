package com.springboot.MyTodoList.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.springboot.MyTodoList.dto.MemberDto;
import com.springboot.MyTodoList.dto.TaskDto;
import com.springboot.MyTodoList.service.MemberService;
import com.springboot.MyTodoList.service.TaskService;
import com.springboot.MyTodoList.service.TaskSessionService;


public class ToDoItemBotControllerTest {

    private ToDoItemBotController botController;
    private TaskService taskService;
    private TaskSessionService taskSessionService;
    private MemberService memberService;

    @BeforeEach
    public void setUp() {
        taskService = mock(TaskService.class);
        taskSessionService = mock(TaskSessionService.class);
        memberService = mock(MemberService.class);
        botController = new ToDoItemBotController("6994682300:AAFE0w69uB0UKoLayRv3qM5GheVUV8p1k8A", "testoracle34_bot", taskService, memberService, taskSessionService);
        when(memberService.getMemberByTelegramId(anyLong())).thenReturn(new MemberDto());
    }

    @Test
    public void testHandleTaskCallback() {
        long chatId = 2010716726;
        String data = "Done-1";
        TaskDto mockedTaskDto = new TaskDto();
        mockedTaskDto.setName("Automated Test Task");
        mockedTaskDto.setDescription("This is an automated test task");
        mockedTaskDto.setStartDate(LocalDate.now());
        mockedTaskDto.setEndDate(LocalDate.now().plusDays(1));

        when(taskService.getTaskById(anyInt())).thenReturn(mockedTaskDto);

        botController.handleTaskCallback(chatId, data);

        verify(taskService).updateTask(any(TaskDto.class));
    }

}
