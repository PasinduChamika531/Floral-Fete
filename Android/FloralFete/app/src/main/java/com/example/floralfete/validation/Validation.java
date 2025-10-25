package com.example.floralfete.validation;

import java.util.regex.Pattern;

public class Validation {

    public static boolean isValidEmail(String email) {
        return !email.isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        Pattern PASSWORD_PATTERN =
                Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*!]).{6,}$");
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}
