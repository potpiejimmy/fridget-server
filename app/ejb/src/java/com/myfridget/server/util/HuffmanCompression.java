/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 *
 * @author thorsten
 */
public class HuffmanCompression
{
    public static class HuffmanTree implements Comparable<HuffmanTree>
    {
        public final int frequency; // the frequency of this tree
        public final short symbol;
        public final int depth; // current depth at this node level
        public final HuffmanTree left, right; // subtrees
        public HuffmanTree(int freq, short sym) { frequency = freq; symbol = sym; left = null; right = null; depth = 0;}
        public HuffmanTree(HuffmanTree l, HuffmanTree r) { frequency = l.frequency + r.frequency; left = l; right = r; symbol = 0; depth = Math.max(l.depth, r.depth) + 1;}

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

            // put into new node and re-insert into queue, shorter depths go left
            trees.offer(a.depth <= b.depth ? new HuffmanTree(a, b) : new HuffmanTree(b, a));
        }
        return trees.poll();
    }
    
    public static int buildDictionary(HuffmanTree tree, StringBuffer prefix, Map<Short,String> dictionary, List<Short> lrIterator) {
        if (tree.left == null) {
            dictionary.put(tree.symbol, prefix.toString());
            lrIterator.add(tree.symbol);
            return tree.frequency * prefix.length();
        } else {
            // traverse left
            prefix.append('0');
            int sc1 = buildDictionary(tree.left, prefix, dictionary, lrIterator);
            prefix.deleteCharAt(prefix.length()-1);

            // traverse right
            prefix.append('1');
            int sc2 = buildDictionary(tree.right, prefix, dictionary, lrIterator);
            prefix.deleteCharAt(prefix.length()-1);
            
            return sc1 + sc2;
        }
    }
    
    public static byte[] encodeDictionary(Map<Short,String> dictionary, List<Short> lrIterator) {
        byte[] result = new byte[lrIterator.size()*2+1];
        result[0] = (byte)lrIterator.size();
        for (int i=0; i<lrIterator.size(); i++) {
            short symbol = lrIterator.get(i);
            result[1+(i*2)] = (byte)symbol;
            result[1+(i*2)+1] = (byte)dictionary.get(symbol).length();
        }
        return result;
    }
    
    public static void printDictionary(Map<Short, String> dictionary, List<Short> lrIterator) {
        System.out.println("SYMBOL\tHUFFMAN CODE");
        for (short symbol : lrIterator) {
            // print out character, frequency, and code for this leaf (which is just the prefix)
            System.out.println(symbol + "\t" + dictionary.get(symbol));
        }
    }
    
    public static byte[] compress(byte[] data) throws IOException {
        System.out.println("ORIG SIZE: " + data.length);
        int[] counter = new int[256];
        for (byte b : data) counter[b&0xff]++;
        HuffmanTree tree = buildHuffmanTree(counter);

        Map<Short, String> dictionary = new HashMap<>();
        List<Short> lrIterator = new ArrayList<>();
        int expectedResultSize = buildDictionary(tree, new StringBuffer(), dictionary, lrIterator);
        
        printDictionary(dictionary, lrIterator);
        byte[] encodedDictionary = encodeDictionary(dictionary, lrIterator);
        
        // compress data
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int)Math.round(Math.ceil(((double)expectedResultSize)/8)));
        baos.write(encodedDictionary);
        BitOutputStream compressOut = new BitOutputStream(baos);
        for (byte b : data) {
            for (char bit : dictionary.get((short)(b&0xff)).toCharArray()) compressOut.write(bit=='1');
        }
        compressOut.close();
        System.out.println("DICTIONARY SIZE: " + encodedDictionary.length);
        System.out.println("RESULTING SIZE: " + baos.size());
        return baos.toByteArray();
    }
}
