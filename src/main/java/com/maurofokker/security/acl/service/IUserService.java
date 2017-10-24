package com.maurofokker.security.acl.service;

import com.maurofokker.security.acl.model.User;
import com.maurofokker.security.acl.validation.EmailExistsException;

public interface IUserService {

    User registerNewUser(User user) throws EmailExistsException;

    User findUserByEmail(String email);

}
