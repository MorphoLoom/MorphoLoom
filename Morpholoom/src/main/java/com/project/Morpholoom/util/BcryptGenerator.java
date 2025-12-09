package com.project.Morpholoom.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = args.length > 0 ? args[0] : "elxhslr1!";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
    }
}

