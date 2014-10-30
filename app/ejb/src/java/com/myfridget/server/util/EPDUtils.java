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
                int pos = y*width + x;
                int bitOffset = 7 - (pos % 8); // 7,6,5,4,3,2,1,0
                if (r>g+50 && r>b+50) {
                    gr.setColor(Color.RED);
                    result[pos/8] |= (1<<bitOffset); // red map
                } else if (r+g+b<384) {
                    gr.setColor(Color.BLACK);
                    result[pos/8] |= (1<<bitOffset); // put black also in red map
                    result[(width*height+pos)/8] |= (1<<bitOffset); // black map
                } else {
                    gr.setColor(Color.WHITE);
                }
                gr.drawLine(x, y, x, y);
            }
        }
        return result;
    }
}
