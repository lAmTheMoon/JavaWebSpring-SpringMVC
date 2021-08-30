package ru.netology.servlet;

import ru.netology.controller.PostController;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

public class MainServlet extends HttpServlet {

    private PostController controller;
    private final static String API_PATH = "/api/posts";
    private final static String API_PATH_WITH_NUMBERS = API_PATH + "/\\d+";

    @Override
    public void init() {
        final var repository = new PostRepository();
        final var service = new PostService(repository);
        controller = new PostController(service);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final var path = req.getRequestURI();
        if (Pattern.matches(API_PATH_WITH_NUMBERS, path)) {
            final var id = extractId(path);
            controller.getById(id, resp);
            return;
        }
        controller.all(resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        controller.save(req.getReader(), resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final var path = req.getRequestURI();
        if (Pattern.matches(API_PATH_WITH_NUMBERS, path)) {
            final var id = extractId(path);
            controller.removeById(id, resp);
        }
    }

    private long extractId(String path) {
        String NUMBER_PATTERN = "[^0-9]";
        return Long.parseLong(path.replaceAll(NUMBER_PATTERN, ""));
    }
}