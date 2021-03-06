/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author thorsten
 */
public class EPDUtils {
    
    public final static int SPECTRA_DISPLAY_TYPE_441 = 0;
    public final static int SPECTRA_DISPLAY_TYPE_74 = 1;

    public final static int[] SPECTRA_DISPLAY_DEFAULT_TYPES = new int[] {
        SPECTRA_DISPLAY_TYPE_441,
        SPECTRA_DISPLAY_TYPE_74
    };
    
    public static Dimension dimensionForDisplayType(int displayType) {
        switch (displayType) {
            case SPECTRA_DISPLAY_TYPE_441:
                return new Dimension(400,300);
            case SPECTRA_DISPLAY_TYPE_74:
                return new Dimension(480,800);
        }
        return null;
    }
    
    public final static Color SPECTRA_RED = new Color(0xC0, 0, 0);
    
    public static BufferedImage getResizedImageForDisplay(byte[] imgData, int displayType) {
        Dimension dim = dimensionForDisplayType(displayType);
        return Utils.getScaledBufferedImage(imgData, dim.width, dim.height, true);
    }
    
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
                    gr.setColor(SPECTRA_RED);
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
        gr.dispose();
        return result;
    }
    
    public static byte[] compressRLE(byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length/8);
        NibbleOutputStream nibOut = new NibbleOutputStream(baos);
        nibOut.writeNibble((byte)1); // 1 = compressed
        int currentBit = -1;
        int rleCount = 0;
        for (int i=0; i<data.length*8; i++) {
            int bit = (data[i/8] & (1<<(7-(i%8))))>0 ? 1 : 0;
            if (currentBit != bit) {
                // bit changed, encode:
                encodeRLE(nibOut, currentBit, rleCount); // first loop iteration has rleCount 0
                currentBit = bit;
                rleCount = 0;
            }
            rleCount++;
        }
        encodeRLE(nibOut, currentBit, rleCount);
        try {nibOut.close();} catch (IOException e) {/*doesn't matter*/}
        byte[] result = baos.toByteArray();
        if (result.length <= data.length) return result;
        // compressed size larger than input data + 1:
        byte[] uncompressed = new byte[data.length+1];
        uncompressed[0] = (byte)0; // 0 = uncompressed
        System.arraycopy(data, 0, uncompressed, 1, data.length);
        return uncompressed;
    }
    
    protected static void encodeRLE(NibbleOutputStream out, int bit, int rleCount) throws IOException {
        if (rleCount == 0) return;
        rleCount--; // encode count - 1 instead of count
        do {
            byte enc = (byte)(rleCount & 0x07);
            enc |= (bit << 3);
            out.writeNibble(enc);
            rleCount >>= 3;
        } while (rleCount > 0);
    }
}
