package com.example.recrutement.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Evaluation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int note;
    private String feedback;
    private Date dateEvaluation;

    @OneToOne
    @JoinColumn(name = "candidature_id")
    private Candidature candidature;

    //@ManyToOne
    //@JoinColumn(name = "manager_id")
    //private User manager;

}
