package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @Mock
    private ManagerRepository managerRepository;
    @InjectMocks
    private TodoService todoService;

    @Test
    public void todo_저장에_성공한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
        Todo todo = new Todo("title", "contents", "Chilly", user);
        Manager manager = new Manager(user, todo);

        given(weatherClient.getTodayWeather()).willReturn("Chilly");
        given(todoRepository.save(any())).willReturn(todo);
        given(managerRepository.save(any())).willReturn(manager);

        // then
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

        // then
        assertEquals(todoSaveResponse.getTitle(), "title");
        assertEquals(todoSaveResponse.getContents(), "contents");
        assertEquals(todoSaveResponse.getWeather(), "Chilly");
        assertEquals(todoSaveResponse.getUser().getId(), user.getId());
    }

    @Test
    public void todo_목록_조회에_성공한다() {
        // given
        int page = 1;
        int size = 5;
        Pageable pageable = PageRequest.of(page - 1, size);

        User user = new User("test@email.com", "password", UserRole.USER);
        Todo todo = new Todo("title", "contents", "Sunny", user);

        Page<Todo> mockPage = new PageImpl<>(List.of(todo), pageable, 1);
        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(mockPage);

        // when
        Page<TodoResponse> result = todoService.getTodos(page, size);

        // then
        assertEquals(1, result.getTotalElements());
        assertEquals("title", result.getContent().get(0).getTitle());
        assertEquals("Sunny", result.getContent().get(0).getWeather());
        assertEquals("test@email.com", result.getContent().get(0).getUser().getEmail());
    }

    @Test
    void todo_상세조회_시_없는_todoId이면_예외가_발생한다() {
        // given
        long todoId = 1;
        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            todoService.getTodo(todoId);
        });
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    public void todo_상세조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("test@email.com", "password", UserRole.USER);
        Todo todo = new Todo("title", "contents", "Sunny", user);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when
        TodoResponse response = todoService.getTodo(todoId);

        // then
        assertEquals("title", response.getTitle());
        assertEquals("contents", response.getContents());
        assertEquals("Sunny", response.getWeather());
        assertEquals("test@email.com", response.getUser().getEmail());
    }
}