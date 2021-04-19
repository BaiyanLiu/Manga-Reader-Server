package com.baiyanliu.mangareader.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Map;
import java.util.TreeMap;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Manga {
    @GeneratedValue(generator = "mangaId") @Id private long id;
    @JsonIgnore @Version private long version;

    private String name;
    private Source source;
    private String sourceId;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @MapKey(name = "number")
    private Map<String, Chapter> chapters = new TreeMap<>();

    public Manga(String name, Source source, String sourceId) {
        this.name = name;
        this.source = source;
        this.sourceId = sourceId;
    }
}
