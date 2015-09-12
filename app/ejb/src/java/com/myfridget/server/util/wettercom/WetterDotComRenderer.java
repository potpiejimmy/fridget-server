/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.wettercom;

import com.myfridget.server.util.Utils;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.ImageIcon;

/**
 *
 * @author thorsten
 */
public abstract class WetterDotComRenderer {
    
    protected BufferedImage weatherSymbols = null;
    protected BufferedImage image = null;
    protected WetterDotCom w = new WetterDotCom();

    /**
     * Create a new renderer for the given image.
     * Renders the weather on top of the given image.
     * @param image pre-rendered image
     */
    protected WetterDotComRenderer(BufferedImage image) {
        this.image = image;
    }
    
    /**
     * Render weather panel for the given location.
     * @param location a location search string
     * @return update image buffer
     */
    public BufferedImage renderWeather(String location) throws IOException {
        
        byte[] symbolsPicData = Utils.readAll(getClass().getResourceAsStream("/com/myfridget/server/util/wettercom/Wetter1.png"));
        Image symbolsPic = new ImageIcon(symbolsPicData).getImage();
        this.weatherSymbols = new BufferedImage(symbolsPic.getWidth(null), symbolsPic.getHeight(null), BufferedImage.TYPE_INT_RGB);
        this.weatherSymbols.getGraphics().setColor(Color.WHITE);
        this.weatherSymbols.getGraphics().fillRect(0, 0, weatherSymbols.getWidth(), weatherSymbols.getHeight());
        this.weatherSymbols.getGraphics().drawImage(symbolsPic, 0, 0, null);
        
        w.updateData(location);
        
        renderImage();
        
        return image;
    }
    
    /**
     * Create renderer instance for the given screen image
     * @param img a pre-rendered screen
     * @return a renderer for the given screen
     */
    public static WetterDotComRenderer createInstanceForImage(BufferedImage img) {
        return img.getHeight() < 400 ?
                new WetterDotComRenderer_Small(img) :
                new WetterDotComRenderer_Large(img);
    }
    
    protected abstract void renderImage();

    protected Image getBmpFromWeatherState(WetterDotCom.WeatherState state)
    {
        int index = 6;
        switch (state)
        {
            case bedeckt:
                    index = 3; break;
            case gewitter:
                    index = 4; break;
            case leicht_bewölkt:
                    index = 1; break;
            case regen:
                    index = 5; break;
            case sonnig:
                    index = 0; break;
            case wolkig:
                    index = 2; break;
            case klar:
                    index = 7; break;
            case bewölkt_nachts:
                    index = 8; break;
            case UNKNOWN:
                    index = 6; break;
        }
        
        return weatherSymbols.getSubimage(80*index,0,80,80);
    }

    public static void main(String[] args) throws Exception 
    {
        BufferedImage output = new BufferedImage(800, 480, BufferedImage.TYPE_INT_RGB);
        WetterDotComRenderer renderer = WetterDotComRenderer.createInstanceForImage(output);
        FileOutputStream fos = new FileOutputStream("/Users/thorsten/weatheroutput.png");
        fos.write(Utils.encodeImage(renderer.renderWeather("60598"), "png"));
        fos.close();
    }
}
