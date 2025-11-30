// package com.sagafitmi.ecommerce.service;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import org.mockito.ArgumentCaptor;

// import com.sagafitmi.ecommerce.dto.UserCreateDTO;
// import com.sagafitmi.ecommerce.dto.UserDTO;
// import com.sagafitmi.ecommerce.model.Role;
// import com.sagafitmi.ecommerce.model.User;
// import com.sagafitmi.ecommerce.repository.OrderRepository;
// import com.sagafitmi.ecommerce.repository.UserRepository;
// import com.sagafitmi.ecommerce.service.impl.UserServiceImpl;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

// class UserServiceImplTest {

//     UserRepository userRepository;
//     BCryptPasswordEncoder passwordEncoder;
//     UserServiceImpl service;
//     OrderRepository orderRepository;

//     @BeforeEach
//     void setUp() {
//         userRepository = mock(UserRepository.class);
//         passwordEncoder = mock(BCryptPasswordEncoder.class);
//         orderRepository = mock(OrderRepository.class);
//         service = new UserServiceImpl(userRepository, passwordEncoder, "pepper");
//     }

//     @Test
//     void getAllUsers_maps() {
//         User u = new User();
//         u.setId(1L);
//         u.setName("A");
//         u.setEmail("a@x.com");
//         u.setRole(Role.USER);

//         when(userRepository.findAll()).thenReturn(List.of(u));

//         var list = service.getAllUsers();
//         assertEquals(1, list.size());
//         UserDTO dto = list.get(0);
//         assertEquals(1L, dto.getId());
//         assertEquals("A", dto.getName());
//     }

//     @Test
//     void createUser_null_returnsNull() {
//         assertNull(service.createUser(null));
//     }

//     @Test
//     void createUser_duplicateEmail_returnsNull() {
//         UserCreateDTO create = new UserCreateDTO();
//         create.setEmail("a@x.com");
//         when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(new User()));
//         assertNull(service.createUser(create));
//     }

//     @Test
//     void createUser_hashesPassword_andSaves() {
//         UserCreateDTO create = new UserCreateDTO();
//         create.setName("B");
//         create.setEmail("b@x.com");
//         create.setPassword("pw");

//         when(userRepository.findByEmail("b@x.com")).thenReturn(Optional.empty());
//         when(passwordEncoder.encode("pwpepper")).thenReturn("hashed");

//         when(userRepository.save(any(User.class))).thenAnswer(i -> {
//             User u = i.getArgument(0);
//             u.setId(5L);
//             return u;
//         });

//         var dto = service.createUser(create);
//         assertNotNull(dto);
//         assertEquals(5L, dto.getId());

//         ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
//         verify(userRepository).save(captor.capture());
//         assertEquals("hashed", captor.getValue().getPassword());
//     }

//     @Test
//     void updateUser_notExists_returnsNull() {
//         when(userRepository.existsById(9L)).thenReturn(false);
//         assertNull(service.updateUser(9L, new UserDTO()));
//     }

//     @Test
//     void updateUser_exists_updates() {
//         User existing = new User();
//         existing.setId(8L);
//         existing.setName("Old");
//         when(userRepository.existsById(8L)).thenReturn(true);
//         when(userRepository.findById(8L)).thenReturn(Optional.of(existing));
//         when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

//         UserDTO dto = new UserDTO();
//         dto.setName("New");
//         dto.setEmail("n@x.com");

//         var res = service.updateUser(8L, dto);
//         assertNotNull(res);
//         assertEquals("New", res.getName());
//     }

//     @Test
//     void findByEmail_returnsDTOOrNull() {
//         User u = new User(); u.setId(3L); u.setEmail("c@x.com");
//         when(userRepository.findByEmail("c@x.com")).thenReturn(Optional.of(u));
//         assertNotNull(service.findByEmail("c@x.com"));
//         when(userRepository.findByEmail("no@x.com")).thenReturn(Optional.empty());
//         assertNull(service.findByEmail("no@x.com"));
//     }

//     @Test
//     void deleteUser_checksExists() {
//         when(userRepository.existsById(4L)).thenReturn(true);
//         service.deleteUser(4L);
//         verify(userRepository).deleteById(4L);

//         when(userRepository.existsById(7L)).thenReturn(false);
//         service.deleteUser(7L);
//         verify(userRepository, never()).deleteById(7L);
//     }

//     @Test
//     void authenticate_handlesNulls_andChecksPassword() {
//         assertFalse(service.authenticate(null, "x"));
//         assertFalse(service.authenticate("a@x.com", null));

//         User u = new User(); u.setEmail("a@x.com"); u.setPassword("stored");
//         when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(u));
//         when(passwordEncoder.matches("pwpepper", "stored")).thenReturn(true);

//         assertTrue(service.authenticate("a@x.com", "pw"));
//         when(passwordEncoder.matches("badpepper", "stored")).thenReturn(false);
//         assertFalse(service.authenticate("a@x.com", "bad"));
//     }
// }
