package com.example.springtest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Table(name = "services")
@Entity(name = "services")
@Getter
@Setter

public class Services {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private String description;

    @Setter
    @Getter
    private String image;

    @Setter
    @Getter
    private Double value;

    @Column(name = "createdAt")
    private Date createdAt;

    @Column(name = "updatedAt")
    private Date updatedAt;

    @Column(name = "deletedAt")
    private Date deletedAt;

    @OneToMany(mappedBy = "service")
    @JsonIgnore
    private List<Schedule> schedules;
}