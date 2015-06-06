/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.vo;

/**
 *
 * @author thorsten
 */
public class AdMediumPreviewImageData {
    
    public byte[] data;
    public short gentype;
    public String geninfo;
    
    public AdMediumPreviewImageData(byte[] data, short gentype) {
        this(data, gentype, null);
    }
    
    public AdMediumPreviewImageData(byte[] data, short gentype, String geninfo) {
        this.data = data;
        this.gentype = gentype;
        this.geninfo = geninfo;
    }
}
