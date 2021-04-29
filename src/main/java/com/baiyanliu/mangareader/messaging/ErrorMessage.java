package com.baiyanliu.mangareader.messaging;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorMessage extends Message {
    @GeneratedValue(generator = "errorMessageId") @Id private long id;

    private String error;

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
