package com.sagafitmi.ecommerce.mapper;

import com.sagafitmi.ecommerce.dto.UserCreateDTO;
import com.sagafitmi.ecommerce.dto.UserDTO;
import com.sagafitmi.ecommerce.model.Role;
import com.sagafitmi.ecommerce.model.User;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        if (user == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        return dto;
    }

    public static User toEntity(UserCreateDTO createDto) {
        if (createDto == null) return null;
        User user = new User();
        user.setName(createDto.getName());
        user.setEmail(createDto.getEmail());
        user.setPassword(createDto.getPassword());
        // set role if provided, otherwise default stays in entity
        if (createDto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(createDto.getRole()));
            } catch (IllegalArgumentException e) {
                // ignore invalid role and keep default
            }
        }
        return user;
    }

    public static void updateEntityFromDTO(UserDTO dto, User user) {
        if (dto == null || user == null) return;
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        // update role if provided
        if (dto.getRole() != null) {
            try {
                user.setRole(Role.valueOf(dto.getRole()));
            } catch (IllegalArgumentException e) {
                // ignore invalid role
            }
        }
        // password intentionally not updated here
    }
}
