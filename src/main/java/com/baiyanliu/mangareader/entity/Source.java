package com.baiyanliu.mangareader.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public enum Source {
    MANGA_SEE("MangaSee"),
    MANGA_DEX("MangaDex");

    private static final Map<String, Source> nameToSource = new HashMap<>();

    private final String name;

    static {
        for (Source source : values()) {
            nameToSource.put(source.name, source);
        }
    }

    @JsonCreator
    public static Source fromString(String value) {
        return nameToSource.get(value);
    }

    @JsonValue
    @Override
    public String toString() {
        return name;
    }
}
