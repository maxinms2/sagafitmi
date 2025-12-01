package com.sagafitmi.ecommerce.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.sagafitmi.ecommerce.dto.UserCreateDTO;
import com.sagafitmi.ecommerce.dto.UserDTO;
import com.sagafitmi.ecommerce.exception.UserCreateException;
import com.sagafitmi.ecommerce.exception.UserDeleteException;
import com.sagafitmi.ecommerce.exception.UserUpdateException;
import com.sagafitmi.ecommerce.mapper.UserMapper;
import com.sagafitmi.ecommerce.model.Role;
import com.sagafitmi.ecommerce.model.User;
import com.sagafitmi.ecommerce.repository.UserRepository;
import com.sagafitmi.ecommerce.repository.OrderRepository;
import com.sagafitmi.ecommerce.repository.CartItemRepository;
import com.sagafitmi.ecommerce.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Value("${SUPER_USER:edgar.aws.0307@gmail.com}")
    private String superUserEmail;

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String pepper;

    public UserServiceImpl(UserRepository userRepository,
            OrderRepository orderRepository,
            CartItemRepository cartItemRepository,
            BCryptPasswordEncoder passwordEncoder,
            @Value("${app.security.pepper:Peluso3000.}") String pepper) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.cartItemRepository = cartItemRepository;
        this.passwordEncoder = passwordEncoder;
        this.pepper = pepper != null ? pepper : "";
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(UserMapper::toDTO).toList();
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return UserMapper.toDTO(user);
    }

    @Override
    public UserDTO createUser(UserCreateDTO userCreateDTO) {
        if (userCreateDTO == null) return null;

        // Simple uniqueness check: if email already exists, return null
        if (userCreateDTO.getEmail() != null && userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()) {
            throw new UserCreateException("Usuario ya existe con el email: " + userCreateDTO.getEmail());
        }

        User user = UserMapper.toEntity(userCreateDTO);
        // Ensure newly created users always get the USER role
        user.setRole(Role.USER);
        // hash password with pepper
        String raw = userCreateDTO.getPassword() != null ? userCreateDTO.getPassword() : "";
        String toHash = raw + (pepper != null ? pepper : "");
        user.setPassword(passwordEncoder.encode(toHash));

        user = userRepository.save(user);
        return UserMapper.toDTO(user);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing == null){
            throw new UserUpdateException(" usuario no existe con id: " + id);
        }
        if(isSuperUser(existing.getEmail())){
            throw new UserUpdateException(" No se puede modificar el super usuario");
        }
        UserMapper.updateEntityFromDTO(userDTO, existing);
        existing = userRepository.save(existing);
        return UserMapper.toDTO(existing);
    }

    @Override
    public UserDTO findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDTO).orElse(null);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if(user == null){
            throw new UserDeleteException(" usuario no existe con id: " + id);
        }
        if (isSuperUser(user.getEmail())) {
            throw new UserDeleteException(" No se puede eliminar el super usuario");
        }   
        // comprobaciones eficientes: usar queries "exists" para evitar traer listas
        Long userId = user.getId();
        boolean hasCartItems = cartItemRepository.existsByUserId(userId);
        boolean hasOrders = orderRepository.existsByUserId(userId);

        if (hasCartItems || hasOrders) {
            // No eliminar: existe información relacionada en otras tablas
            throw new UserDeleteException(" No se puede eliminar el usuario porque tiene información relacionada en otras tablas");
        }

        userRepository.deleteById(id);
    }

    @Override
    public boolean authenticate(String email, String password) {
        if (email == null || password == null) return false;
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || user.getPassword() == null) return false;
        String toCheck = password + (pepper != null ? pepper : "");
        return passwordEncoder.matches(toCheck, user.getPassword());
    }

    private boolean isSuperUser(String email) {
        return superUserEmail != null && superUserEmail.equalsIgnoreCase(email);
    }
}
