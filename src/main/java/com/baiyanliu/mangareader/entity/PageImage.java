package com.baiyanliu.mangareader.entity;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.imageio.ImageIO;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageImage {
    @GeneratedValue(generator = "pageImageId") @Id private long id;

    @Lob
    @Type(type = "org.hibernate.type.ImageType")
    private byte[] data;

    public PageImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        data = out.toByteArray();
    }
}
