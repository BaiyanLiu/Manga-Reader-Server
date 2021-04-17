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
public class Chapter implements Comparable<Chapter> {
    @GeneratedValue(generator = "chapterId") @Id private long id;
    @JsonIgnore @Version private long version;

    private int number;
    private String name;
    private boolean downloaded;
    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL)
    @MapKey(name = "number")
    private Map<Integer, Page> pages;

    public Chapter(int number, String name) {
        this.number = number;
        this.name = name;
        this.pages = new HashMap<>();
    }

    @Override
    public int compareTo(Chapter o) {
        return Integer.compare(number, o.number);
    }
}
