package com.example.springtest.controller;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.springtest.model.Services;
import com.example.springtest.model.ServicesRepository;
import com.example.springtest.model.UserRepository;
import com.example.springtest.midleware.JwtUtil;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/services")
public class ServicesController {

    @Autowired
    private ServicesRepository servicesRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token.replace("Bearer ", ""));
    }

    private boolean isUserValid(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @GetMapping
    public ResponseEntity<List<Services>> getAllServices(@RequestHeader("Authorization") String token) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Services> services = servicesRepository.findAll();
        return new ResponseEntity<>(services, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Services> getServiceById(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Services> service = servicesRepository.findById(id);
        return service.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Services> createService(@RequestHeader("Authorization") String token, @RequestBody Services service) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Services createdService = servicesRepository.save(service);
        return new ResponseEntity<>(createdService, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Services> editService(@RequestHeader("Authorization") String token, @PathVariable Integer id, @RequestBody Services serviceDetails) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<Services> optionalService = servicesRepository.findById(id);
        if (optionalService.isPresent()) {
            Services service = optionalService.get();


            BeanUtils.copyProperties(serviceDetails, service, "id", "createdAt", "updatedAt", "deletedAt");

            Services updatedService = servicesRepository.save(service);
            return new ResponseEntity<>(updatedService, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteService(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            servicesRepository.deleteById(id);
            return new ResponseEntity<>("Serviço deletado com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Falha ao deletar serviço.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
