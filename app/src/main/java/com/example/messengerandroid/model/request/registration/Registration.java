package com.example.messengerandroid.model.request.registration;

import com.example.messengerandroid.model.request.registration.Account;
import com.example.messengerandroid.model.request.registration.User;

public class Registration {
    private Account accountDto;
    private User userDto;

    public Account getAccountDto() {
        return accountDto;
    }

    public void setAccountDto(Account accountDto) {
        this.accountDto = accountDto;
    }

    public User getUserDto() {
        return userDto;
    }

    public void setUserDto(User userDto) {
        this.userDto = userDto;
    }
}
