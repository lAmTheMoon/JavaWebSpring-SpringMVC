package ru.netology.servlet;



import ru.netology.controller.PostController;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "main-servlet", value = "/api/posts")
public class MainServlet extends HttpServlet {

    private PostController controller;
    private final static String API_PATH = "/api/posts";
    private final static String API_PATH_WITH_NUMBERS = API_PATH + "\\d+";

    @Override
    public void init() {
        final var repository = new PostRepository();
        final var service = new PostService(repository);
        controller = new PostController(service);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        final var path = req.getRequestURI();
        if (path.equals(API_PATH)) {
            controller.all(resp);
            return;
        }
        if (path.matches(API_PATH_WITH_NUMBERS)) {
            final var id = Long.parseLong(path.substring(path.lastIndexOf("/")));
            controller.getById(id, resp);
            return;
        }
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        if (req.getRequestURI().equals(API_PATH)) {
            controller.save(req.getReader(), resp);
            return;
        }
        super.doPost(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final var path = req.getRequestURI();
        if (req.getRequestURI().matches(API_PATH_WITH_NUMBERS)) {
            final var id = Long.parseLong(path.substring(path.lastIndexOf("/")));
            controller.removeById(id, resp);
            return;
        }
        super.doDelete(req, resp);
    }
}