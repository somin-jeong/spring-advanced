package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@ExtendWith(SpringExtension.class)
class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void todo에_대한_댓글들_조회_시_user와_함께_조회한다() {
        // given
        User user = new User("test@email.com", "password", UserRole.USER);
        User savedUser = userRepository.save(user);

        Todo todo = new Todo("title1", "content1", "Sunny", savedUser);
        Todo savedTodo = todoRepository.save(todo);

        Comment comment1 = new Comment("comment1", user, savedTodo);
        Comment comment2 = new Comment("comment2", user, savedTodo);
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        List<Comment> comments = commentRepository.findByTodoIdWithUser(savedTodo.getId());

        // then
        assertEquals(2, comments.size());
        assertEquals("comment1", comments.get(0).getContents());
        assertEquals("comment2", comments.get(1).getContents());
        assertEquals(user.getEmail(), comments.get(0).getUser().getEmail());
    }
}