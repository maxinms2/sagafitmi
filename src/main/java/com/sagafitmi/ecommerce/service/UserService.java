package com.sagafitmi.ecommerce.service;

import java.util.List;

import com.sagafitmi.ecommerce.dto.UserCreateDTO;
import com.sagafitmi.ecommerce.dto.UserDTO;

public interface UserService {

    List<UserDTO> getAllUsers();

    UserDTO getUserById(Long id);

    UserDTO createUser(UserCreateDTO userCreateDTO);

    UserDTO updateUser(Long id, UserDTO userDTO);

    UserDTO findByEmail(String email);

    void deleteUser(Long id);

    /**
     * Verifica que el email y la contraseña sean válidos.
     * @param email correo del usuario
     * @param password contraseña en texto plano proporcionada por el usuario
     * @return true si las credenciales son correctas
     */
    boolean authenticate(String email, String password);

}
