package ru.netology.repository;

import ru.netology.model.Post;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PostRepository {

    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private static final AtomicInteger id = new AtomicInteger(0);

    public List<Post> all() {
        return Collections.emptyList();
    }

    public Optional<Post> getById(long id) {
        return Optional.empty();
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            Long newId = (long) id.getAndIncrement();
            post = post.createNew(newId);
        }
        posts.put(post.getId(), post);
        return post;
    }

    public void removeById(long id) {
        posts.remove(id, posts.get(id));
    }
}