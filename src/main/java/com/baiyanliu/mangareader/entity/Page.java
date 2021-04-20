package com.baiyanliu.mangareader.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.imageio.ImageIO;
import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Data
@Entity
@EqualsAndHashCode(exclude = "data")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page {
    @GeneratedValue(generator = "pageId") @Id private long id;
    @JsonIgnore @Version private long version;

    private int number;
    @Lob private byte[] data;

    public Page(int number) {
        this.number = number;
    }

    public void setData(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        data = out.toByteArray();
    }
}
