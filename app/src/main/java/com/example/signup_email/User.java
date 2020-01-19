package com.example.signup_email;

public class User {
    private String user_name;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getUser_pass() {
        return user_pass;
    }

    public void setUser_pass(String user_pass) {
        this.user_pass = user_pass;
    }

    private String user_email;
    private String user_phone;
    private String user_pass;

    public User(){

    }

    public  User(String name, String email, String mobile, String pass)
    {
        this.user_name = name;
        this.user_email = email;
        this.user_phone = mobile;
        this.user_pass = pass;
    }

}
