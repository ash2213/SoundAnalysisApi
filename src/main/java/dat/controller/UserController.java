package dat.controller;

import dat.dao.UserDAO;
import dat.dtos.UserDTO;
import dat.entities.User;
import dat.exceptions.DatabaseException;
import io.javalin.http.Context;
import dat.utils.JwtUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;

public class UserController {

    public void createUser(Context ctx) {
        UserDTO dto = ctx.bodyAsClass(UserDTO.class);

        String email = dto.getEmail();
        String password = dto.getPassword();

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            ctx.status(400).json(Map.of("error", "Missing email or password"));
            return;
        }

        try {
            UserDAO.createUser(email, password);
            ctx.status(201).json(Map.of("message", "User created successfully"));
        } catch (DatabaseException e) {
            if (e.getMessage().toLowerCase().contains("duplicate")) {
                ctx.status(409).json(Map.of("error", "Email already exists"));
            } else {
                ctx.status(500).json(Map.of("error", "Registration failed: " + e.getMessage()));
            }
        }
    }

    // ---------- API: JSON-based login (POST /api/user/login) ----------
    public void login(Context ctx) {
        try {
            System.out.println("🔐 Login started");

            UserDTO dto = ctx.bodyAsClass(UserDTO.class);
            String email = dto.getEmail();
            String password = dto.getPassword();
            System.out.println("📨 Email: " + email);

            User user = UserDAO.login(email, password); // Can still throw exception
            System.out.println("✅ User authenticated: " + user.getEmail());

            // JWT could fail if misconfigured
            String token = JwtUtils.generateToken(user.getEmail());
            System.out.println("🔑 Token created: " + token);

            ctx.status(200).json(Map.of("token", token));
        } catch (Exception e) {
            System.err.println("❌ Login failed: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of("error", "Login failed: " + e.getMessage()));
        }
    }


}
