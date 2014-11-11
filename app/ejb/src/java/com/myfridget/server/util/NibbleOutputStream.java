/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author thorsten
 */
public final class NibbleOutputStream
{ 
    private OutputStream output; 

    private int currentByte; 
    private boolean flip; 

    // Creates a bit output stream based on the given byte output stream. 
    public NibbleOutputStream(OutputStream out) {
        if (out == null) 
                throw new NullPointerException("Argument is null"); 
        output = out; 
        currentByte = 0; 
        flip = false; // upper nibble 
    } 

    // Writes a nibble to the stream.
    public void writeNibble(byte nibble) throws IOException { 
        currentByte = currentByte << 4 | nibble;
        if (!(flip ^= true)) output.write(currentByte); 
    } 

    public void close() throws IOException {
        if (flip) writeNibble((byte)0);
        output.close(); 
    } 
} 
