/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.wettercom;

import com.myfridget.server.util.Utils;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.ImageIcon;

/**
 *
 * @author thorsten
 */
public class WetterDotComRenderer {
    
    private final static int LINE_HEIGHT = 70;  // = pixel size of weather symbols
    private final static int LINE_GAP = 10;  // gap between the two weather lines
    private final static int CREDIT_SPACE = 10;  // height of the space at the buttom used for displaying the wetter.com credit stuff


    // calculated values
    private int totalHeight = 0;
    private final static int VERTICAL_OFFSET = LINE_HEIGHT+LINE_GAP;
    
    private BufferedImage weatherSymbols = null;
    private WetterDotCom w = new WetterDotCom();
    
    /**
     * Renders the weather on top of the given image.
     * @param image an image buffer
     * @param location a location search string
     * @return update image buffer
     */
    public BufferedImage renderWeather(BufferedImage image, String location) throws IOException {
        
        if (isSingleDayOnly(image))
            this.totalHeight = LINE_HEIGHT+CREDIT_SPACE;
        else
            this.totalHeight = 2*LINE_HEIGHT+LINE_GAP+CREDIT_SPACE;
        
        byte[] symbolsPicData = Utils.readAll(getClass().getResourceAsStream("/com/myfridget/server/util/wettercom/Wetter1.png"));
        Image symbolsPic = new ImageIcon(symbolsPicData).getImage();
        this.weatherSymbols = new BufferedImage(symbolsPic.getWidth(null), symbolsPic.getHeight(null), BufferedImage.TYPE_INT_RGB);
        this.weatherSymbols.getGraphics().setColor(Color.WHITE);
        this.weatherSymbols.getGraphics().fillRect(0, 0, weatherSymbols.getWidth(), weatherSymbols.getHeight());
        this.weatherSymbols.getGraphics().drawImage(symbolsPic, 0, 0, null);
        
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, image.getHeight() - totalHeight, image.getWidth(), totalHeight);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, image.getHeight() - totalHeight - 2, image.getWidth(), 2);
        
        w.updateData(location);
        
        renderImage(image, graphics);
        
        return image;
    }
    
    protected static boolean isSingleDayOnly(BufferedImage img) {
        return img.getHeight() < 400;
    }
    
    private void renderImage(BufferedImage image, Graphics2D graphics)
    {
        final int HORIZONTAL_OFFSET = image.getWidth() / 5;
        final int VERTICAL_START = image.getHeight() - totalHeight;
        
        Font boldFont = graphics.getFont().deriveFont(Font.BOLD);
        graphics.rotate(-java.lang.Math.PI/2);
        graphics.setFont(boldFont.deriveFont(10.0f));
        graphics.setColor(Color.BLACK);
        graphics.drawString(w.city, -image.getHeight() + CREDIT_SPACE + 3, 10);
        graphics.rotate(java.lang.Math.PI/2);

        graphics.setFont(boldFont.deriveFont((float)LINE_HEIGHT/3));
        graphics.setColor(Color.BLACK);
        graphics.drawString(w.today.tempMin+"°C", HORIZONTAL_OFFSET/4, VERTICAL_START + LINE_HEIGHT/3 + 3);
        graphics.setColor(Color.RED);
        graphics.drawString(w.today.tempMax+"°C",  HORIZONTAL_OFFSET/4, VERTICAL_START + LINE_HEIGHT/3 + LINE_HEIGHT/2 + 3);
        if (!isSingleDayOnly(image)) {
            graphics.setColor(Color.BLACK);
            graphics.drawString(w.tomorrow.tempMin+"°C",  HORIZONTAL_OFFSET/4, VERTICAL_START + LINE_HEIGHT/3 + LINE_HEIGHT+LINE_GAP + 3);
            graphics.setColor(Color.RED);
            graphics.drawString(w.tomorrow.tempMax+"°C",  HORIZONTAL_OFFSET/4, VERTICAL_START + LINE_HEIGHT/3 + LINE_HEIGHT+LINE_GAP+LINE_HEIGHT/2 + 3);
        }
        graphics.drawImage(getBmpFromWeatherState(w.today.morning), HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.noon), 2*HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.evening), 3*HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.night), 4*HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        if (!isSingleDayOnly(image)) {
            graphics.drawImage(getBmpFromWeatherState(w.tomorrow.morning), HORIZONTAL_OFFSET,VERTICAL_START+VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
            graphics.drawImage(getBmpFromWeatherState(w.tomorrow.noon), 2*HORIZONTAL_OFFSET,VERTICAL_START+VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
            graphics.drawImage(getBmpFromWeatherState(w.tomorrow.evening), 3*HORIZONTAL_OFFSET,VERTICAL_START+VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
            graphics.drawImage(getBmpFromWeatherState(w.tomorrow.night), 4*HORIZONTAL_OFFSET,VERTICAL_START+VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
        }
        graphics.setColor(Color.BLACK);
        graphics.setFont(boldFont.deriveFont(10.0f));
        graphics.drawString(w.creditString +" " + w.creditLink,  image.getWidth()-280, image.getHeight()-3);
    }

    private Image getBmpFromWeatherState(WetterDotCom.WeatherState state)
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
        WetterDotComRenderer renderer = new WetterDotComRenderer();
        BufferedImage output = new BufferedImage(400, 500, BufferedImage.TYPE_INT_RGB);
        FileOutputStream fos = new FileOutputStream("/Users/thorsten/weatheroutput.png");
        fos.write(Utils.encodeImage(renderer.renderWeather(output, "60598"), "png"));
        fos.close();
    }
}
