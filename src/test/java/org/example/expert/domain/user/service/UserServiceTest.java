package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.example.expert.global.auth.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void user_조회_시_없는_userId이면_예외가_발생한다() {
        // given
        long userId = 1;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.getUser(userId));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void user_조회에_성공한다() {
        // given
        long userId = 1;
        User user = new User("user1@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        UserResponse userResponse = userService.getUser(userId);

        // then
        assertEquals(userResponse.getId(), userId);
        assertEquals(userResponse.getEmail(), "user1@example.com");
    }

    @Test
    public void 비밀번호_변경_시_없는_userId이면_예외가_발생한다() {
        // given
        long userId = 1;
        UserChangePasswordRequest request = new UserChangePasswordRequest("Old1234!", "New1234!");
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when && then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(userId, request));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void 비밀번호_변경_시_새비밀번호가_기존과_같으면_예외() {
        // given
        long userId = 1;
        String password = "samePassword";
        UserChangePasswordRequest request = new UserChangePasswordRequest(password, password);

        User user = new User("user1@example.com", "password", UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(password, "password")).willReturn(true); // 새 비밀번호가 기존과 같음

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(userId, request);
        });
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());
    }

    @Test
    public void 비밀번호_변경_시_기존_비밀번호가_불일치하면_예외() {
        // given
        long userId = 1;
        String newPwd = "New1234!";
        String oldPwd = "Old1234!";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPwd, newPwd);

        User user = new User("user1@example.com", "password", UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(passwordEncoder.matches(newPwd, "password")).willReturn(false);
        given(passwordEncoder.matches(oldPwd, "password")).willReturn(false);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userService.changePassword(userId, request);
        });
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    public void 비밀번호_변경에_성공한다() {
        // given
        long userId = 1;
        String newPwd = "New1234!";
        String oldPwd = "Old1234!";
        UserChangePasswordRequest request = new UserChangePasswordRequest(oldPwd, newPwd);

        User user = new User("user1@example.com", "password", UserRole.USER);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        given(passwordEncoder.matches(newPwd, "password")).willReturn(false);
        given(passwordEncoder.matches(oldPwd, "password")).willReturn(true);
        given(passwordEncoder.encode(newPwd)).willReturn("encodedPassword");

        // when
        userService.changePassword(userId, request);

        // then
        assertEquals("encodedPassword", user.getPassword());
    }
}