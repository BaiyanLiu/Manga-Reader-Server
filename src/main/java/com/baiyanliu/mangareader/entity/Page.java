package com.baiyanliu.mangareader.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Page {
    @GeneratedValue(generator = "pageId") @Id private long id;
    @JsonIgnore @Version private long version;

    @ManyToOne private Chapter chapter;
    private int number;
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private PageImage image;

    public Page(Chapter chapter, int number) {
        this.chapter = chapter;
        this.number = number;
    }

    public void setImage(BufferedImage image) throws IOException {
        this.image = new PageImage(image);
    }
}
