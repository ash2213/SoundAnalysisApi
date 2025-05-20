package dat.controller;

import dat.dao.UserDAO;
import dat.dtos.UserDTO;
import dat.entities.User;
import dat.exceptions.DatabaseException;
import io.javalin.http.Context;
import dat.utils.JwtUtils;
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
        UserDTO dto = ctx.bodyAsClass(UserDTO.class);
        String email = dto.getEmail();
        String password = dto.getPassword();

        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            ctx.status(400).result("Missing email or password");
            return;
        }

        try {
            User user = UserDAO.login(email, password); // this verifies the user and returns the User object

            // ✅ Generate JWT token based on email or user ID
            String token = JwtUtils.generateToken(user.getEmail());

            // ✅ Return token instead of the user object
            ctx.status(200).json(Map.of("token", token));

        } catch (DatabaseException e) {
            ctx.status(401).result("Login failed: " + e.getMessage());
        }
    }
}
