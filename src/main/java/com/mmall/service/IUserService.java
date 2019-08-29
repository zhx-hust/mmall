package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String string, String type);

    ServerResponse<String> GetQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);

    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    ServerResponse<String> resetPassword(String passwodOld, String passwoldNew, User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(int id);
    ServerResponse checkAdminRole(User user);
}
