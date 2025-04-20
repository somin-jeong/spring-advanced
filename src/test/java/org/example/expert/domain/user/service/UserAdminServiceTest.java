package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {
    @InjectMocks
    private UserAdminService userAdminService;

    @Mock
    private UserRepository userRepository;

    @Test
    void 사용자_권한_변경에_성공한다() {
        // given
        long userId = 1L;
        User user = new User("test@email.com", "password", UserRole.USER);
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userAdminService.changeUserRole(userId, request);

        // then
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }

    @Test
    void 사용자_권한_변경_시_존재하지_않는_사용자면_예외가_발생한다() {
        // given
        long userId = 1;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            userAdminService.changeUserRole(userId, new UserRoleChangeRequest("ADMIN"));
        });
        assertEquals("User not found", exception.getMessage());
    }
}