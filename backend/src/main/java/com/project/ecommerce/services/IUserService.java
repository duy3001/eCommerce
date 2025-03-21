package com.project.ecommerce.services;

import com.project.ecommerce.dtos.UpdateUserDTO;
import com.project.ecommerce.dtos.UserDTO;
import com.project.ecommerce.models.User;
import com.project.ecommerce.responses.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUserService {
    User createUser(UserDTO userDTO) throws Exception;
    String login(String phoneNumber, String password, Long roleId) throws Exception;
    User getUserDetailsFromToken(String token) throws Exception;
    User updateUser(Long userId, UpdateUserDTO updatedUserDTO) throws Exception;
    Page<UserResponse> findAll(String keyword, Pageable pageable) throws Exception;
}
