package com.sathish.thodar.features.auth;

import com.sathish.thodar.util.ConsoleInput;
import com.sathish.thodar.util.ParseHelper;
import com.sathish.thodar.data.repository.ThodarDB;
import com.sathish.thodar.data.dto.enums.Role;
import com.sathish.thodar.data.dto.request.auth.LoginRequest;
import com.sathish.thodar.data.dto.request.auth.RegisterRequest;
import com.sathish.thodar.data.dto.response.auth.AuthResponse;
import com.sathish.thodar.features.admin.AdminView;
import com.sathish.thodar.features.passenger.PassengerView;

public class AuthView {

    private final ThodarDB db = ThodarDB.getInstance();

    public AuthView() {
        
        if (db.getUserByEmail("admin@thodar.com") == null) {
            RegisterRequest admin = new RegisterRequest();
            admin.setName("Super Admin");
            admin.setEmail("admin@thodar.com");
            admin.setPassword("Admin@123");
            admin.setMobileNo("9999999999");
            admin.setRole(Role.ADMIN);
            
            db.addUser(admin);
        }
    }

    public void showLandingMenu() {
        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Passenger Login");
            System.out.println("2. Passenger Register");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            
            String choice = ConsoleInput.getString("Choice: ").trim();

            switch (choice) {
                case "1":
                    handleLogin(Role.CUSTOMER);
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    handleLogin(Role.ADMIN);
                    break;
                case "4":
                    System.out.println("Thank you for using Thodar Railways!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private String getValidEmail() {
        while (true) {
            String email = ConsoleInput.getString("Email: ");
            if (ParseHelper.isValidEmail(email)) {
                return email;
            } else {
                System.out.println(" Invalid Email Format! (e.g., user@example.com)");
            }
        }
    }

    private String getValidPasswordForRegistration() {
        while (true) {
            String pwd = ConsoleInput.getString("Password (Min 8 chars, 1 Upper, 1 Lower, 1 Number, 1 Symbol): ");
            boolean hasUpper = pwd.matches(".*[A-Z].*");
            boolean hasLower = pwd.matches(".*[a-z].*");
            boolean hasNum = pwd.matches(".*\\d.*");
            boolean hasSymbol = pwd.matches(".*[^A-Za-z0-9].*");
            
            if (pwd.length() >= 8 && hasUpper && hasLower && hasNum && hasSymbol) {
                String confirmPwd = ConsoleInput.getString("Confirm Password: ");
                if (pwd.equals(confirmPwd)) {
                    return pwd;
                } else {
                    System.out.println(" Passwords do not match! Please try again.");
                }
            } else {
                System.out.println(" Weak Password! Must be at least 8 characters and contain Upper, Lower, Number and Symbol.");
            }
        }
    }

    private String getValidMobile() {
        while (true) {
            String mobile = ConsoleInput.getString("Mobile No (10 digits): ");
            if (ParseHelper.isValidMobile(mobile)) {
                return mobile;
            } else {
                System.out.println(" Invalid Mobile Number!");
            }
        }
    }

    private void handleRegister() {
        System.out.println("\n--- REGISTRATION ---");
        RegisterRequest newUser = new RegisterRequest();
        
        newUser.setName(ConsoleInput.getString("Name: "));
        newUser.setEmail(getValidEmail());
        newUser.setPassword(getValidPasswordForRegistration());
        newUser.setMobileNo(getValidMobile());
        
        newUser.setRole(Role.CUSTOMER);
        newUser.setWalletBalance(0.0);
        
        db.addUser(newUser);
        System.out.println(" Registered Successfully! Please login and recharge your wallet to book tickets.");
    }

    private void handleLogin(Role requiredRole) {
        if (requiredRole == Role.ADMIN) {
            System.out.println("\n--- SECURE ADMIN PORTAL ---");
        } else {
            System.out.println("\n--- PASSENGER LOGIN ---");
        }
        
        LoginRequest req = new LoginRequest();
        req.setEmail(getValidEmail());
        req.setPassword(ConsoleInput.getString("Password: "));
        

        RegisterRequest userEntity = db.authenticateUser(req.getEmail(), req.getPassword());
        
        if (userEntity != null && userEntity.getRole() == requiredRole) {
            AuthResponse resp = new AuthResponse();
            resp.setId(userEntity.getId());
            resp.setName(userEntity.getName());
            resp.setRole(userEntity.getRole());
            
            System.out.println("\nWelcome, " + resp.getName() + "!");
            
            if (requiredRole == Role.ADMIN) {
                new AdminView().showAdminMenu();
            } else {
                new PassengerView(resp, userEntity).showPassengerMenu();
            }
        } else {
            System.out.println(" Invalid Credentials!");
        }
    }
}