package com.example.recrutement.services;

import com.example.recrutement.entities.User;

import java.util.List;

public interface IUserService {
    public List<User> retrieveAllUsers() ;
    public User retrieveUser(Long id) ;
    public User addUser(User u) ;
    public void removeUser(Long id) ;
    public User modifyUser(User u) ;
}
