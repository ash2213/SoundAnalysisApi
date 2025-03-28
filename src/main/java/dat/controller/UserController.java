package dat.controller;

import dat.dao.UserDAO;
import dat.entities.User;
import dat.exceptions.DatabaseException;
import io.javalin.http.Context;

public class UserController {

    public void createUser(Context ctx) {
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if (email == null || password1 == null || password2 == null || !password1.equals(password2)) {
            ctx.attribute("message", "Passwords do not match or fields are missing");
            ctx.render("register.html");
            return;
        }

        try {
            UserDAO.createUser(email, password1);
            ctx.attribute("message", "Account created! Please log in.");
            ctx.render("login.html");
        } catch (DatabaseException e) {
            ctx.attribute("message", "Registration failed: " + e.getMessage());
            ctx.render("register.html");
        }
    }

    public void login(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        if (email == null || password == null) {
            ctx.attribute("message", "Please fill in both fields");
            ctx.render("login.html");
            return;
        }

        try {
            User user = UserDAO.login(email, password);
            ctx.sessionAttribute("currentUser", user);
            ctx.sessionAttribute("isLoggedIn", true);
            ctx.sessionAttribute("userId", user.getUserId());
            ctx.redirect("/dashboard");
        } catch (DatabaseException e) {
            ctx.attribute("message", "Login failed: " + e.getMessage());
            ctx.render("login.html");
        }
    }

    public void logout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.attribute("message", "You have been logged out.");
        ctx.render("index.html");
    }

    public void renderHomePage(Context ctx) {
        ctx.attribute("message", "Welcome to Thymeleaf with Javalin 6!");
        ctx.render("index.html");
    }
}
