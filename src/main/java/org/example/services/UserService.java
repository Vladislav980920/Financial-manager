package org.example.services;

import org.example.dao.UserDao;
import org.example.model.User;

public class UserService {
    private final UserDao userDao = new UserDao();

    public User authenticate(String username, String password) throws Exception {
        User user = userDao.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public void register(User user) throws Exception {
        if (userDao.userExists(user.getUsername())) {
            throw new Exception("Пользователь с таким именем уже существует");
        }
        userDao.addUser(user);
    }
}
