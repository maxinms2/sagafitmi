package com.sagafitmi.ecommerce.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.sagafitmi.ecommerce.dto.UserCreateDTO;
import com.sagafitmi.ecommerce.dto.UserDTO;
import com.sagafitmi.ecommerce.model.Role;
import com.sagafitmi.ecommerce.model.User;

class UserMapperTest {

    @Test
    void toDTO_nullInput_returnsNull() {
        assertNull(UserMapper.toDTO(null));
    }

    @Test
    void toDTO_mapsFieldsAndRole() {
        User u = new User();
        u.setId(2L);
        u.setName("Alice");
        u.setEmail("a@x.com");
        u.setRole(Role.ADMIN);

        UserDTO dto = UserMapper.toDTO(u);

        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("Alice", dto.getName());
        assertEquals("a@x.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    void toEntity_withValidRole_setsRole() {
        UserCreateDTO create = new UserCreateDTO();
        create.setName("Bob");
        create.setEmail("b@x.com");
        create.setPassword("secret");
        create.setRole("USER");

        User u = UserMapper.toEntity(create);

        assertNotNull(u);
        assertEquals("Bob", u.getName());
        assertEquals("b@x.com", u.getEmail());
        assertEquals("secret", u.getPassword());
        assertEquals(Role.USER, u.getRole());
    }

    @Test
    void toEntity_withInvalidRole_ignoresRole() {
        UserCreateDTO create = new UserCreateDTO();
        create.setName("C");
        create.setEmail("c@x.com");
        create.setPassword("p");
        create.setRole("NO_SUCH_ROLE");

        User u = UserMapper.toEntity(create);

        assertNotNull(u);
        // The entity has a default role (Role.USER) when no valid role is provided
        assertEquals(Role.USER, u.getRole());
    }

    @Test
    void updateEntityFromDTO_updatesFieldsAndRole() {
        User user = new User();
        user.setName("Old");
        user.setEmail("old@x.com");
        user.setRole(Role.USER);

        UserDTO dto = new UserDTO();
        dto.setName("New");
        dto.setEmail("new@x.com");
        dto.setRole("ADMIN");

        UserMapper.updateEntityFromDTO(dto, user);

        assertEquals("New", user.getName());
        assertEquals("new@x.com", user.getEmail());
        assertEquals(Role.ADMIN, user.getRole());
    }
}
