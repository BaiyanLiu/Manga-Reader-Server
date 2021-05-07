package com.baiyanliu.mangareader.messaging;

import lombok.*;

import javax.persistence.MappedSuperclass;
import java.util.Date;

@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class LogMessage extends Message {
    private Date timestamp;
}
