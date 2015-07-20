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
        public final HuffmanTree left, right; // subtrees
        public HuffmanTree(int freq, short sym) { frequency = freq; symbol = sym; left = null; right = null;}
        public HuffmanTree(HuffmanTree l, HuffmanTree r) { frequency = l.frequency + r.frequency; left = l; right = r; symbol = 0;}

        @Override
        public int compareTo(HuffmanTree o) {
            return frequency - o.frequency;
        }
    }
    
    public static class DictionaryEntry implements Comparable<DictionaryEntry>{
        public final int length;
        public final short symbol;
        public String code = null;
        public DictionaryEntry(int len, short sym) {length=len; symbol=sym;}

        @Override
        public int compareTo(DictionaryEntry o) {
            // sort order for canonical huffman: 
            // see http://en.wikipedia.org/wiki/Canonical_Huffman_code
            return length == o.length ? symbol - o.symbol : length - o.length;
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
    
    protected static void buildDictionaryTraverseTree(HuffmanTree tree, int prefixLength, List<DictionaryEntry> dictionary) {
        if (tree.left == null) {
            dictionary.add(new DictionaryEntry(prefixLength, tree.symbol));
        } else {
            // traverse left
            buildDictionaryTraverseTree(tree.left, prefixLength + 1, dictionary);
            // traverse right
            buildDictionaryTraverseTree(tree.right, prefixLength + 1, dictionary);
        }
    }
    
    public static List<DictionaryEntry> buildDictionary(HuffmanTree tree) {
        List<DictionaryEntry> dictionary = new ArrayList<>();
        buildDictionaryTraverseTree(tree, 0, dictionary);
        // now order by code length, symbol asc:
        dictionary.sort(null);
        // create canonical huffman code:
        int currentLen = 0;
        long prefix = 1;
        for (DictionaryEntry entry : dictionary) {
            prefix <<= entry.length-currentLen;
            entry.code = Long.toString(prefix, 2).substring(1);
            currentLen = entry.length;
            prefix++;
        }
        return dictionary;
    }
    
    public static Map<Short,String> buildDictionaryLookupMap(List<DictionaryEntry> dictionary) {
        Map<Short,String> map = new HashMap<>();
        dictionary.forEach(i->map.put(i.symbol, i.code));
        return map;
    }
    
    public static byte[] encodeDictionary(List<DictionaryEntry> dictionary) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(dictionary.size()*2);
        // write out number of symbols (=dictionary size)
        baos.write(dictionary.size()); // Warning: 256 results in encoded 0x00
        int currentLen = 0;
        int currentCount = 0;
        // write out number of symbols of length 1,2,3...n (n=maximum prefix length)
        for (DictionaryEntry entry : dictionary) {
            if (entry.length > currentLen) {
                if (currentCount>0) baos.write(currentCount);
                for (int i=0; i<entry.length-currentLen-1; i++) baos.write(0);
                currentLen = entry.length;
                currentCount = 0;
            }
            currentCount++;
        }
        if (currentCount>0) baos.write(currentCount);
        // write out list of symbols:
        for (DictionaryEntry entry : dictionary) baos.write(entry.symbol);
        try {baos.close();} catch (IOException e) {}
        return baos.toByteArray();
    }
    
    public static void printDictionary(List<DictionaryEntry> dictionary) {
        //System.out.println("SYMBOL\tHUFFMAN CODE");
        // print out character, frequency, and code for this leaf (which is just the prefix)
        dictionary.forEach(i->System.out.println(i.symbol + "\t" + i.code));
    }
    
    public static byte[] compress(byte[] data) throws IOException {
        //System.out.println("ORIG SIZE: " + data.length);

        int[] counter = new int[256];
        for (byte b : data) counter[b&0xff]++;

        HuffmanTree tree = buildHuffmanTree(counter);
        List<DictionaryEntry> dictionary = buildDictionary(tree);
//        printDictionary(dictionary);
        
        byte[] encodedDictionary = encodeDictionary(dictionary);
        
        // compress data
        Map<Short,String> map = buildDictionaryLookupMap(dictionary);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(encodedDictionary);
        BitOutputStream compressOut = new BitOutputStream(baos);
        for (byte b : data) {
            for (char bit : map.get((short)(b&0xff)).toCharArray()) compressOut.write(bit=='1');
        }
        compressOut.close();
        //System.out.println("DICTIONARY SIZE: " + encodedDictionary.length);
        //System.out.println("RESULTING SIZE: " + baos.size());
        return baos.toByteArray();
    }
}
