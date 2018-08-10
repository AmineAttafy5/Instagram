package com.keremturker.instapp;

public interface AuthenticationListener {

    void onCodeReceived(String auth_token);
}
