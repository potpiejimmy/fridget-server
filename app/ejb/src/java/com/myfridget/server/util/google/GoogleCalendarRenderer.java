/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.google;

import com.myfridget.server.util.google.GoogleCalendar.CalendarItem;
import com.myfridget.server.util.Utils;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author thorsten
 */
public class GoogleCalendarRenderer extends BaseRenderer {
    
    private GoogleCalendar calendar = null;
    
    private DateFormat formatterDateline = null;
    private DateFormat formatterTime = null;
    
    public GoogleCalendarRenderer(Dimension dimension) {
        super(dimension);
        
        this.formatterDateline = new SimpleDateFormat("EEEE, MMMM dd");
        this.formatterTime = new SimpleDateFormat("HH:mm");
    }
    
    /**
     * Renders the Google Calendar as an image.
     * @param userId a system user ID
     * @return an image
     */
    public BufferedImage renderCalendar(String userId) throws IOException {

        BufferedImage result = createImage();

        drawHeader();
        
        this.calendar = new GoogleCalendar(userId);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        Calendar iterator = Calendar.getInstance();
        for (CalendarItem i : this.calendar.nextCalendarItems())
        {
            if (i.start.getTime() >= today.getTimeInMillis())
            {
                Calendar iCal = Calendar.getInstance();
                iCal.setTime(i.start);
                while (iCal.get(Calendar.DAY_OF_YEAR) != iterator.get(Calendar.DAY_OF_YEAR))
                {
                    iterator.add(Calendar.DAY_OF_YEAR, 1);
                    drawDateLine(iterator);
                }
                drawItem(i);				
            }
        }
        fillUpWithWhite();
        
        return result;
    }
    
    private void drawDateLine(Calendar d)
    {
        int currentLine = this.lineNumber;
        if (d.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
        {
            drawColoredLines(2, Color.WHITE);
            drawColoredLines(22, Color.BLACK);
            graphics.setColor(Color.WHITE);
            graphics.setFont(graphics.getFont().deriveFont(12f));
            graphics.drawString("Kalenderwoche " + d.get(Calendar.WEEK_OF_YEAR), 12, currentLine+16);
        }
        currentLine = this.lineNumber;
        drawColoredLines(24, Color.RED);
        graphics.setColor(Color.WHITE);
        graphics.setFont(graphics.getFont().deriveFont(16f));
        graphics.drawString(formatterDateline.format(d.getTime()), 10, currentLine+16);
        drawColoredLines(1, Color.WHITE);		
    }
		
    private void drawItem(CalendarItem i)
    {
        int currentLine = this.lineNumber;
        if (i.wholeDay)
            drawColoredLines(32, Color.WHITE);
        else
            drawColoredLines(42, Color.WHITE);
        if (i.description != null && i.description.length() > 0) drawColoredLines(18, Color.WHITE);
        if (i.location != null && i.location.length() > 0) drawColoredLines(18, Color.WHITE);
        drawColoredLines(2, Color.RED);
        drawColoredLines(2, Color.WHITE);
        drawColoredLines(3, Color.BLACK);
        if (i.wholeDay)
        {
            graphics.setColor(Color.RED);
            graphics.setFont(graphics.getFont().deriveFont(20f));
            graphics.drawString(i.title, 10, currentLine+20);				
            currentLine += 25;		
        }
        else
        {
            graphics.setColor(Color.BLACK);
            graphics.setFont(graphics.getFont().deriveFont(25f));
            graphics.drawString(formatTimes(i), 10, currentLine+26);
            graphics.drawString(i.title, 180, currentLine+26);
            currentLine += 35;				
        }
        graphics.setFont(graphics.getFont().deriveFont(14f));
        if (i.description != null && i.description.length() > 0)
        {
            graphics.setColor(Color.BLACK);
            graphics.drawString(i.description, 10, currentLine+14);
            currentLine += 18;
        }
        if (i.location != null && i.location.length() > 0)
        {
            graphics.setColor(Color.RED);
            graphics.drawString(i.location, 10, currentLine+14);
            currentLine += 18;
        }
    }
		
    private String formatTimes(CalendarItem i)
    {
        return formatterTime.format(i.start) + "-" + formatterTime.format(i.end);
    }

    public static void main(String[] args) throws Exception 
    {
        GoogleCalendarRenderer renderer = new GoogleCalendarRenderer(new Dimension(480, 800));
        FileOutputStream fos = new FileOutputStream("/Users/thorsten/calendar.png");
        fos.write(Utils.encodeImage(renderer.renderCalendar("thorsten@potpiejimmy.de"), "png"));
        fos.close();
    }
}
