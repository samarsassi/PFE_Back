package com.example.recrutement.services;

import com.example.recrutement.entities.Challenge;
import com.example.recrutement.repositories.ChallengeRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepo repo;

    @InjectMocks
    private ChallengeService service;

    @Test
    void save_delegatesToRepository() {
        Challenge c = new Challenge();
        service.save(c);
        verify(repo).save(c);
    }

    @Test
    void deleteById_delegatesToRepository() {
        service.deleteById(3);
        verify(repo).deleteById(3);
    }
}


