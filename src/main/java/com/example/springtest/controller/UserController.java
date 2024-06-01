package com.example.springtest.controller;

import com.example.springtest.midleware.JwtUtil;
import com.example.springtest.model.User;
import com.example.springtest.model.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userRepository.findByEmail(email);
    }

    @PostMapping("/create")
    public Map<String, Object> createUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();

        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        response.put("status", "success");
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("jwt", jwtToken);

        return response;
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateUser(@RequestHeader("Authorization") String token, @PathVariable Integer id, @RequestBody User userDetails) {
        Map<String, Object> response = new HashMap<>();

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email);

        if (user == null || !user.getId().equals(id)) {
            response.put("status", "error");
            response.put("message", "Acesso não autorizado.");
            return response;
        }

        BeanUtils.copyProperties(userDetails, user, "id", "createdAt", "updatedAt", "deletedAt", "password", "email");

        if (StringUtils.hasText(userDetails.getPassword())) {
            user.setPassword(BCrypt.hashpw(userDetails.getPassword(), BCrypt.gensalt()));
        }
        user.setUpdatedAt(new Date());

        userRepository.save(user);

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        response.put("status", "success");
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("jwt", jwtToken);

        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> loginUser(@RequestBody User loginUser) {
        Map<String, Object> response = new HashMap<>();

        String email = loginUser.getEmail();
        String password = loginUser.getPassword();

        User user = userRepository.findByEmail(email);

        if (user == null || !BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("Credenciais inválidas. Por favor, verifique seu email e senha.");
        }

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        response.put("status", "success");
        response.put("id", user.getId());
        response.put("name", user.getName());
        response.put("jwt", jwtToken);

        return response;
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteUser(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email);

        if (user == null || !user.getId().equals(id)) {
            response.put("status", "error");
            response.put("message", "Acesso não autorizado.");
            return response;
        }

        try {
            userRepository.deleteById(id);
            response.put("status", "success");
            response.put("message", "Usuário deletado com sucesso.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Falha ao deletar usuário.");
        }

        return response;
    }
}