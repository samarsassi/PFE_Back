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
//@Entity

@FieldDefaults(level = AccessLevel.PRIVATE)
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String firstName;
    String lastName;
    int age;
    String email;
    Date birthdate;
    Date inscriptionDate;
    String adress;
    String role;
    String immatriculationNumber ;
    String sectorOfActivity ;

    String levelOfStudies ;
    String domainOfStudies ;

}
