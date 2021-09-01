package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class PostRepositoryStubImpl implements PostRepository {

    private final Map<Long, Post> posts = new ConcurrentHashMap<>();
    private AtomicLong id = new AtomicLong(0);

    public List<Post> all() {
        if (posts.isEmpty()) return Collections.emptyList();
        return posts.values().stream().filter(p -> !p.isRemoved()).collect(Collectors.toList());
    }

    public Optional<Post> getById(long id) {
        return Optional.of(posts.get(id));
    }

    public Post save(Post post) {
        if (post.getId() != 0 && posts.get(post.getId()).isRemoved()) {
            throw new NotFoundException();
        }
        if (post.getId() == 0) {
            Long newId = id.incrementAndGet();
            post = post.createNew(newId);
        }
        posts.put(post.getId(), post);
        return post;
    }

    public Post removeById(long id) {
        posts.get(id).setRemoved(true);
        return posts.get(id);
    }
}