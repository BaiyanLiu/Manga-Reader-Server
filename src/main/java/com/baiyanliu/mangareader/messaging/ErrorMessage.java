package com.baiyanliu.mangareader.messaging;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorMessage extends LogMessage {
    @GeneratedValue(generator = "errorMessageId") @Id private long id;

    @Lob private String error;

    public ErrorMessage(String error) {
        super(new Date());
        this.error = error;
    }

    @Transient
    @Override
    protected String getDestination() {
        return "/error";
    }
}
