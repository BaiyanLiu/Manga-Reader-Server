package com.baiyanliu.mangareader.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chapter {
    @GeneratedValue(generator = "chapterId") @Id private long id;
    @JsonIgnore @Version private long version;

    private String number;
    private String name;
    private int lastPage;
    private boolean downloaded;
    private boolean read;
    @JsonIgnore
    @MapKey(name = "number")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Map<Integer, Page> pages = new HashMap<>();

    public Chapter(String number, String name) {
        this.number = number;
        this.name = name;
    }
}
