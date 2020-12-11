package com.sy.entity.message;

/**
 * Created by pc on 2017/4/12.
 */
import java.awt.image.BufferedImage;

public class CodeImage {
    public String code;
    public BufferedImage image;

    public CodeImage(String code, BufferedImage image) {
        this.code = code;
        this.image = image;
    }
}