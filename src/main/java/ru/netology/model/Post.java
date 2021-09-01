package ru.netology.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Post {
    private long id;
    private String content;
    private boolean isRemoved;

    public Post() {
    }

    public Post(long id, String content) {
        this.id = id;
        this.content = content;
        isRemoved = false;
    }

    public Post createNew(Long id) {
        return new Post(id, content);
    }
}