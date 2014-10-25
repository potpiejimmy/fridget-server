/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

/**
 *
 * @author thorsten
 */
public class EPDUtils {
    
    public static byte[] makeSpectra3Color(BufferedImage img) {
        Graphics gr = img.getGraphics();
        int width = img.getWidth();
        int height = img.getHeight();
        byte[] result = new byte[width/4 * height];
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb&0xff0000)>>16;
                int g = (rgb&0xff00)>>8;
                int b = (rgb&0xff);
                byte encodedPixel;
                if (r>g+50 && r>b+50) {
                    gr.setColor(Color.RED);
                    encodedPixel = (byte)0b01;
                } else if (r+g+b<384) {
                    gr.setColor(Color.BLACK);
                    encodedPixel = (byte)0b00;
                } else {
                    gr.setColor(Color.WHITE);
                    encodedPixel = (byte)0b11;
                }
                gr.drawLine(x, y, x, y);
                int pos = y*width + x;
                int bitOffset = (3 - pos % 4) * 2; // 6, 4, 2, 0
                result[pos/4] |= (encodedPixel<<bitOffset);
            }
        }
        return result;
    }
}
