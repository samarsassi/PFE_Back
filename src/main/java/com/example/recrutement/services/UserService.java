/* package com.example.recrutement.services;

import com.example.recrutement.entities.User;
import com.example.recrutement.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService implements IUserService {
    @Autowired
    UserRepo userRepository;

    @Override
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User retrieveUser(Long id) {
        return userRepository.findById(id).get();
    }

    @Override
    public User addUser(User u) {
        u.setInscriptionDate(new Date());
        return userRepository.save(u);
    }

    @Override
    public void removeUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User modifyUser(User u) {
        return userRepository.save(u);
    }
}
*/
