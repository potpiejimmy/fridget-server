/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import com.myfridget.server.util.GoogleCalendar.CalendarItem;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
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
public class GoogleCalendarRenderer {
    
    public final static int WIDTH = 480;
    public final static int HEIGHT = 800;
    
    private GoogleCalendar calendar = null;
    private Graphics2D graphics = null;
    private int lineNumber = 0;
    
    private DateFormat formatterDate = null;
    private DateFormat formatterDayOfWeek = null;
    private DateFormat formatterDateline = null;
    private DateFormat formatterTime = null;
    
    public GoogleCalendarRenderer() {
        this.calendar = null;
        this.lineNumber = 0;
        
        this.formatterDate = new SimpleDateFormat("MMMM dd");
        this.formatterDayOfWeek = new SimpleDateFormat("EEEE");
        this.formatterDateline = new SimpleDateFormat("EEEE, MMMM dd");
        this.formatterTime = new SimpleDateFormat("HH:mm");
    }
    
    /**
     * Renders the Google Calendar as an image.
     * @param userId a system user ID
     * @return an image
     */
    public BufferedImage renderCalendar(String userId) throws IOException {

        BufferedImage result = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        
        this.graphics = (Graphics2D)result.getGraphics();

        //m_Graphics.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.None;
        //m_Graphics.TextRenderingHint = System.Drawing.Text.TextRenderingHint.SingleBitPerPixelGridFit;

        this.calendar = new GoogleCalendar(userId);

        drawHeader();
        
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
    
    private void drawHeader()
    {
        Calendar now = Calendar.getInstance();
        int currentLine = this.lineNumber;
        drawColoredLines(56, Color.RED);
        drawColoredLines(1, Color.WHITE);			// needed when no item at current date
//			drawColoredLines(2, Color.Red);
//			drawColoredLines(2, Color.White);
//			drawColoredLines(4, Color.Black);
        graphics.setColor(Color.WHITE);
        Font font = graphics.getFont();
        font = font.deriveFont(Font.BOLD);
        
        graphics.setFont(font.deriveFont(14f));
        graphics.drawString(formatterDayOfWeek.format(now.getTime()), 10, currentLine + 16);

        graphics.setFont(font.deriveFont(28f));
        graphics.drawString(formatterDate.format(now.getTime()), 10, currentLine + 44);

        graphics.setFont(font.deriveFont(14f));
        graphics.drawString("Kalenderwoche " + now.get(Calendar.WEEK_OF_YEAR), 335, currentLine + 16);
    }

    private void drawColoredLines(int n, Color c)
    {
        if (this.lineNumber == 800) return;
        this.graphics.setColor(c);
        for (int i=0; i<n; i++)
        {
            this.graphics.drawLine(0, this.lineNumber, WIDTH-1, this.lineNumber);
            this.lineNumber++;
            if (this.lineNumber == 800) return;
        }
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

    private void fillUpWithWhite()
    {
        while (this.lineNumber<800) drawColoredLines(1, Color.WHITE);
    }
    
    public static void main(String[] args) throws Exception 
    {
        GoogleCalendarRenderer renderer = new GoogleCalendarRenderer();
        FileOutputStream fos = new FileOutputStream("/Users/thorsten/calendar.png");
        fos.write(Utils.encodeImage(renderer.renderCalendar("thorsten@potpiejimmy.de"), "png"));
    }
}
