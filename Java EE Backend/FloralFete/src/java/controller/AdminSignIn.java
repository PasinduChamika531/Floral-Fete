package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "AdminSignIn", urlPatterns = {"/AdminSignIn"})
public class AdminSignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(request.getReader(), JsonObject.class);

        JsonObject responseJson = new JsonObject();

        if (jsonObject == null || !jsonObject.has("email") || !jsonObject.has("password")) {
            responseJson.addProperty("response", "Invalid Request");
            sendResponse(response, responseJson);
            return;
        }

        String email = jsonObject.get("email").getAsString();
        String password = jsonObject.get("password").getAsString();

        if (email.equals("pasindu@gmail.com") && password.equals("admin1234")) {
            responseJson.addProperty("response", "success");
        } else {
            responseJson.addProperty("response", "Invalid Credintials");
        }

        sendResponse(response, responseJson);

    }

    private void sendResponse(HttpServletResponse response, JsonObject responseJson) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseJson.toString());
        response.getWriter().flush();
    }

}
