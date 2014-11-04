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
public final class BitOutputStream
{ 
    private OutputStream output; 

    private int currentByte; 
    private int numBitsInCurrentByte; 

    // Creates a bit output stream based on the given byte output stream. 
    public BitOutputStream(OutputStream out) {
        if (out == null) 
                throw new NullPointerException("Argument is null"); 
        output = out; 
        currentByte = 0; 
        numBitsInCurrentByte = 0; 
    } 

    // Writes a bit to the stream.
    public void write(boolean b) throws IOException { 
        currentByte = currentByte << 1 | (b?1:0); 
        numBitsInCurrentByte++; 
        if (numBitsInCurrentByte == 8) { 
                output.write(currentByte); 
                numBitsInCurrentByte = 0; 
        } 
    } 

    // Closes this stream and the underlying OutputStream. If called when this bit stream is not at a byte boundary, 
    // then the minimum number of "0" bits (between 0 and 7 of them) are written as padding to reach the next byte boundary. 
    public void close() throws IOException {
        while (numBitsInCurrentByte != 0) 
                write(false); 
        output.close(); 
    } 
} 
