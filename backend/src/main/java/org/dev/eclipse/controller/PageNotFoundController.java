package org.dev.eclipse.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
public class PageNotFoundController implements ErrorController {

    @RequestMapping("/error")
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int status = resolveStatusCode(request);
        response.setStatus(status);

        if (status == HttpServletResponse.SC_NOT_FOUND) {
            request.getRequestDispatcher("/error/404.html").forward(request, response);
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("");
    }

    private int resolveStatusCode(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode == null) {
            return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        return Integer.parseInt(statusCode.toString());
    }
}
