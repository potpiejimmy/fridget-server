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
public class WetterDotComRenderer_Small extends WetterDotComRenderer {
    
    private final static int LINE_HEIGHT = 70;  // = pixel size of weather symbols
    private final static int LINE_GAP = 10;  // gap between the two weather lines
    private final static int CREDIT_SPACE = 10;  // height of the space at the buttom used for displaying the wetter.com credit stuff

    // calculated values
    private final static int TOTAL_HEIGHT = LINE_HEIGHT+CREDIT_SPACE;
    private final static int VERTICAL_OFFSET = LINE_HEIGHT+LINE_GAP;

    public WetterDotComRenderer_Small(BufferedImage image) {
        super(image);
    }
    
    @Override
    protected void renderImage()
    {
        Graphics2D graphics = (Graphics2D)image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, image.getHeight() - TOTAL_HEIGHT, image.getWidth(), TOTAL_HEIGHT);
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0, image.getHeight() - TOTAL_HEIGHT - 2, image.getWidth(), 2);
        
        final int HORIZONTAL_OFFSET = image.getWidth() / 5;
        final int VERTICAL_START = image.getHeight() - TOTAL_HEIGHT;
        
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
        graphics.drawImage(getBmpFromWeatherState(w.today.morning), HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.noon), 2*HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.evening), 3*HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.drawImage(getBmpFromWeatherState(w.today.night), 4*HORIZONTAL_OFFSET,VERTICAL_START,LINE_HEIGHT,LINE_HEIGHT, null);
        graphics.setColor(Color.BLACK);
        graphics.setFont(boldFont.deriveFont(10.0f));
        graphics.drawString(w.creditString +" " + w.creditLink,  image.getWidth()-280, image.getHeight()-3);
    }
}
