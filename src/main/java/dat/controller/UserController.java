package dat.controller;

import dat.DatabaseException;
import dat.dao.UserDAO;
import dat.entities.User;
import io.javalin.http.Context;
import org.postgresql.jdbc2.optional.ConnectionPool;

public class UserController {


    public static void createUser(Context ctx, ConnectionPool connectionPool) {
        String email = ctx.formParam("email");
        String password1 = ctx.formParam("password1");
        String password2 = ctx.formParam("password2");

        if (!password1.equals(password2)) {
            ctx.attribute("message", "Passwords do not match");
            ctx.render("register.html");
            return;
        }

        try {
            UserDAO.createUser(email, password1, connectionPool);
            ctx.attribute("message", "Account created! Please log in.");
            ctx.render("login.html");
        } catch (DatabaseException e) {
            ctx.attribute("message", e.getMessage());
            ctx.render("register.html");
        }
    }

    public static void login(Context ctx, ConnectionPool connectionPool) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try {
            User user = UserDAO.login(email, password, connectionPool);
            ctx.sessionAttribute("currentUser", user);
            ctx.sessionAttribute("isLoggedIn", true);
            ctx.sessionAttribute("userId", user.getUserId());

            ctx.redirect("/dashboard"); // Redirect after login
        } catch (DatabaseException e) {
            ctx.attribute("message", "Invalid credentials");
            ctx.render("login.html");
        }
    }

    public static void logout(Context ctx) {
        ctx.sessionAttribute("currentUser", null);
        ctx.sessionAttribute("isLoggedIn", false);
        ctx.sessionAttribute("userId", null);

        ctx.redirect("/");
    }

    public void renderHomePage(Context ctx) {
        ctx.attribute("message", "Welcome to Thymeleaf with Javalin 6!");
        ctx.render("index.html"); // This will look for resources/templates/index.html
    }
    public static void dashboard(Context ctx) {
        User user = ctx.sessionAttribute("currentUser");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        ctx.attribute("currentUser", user);
        ctx.render("dashboard.html");
    }
}