package com.baiyanliu.mangareader.messaging;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ErrorMessage extends Message {
    @Getter private final String error;

    @Override
    protected String getDestination() {
        return "/error";
    }
}
