package com.example.springtest.controller;

import com.example.springtest.model.Schedule;
import com.example.springtest.model.User;
import com.example.springtest.model.Services;
import com.example.springtest.model.ScheduleRepository;
import com.example.springtest.model.UserRepository;
import com.example.springtest.model.ServicesRepository;
import com.example.springtest.midleware.JwtUtil;
import com.example.springtest.request.ScheduleRequest;
import com.example.springtest.request.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServicesRepository servicesRepository;

    @Autowired
    private JwtUtil jwtUtil;


    private String extractEmailFromToken(String token) {
        return jwtUtil.extractEmail(token.replace("Bearer ", ""));
    }

    private boolean isUserValid(String email) {
        return userRepository.findByEmail(email) != null;
    }

    @PostMapping("/byUser")
    public ResponseEntity<List<Schedule>> getSchedulesByUserId(
            @RequestHeader("Authorization") String token,
            @RequestBody UserRequest userRequest,
            @RequestParam(value = "filter", required = false) String filter,
            @RequestParam(value = "sortby", required = false) String sortby) {

        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Optional<User> userOptional = userRepository.findById(userRequest.getId());
        if (userOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User user = userOptional.get();
        List<Schedule> schedules = scheduleRepository.findByUserId(user.getId());


        if (filter != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date filterDate = dateFormat.parse(filter);
                schedules = schedules.stream()
                        .filter(schedule -> dateFormat.format(schedule.getDate()).equals(dateFormat.format(filterDate)))
                        .collect(Collectors.toList());
            } catch (ParseException e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }


        if (sortby != null) {
            if (sortby.equalsIgnoreCase("asc")) {
                schedules.sort((s1, s2) -> s1.getDate().compareTo(s2.getDate()));
            } else if (sortby.equalsIgnoreCase("desc")) {
                schedules.sort((s1, s2) -> s2.getDate().compareTo(s1.getDate()));
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Schedule> getScheduleById(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(id);
        if (scheduleOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(scheduleOptional.get(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Schedule>> getAllSchedules(@RequestHeader("Authorization") String token) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Schedule> schedules = scheduleRepository.findAll();
        return new ResponseEntity<>(schedules, HttpStatus.OK);
    }




    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestHeader("Authorization") String token, @RequestBody ScheduleRequest scheduleRequest) {
        System.out.println("1111111111111111111111");
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        System.out.println("2222222222222222222222222");
        if (scheduleRequest.getUser_id() == null || scheduleRequest.getService_id() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        System.out.println("333333333333333333333333");
        Optional<User> userOptional = userRepository.findById(scheduleRequest.getUser_id());
        Optional<Services> serviceOptional = servicesRepository.findById(scheduleRequest.getService_id());

        System.out.println("44444444444444444444444444");
        if (userOptional.isEmpty() || serviceOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        System.out.println("55555555555555555555555555555  chegou aqui?");
        User user = userOptional.get();
        Services service = serviceOptional.get();

        System.out.println("666666666666666666666666666666");
        Schedule schedule = new Schedule();
        schedule.setUser(user);
        schedule.setService(service);

        System.out.println("7777777777777777777777777777");
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        try {
            System.out.println("88888888888888888888888888888888");
            Date parsedDate = dateFormat.parse(scheduleRequest.getDate());
            schedule.setDate(parsedDate);
        } catch (ParseException e) {
            System.out.println("9999999999999999999999999999999");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx final");
        Schedule createdSchedule = scheduleRepository.save(schedule);

        return new ResponseEntity<>(createdSchedule, HttpStatus.CREATED);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSchedule(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        String email = extractEmailFromToken(token);
        if (!isUserValid(email)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            scheduleRepository.deleteById(id);
            return new ResponseEntity<>("Agendamento deletado com sucesso.", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Falha ao deletar agendamento.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}