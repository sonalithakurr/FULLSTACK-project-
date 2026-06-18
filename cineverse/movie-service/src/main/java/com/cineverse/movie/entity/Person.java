package com.cineverse.movie.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persons")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    private PersonRole role;   // ACTOR, DIRECTOR, WRITER, PRODUCER

    private String photoUrl;

    public enum PersonRole {
        ACTOR, DIRECTOR, WRITER, PRODUCER
    }
}
