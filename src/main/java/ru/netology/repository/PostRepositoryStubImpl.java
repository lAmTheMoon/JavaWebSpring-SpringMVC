package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.model.Post;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository("postRepository")
public class PostRepositoryStubImpl implements PostRepository {

    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private AtomicLong id = new AtomicLong(0);

    public List<Post> all() {
        if (posts.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(posts.values());
    }

    public Optional<Post> getById(long id) {
        return Optional.of(posts.get(id));
    }

    public Post save(Post post) {
        if (post.getId() == 0) {
            Long newId = id.incrementAndGet();
            post = post.createNew(newId);
        }
        posts.put(post.getId(), post);
        return post;
    }

    public void removeById(long id) {
        posts.remove(id, posts.get(id));
    }
}