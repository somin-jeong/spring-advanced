package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@DataJpaTest
@ExtendWith(SpringExtension.class)
class TodoRepositoryTest {
    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void todo_목록_조회_시_modifiedAt_기준_내림차순으로_user와_함께_조회한다() {
        // given
        User user = new User("test@email.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        User savedUser = userRepository.save(user);

        Todo todo1 = new Todo("title1", "content1", "Sunny", savedUser);
        ReflectionTestUtils.setField(todo1, "createdAt", LocalDateTime.now().minusDays(2));
        ReflectionTestUtils.setField(todo1, "modifiedAt", LocalDateTime.now().minusDays(2));

        Todo todo2 = new Todo("title2", "content2", "Chilly", savedUser);
        ReflectionTestUtils.setField(todo2, "createdAt", LocalDateTime.now().minusDays(1));
        ReflectionTestUtils.setField(todo2, "modifiedAt", LocalDateTime.now().minusDays(1));

        todoRepository.save(todo1);
        todoRepository.save(todo2);

        // when
        Page<Todo> result = todoRepository.findAllByOrderByModifiedAtDesc(PageRequest.of(0, 10));

        // then
        assertEquals(2, result.getTotalElements());
        assertEquals("title2", result.getContent().get(0).getTitle());
        assertEquals("title1", result.getContent().get(1).getTitle());
        assertEquals(user.getEmail(), result.getContent().get(0).getUser().getEmail());
    }

    @Test
    void todo_상세조회_시_user와_함께_조회한다() {
        // given
        User user = new User("test@email.com", "password", UserRole.USER);
        User savedUser = userRepository.save(user);

        Todo todo = new Todo("title", "content", "Sunny", savedUser);
        todoRepository.save(todo);

        // when
        Optional<Todo> result = todoRepository.findByIdWithUser(todo.getId());

        // then
        assertTrue(result.isPresent());
        assertEquals("title", result.get().getTitle());
        assertEquals("content", result.get().getContents());
        assertEquals(user.getEmail(), result.get().getUser().getEmail());
    }

}