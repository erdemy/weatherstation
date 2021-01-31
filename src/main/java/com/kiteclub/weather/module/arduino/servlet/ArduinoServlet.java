package com.kiteclub.weather.module.arduino.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "Arduino Server Servlet",
        description = "Expect data from arduino device",
        urlPatterns = {"/arduino"},
        loadOnStartup = 1
)
public class ArduinoServlet extends HttpServlet {
    private static final long serialVersionUID = 187239872983472L;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<p>Hello World!</p>");
    }
}