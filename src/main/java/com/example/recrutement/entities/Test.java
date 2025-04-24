package com.example.recrutement.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Test implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String lienTest;
    int testScore;
    Date datePassage;
    @OneToOne
    @JoinColumn(name = "candidature_id")
    private Candidature candidature;

    //@OneToOne
    //@JoinColumn(name = "rh_id")
    //private User rh; //crée par
}
