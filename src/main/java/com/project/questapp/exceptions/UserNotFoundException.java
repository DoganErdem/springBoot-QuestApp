package com.project.questapp.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(){
        super();
    }

    public UserNotFoundException(String message){
        super(message);
    }
}
