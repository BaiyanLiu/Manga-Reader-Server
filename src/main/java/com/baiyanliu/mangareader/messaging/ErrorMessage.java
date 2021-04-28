package com.baiyanliu.mangareader.messaging;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ErrorMessage extends Message {
    @GeneratedValue(generator = "errorMessageId") @Id private long id;
    @JsonIgnore @Version private long version;

    private String error;

    public ErrorMessage(String error) {
        this.error = error;
    }

    @Transient
    @Override
    protected String getDestination() {
        return "/error";
    }
}
