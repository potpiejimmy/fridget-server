/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.wettercom;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author thorsten
 */
public class WetterDotComRenderer_Large extends WetterDotComRenderer {
    
    private final static int LINE_HEIGHT = 100;  // = pixel size of weather symbols
    private final static int HEADER_HEIGHT = 70; // header
    private final static int LINE_GAP = 0;  // gap between the two weather lines
    private final static int CREDIT_SPACE = 10;  // height of the space at the buttom used for displaying the wetter.com credit stuff

    // calculated values
    private final static int TOTAL_WIDTH = 300;
    private final static int VERTICAL_OFFSET = LINE_HEIGHT+LINE_GAP;
    
    public WetterDotComRenderer_Large(BufferedImage image) {
        super(image);
    }
    
    @Override
    protected void renderImage()
    {
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(image.getWidth() - TOTAL_WIDTH, 0, TOTAL_WIDTH, image.getHeight());
        graphics.setColor(Color.BLACK);
        graphics.fillRect(image.getWidth() - TOTAL_WIDTH, 0, 2, image.getHeight());
        
        final int HORIZONTAL_START = image.getWidth() - TOTAL_WIDTH;
        
        Font boldFont = graphics.getFont().deriveFont(Font.BOLD);
        graphics.setFont(boldFont.deriveFont(15.0f));
        graphics.setColor(Color.BLACK);
        graphics.drawString("Das Wetter in " + w.city, HORIZONTAL_START+10, 15);
        graphics.setFont(boldFont.deriveFont(10.0f));
        graphics.drawString("Heute", HORIZONTAL_START+10, 35);
        graphics.drawString("Morgen", HORIZONTAL_START+150, 35);

        graphics.setFont(boldFont.deriveFont(15.0f));
        graphics.setColor(Color.BLACK);
        graphics.drawString(w.today.tempMin+"°C /", HORIZONTAL_START+10, 65);
        graphics.setColor(Color.RED);
        graphics.drawString(w.today.tempMax+"°C",  HORIZONTAL_START+65, 65);
        graphics.setColor(Color.BLACK);
        graphics.drawString(w.tomorrow.tempMin+"°C /",  HORIZONTAL_START+150, 65);
        graphics.setColor(Color.RED);
        graphics.drawString(w.tomorrow.tempMax+"°C",  HORIZONTAL_START+150+55, 65);
        graphics.drawImage(getBmpFromWeatherState(w.today.morning), HORIZONTAL_START + 10,HEADER_HEIGHT,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.noon), HORIZONTAL_START + 10,HEADER_HEIGHT+VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.evening), HORIZONTAL_START + 10,HEADER_HEIGHT+2*VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.night), HORIZONTAL_START + 10,HEADER_HEIGHT+3*VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.tomorrow.morning), HORIZONTAL_START+150,HEADER_HEIGHT,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.tomorrow.noon), HORIZONTAL_START+150,HEADER_HEIGHT+VERTICAL_OFFSET,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.tomorrow.evening), HORIZONTAL_START+150,HEADER_HEIGHT+2*VERTICAL_OFFSET, LINE_HEIGHT, LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.tomorrow.night), HORIZONTAL_START+150,HEADER_HEIGHT+3*VERTICAL_OFFSET, LINE_HEIGHT, LINE_HEIGHT, null);
        graphics.setColor(Color.BLACK);
        graphics.setFont(boldFont.deriveFont(10.0f));
        graphics.drawString(w.creditString +" " + w.creditLink,  image.getWidth()-280, image.getHeight()-5);
    }
}
