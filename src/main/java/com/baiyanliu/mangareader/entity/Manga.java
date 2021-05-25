package com.baiyanliu.mangareader.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Manga {
    @GeneratedValue(generator = "mangaId") @Id private long id;
    @JsonIgnore @Version private long version;

    private String name;
    private Source source;
    private String sourceId;
    private Date lastRead;
    private int unread;
    private boolean read;
    @JsonIgnore
    @MapKey(name = "number")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<String, Chapter> chapters = new HashMap<>();

    public Manga(String name, Source source, String sourceId) {
        this.name = name;
        this.source = source;
        this.sourceId = sourceId;
    }

    public void updateUnread(int delta) {
        unread += delta;
        read = unread == 0;
    }
}
