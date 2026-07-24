package com.parabank.qa.utils;

import java.util.UUID;

public class TestDataGenerator {

    private TestDataGenerator() {
    }

    public static UserData newUser() {
        String tag = UUID.randomUUID().toString().substring(0, 8);
        UserData user = new UserData();
        user.firstName = "Alex";
        user.lastName = "Rivera";
        user.address = "742 Maple Street";
        user.city = "Springfield";
        user.state = "IL";
        user.zipCode = "62704";
        user.phone = "5550142890";
        user.ssn = "123-45-" + (1000 + (int) (Math.random() * 8999));
        user.username = "qa_user_" + tag;
        user.password = "Test@" + tag;
        return user;
    }

    public static class UserData {
        public String firstName;
        public String lastName;
        public String address;
        public String city;
        public String state;
        public String zipCode;
        public String phone;
        public String ssn;
        public String username;
        public String password;
    }
}
