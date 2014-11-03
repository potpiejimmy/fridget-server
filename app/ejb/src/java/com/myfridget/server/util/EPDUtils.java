/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.PriorityQueue;

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
    
    public static byte[] compressRLE(byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(data.length/8);
        baos.write((byte)1); // 1 = compressed
        int currentBit = -1;
        int rleCount = 0;
        for (int i=0; i<data.length*8; i++) {
            int bit = (data[i/8] & (1<<(7-(i%8))))>0 ? 1 : 0;
            if (currentBit != bit) {
                // bit changed, encode:
                encodeRLE(baos, currentBit, rleCount); // first loop iteration has rleCount 0
                currentBit = bit;
                rleCount = 0;
            }
            rleCount++;
        }
        try {baos.close();} catch (IOException e) {/*doesn't matter*/}
        byte[] result = baos.toByteArray();
        if (result.length <= data.length) return result;
        // compressed size larger than input data + 1:
        byte[] uncompressed = new byte[data.length+1];
        uncompressed[0] = (byte)0; // 0 = uncompressed
        System.arraycopy(data, 0, uncompressed, 1, data.length);
        return uncompressed;
    }
    
    protected static void encodeRLE(ByteArrayOutputStream baos, int bit, int rleCount) {
        if (rleCount == 0) return;
        rleCount--; // encode count - 1 instead of count
        byte enc = (byte)(bit<<7); // MSB encodes the bit
        enc |= rleCount & 0x3F; // lowest 6 bits of length
        rleCount >>= 6;
        if (rleCount > 0) enc |= 0x40; // second MSB encodes presence of additional length byte
        baos.write(enc);
        // from here, encode 7 bits with each MSB signaling next byte
        while (rleCount > 0) {
            enc = (byte)(rleCount & 0x7f);
            rleCount >>= 7;
            if (rleCount > 0) enc |= 0x80;
            baos.write(enc);
        }
    }
    
    public static class HuffmanTree implements Comparable<HuffmanTree>
    {
        public final int frequency; // the frequency of this tree
        public final short symbol;
        public final HuffmanTree left, right; // subtrees
        public HuffmanTree(int freq, short sym) { frequency = freq; symbol = sym; left = null; right = null;}
        public HuffmanTree(HuffmanTree l, HuffmanTree r) { frequency = l.frequency + r.frequency; left = l; right = r; symbol = 0;}

        @Override
        public int compareTo(HuffmanTree o) {
            return frequency - o.frequency == 0 ? symbol - o.symbol : frequency - o.frequency;
        }
    }
    
    public static HuffmanTree buildHuffmanTree(int[] freqs) {
        PriorityQueue<HuffmanTree> trees = new PriorityQueue<>();
        for (int i = 0; i < freqs.length; i++)
            if (freqs[i] > 0)
                trees.offer(new HuffmanTree(freqs[i], (short)i));

        // loop until there is only one tree left
        while (trees.size() > 1) {
            // two trees with least frequency
            HuffmanTree a = trees.poll();
            HuffmanTree b = trees.poll();

            // put into new node and re-insert into queue
            trees.offer(new HuffmanTree(a, b));
        }
        return trees.poll();
    }
    
    public static int printCodes(HuffmanTree tree, StringBuffer prefix) {
        if (tree.left == null) {
            // print out character, frequency, and code for this leaf (which is just the prefix)
            System.out.println(tree.symbol + "\t" + tree.frequency + "\t" + prefix);
            return tree.frequency * prefix.length();
        } else {
            // traverse left
            prefix.append('0');
            int sc1 = printCodes(tree.left, prefix);
            prefix.deleteCharAt(prefix.length()-1);

            // traverse right
            prefix.append('1');
            int sc2 = printCodes(tree.right, prefix);
            prefix.deleteCharAt(prefix.length()-1);
            
            return sc1 + sc2;
        }
    }
    
    public static byte[] compressHuffman(byte[] data) {
        int[] counter = new int[256];
        for (byte b : data) counter[b&0xff]++;
        HuffmanTree tree = buildHuffmanTree(counter);

        System.out.println("SYMBOL\tFREQ\tHUFFMAN CODE");
        int bits = printCodes(tree, new StringBuffer());
        System.out.println("RESULTING SIZE: " + (bits/8));
        return data;
    }
}
