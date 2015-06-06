/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.google;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author thorsten
 */
public class BaseRenderer {
    public Dimension dimension = null;
    
    protected Graphics2D graphics = null;
    protected int lineNumber = 0;
    
    private DateFormat formatterDate = null;
    private DateFormat formatterDayOfWeek = null;
    
    public BaseRenderer(Dimension dimension) {
        this.dimension = dimension;
        this.lineNumber = 0;
        
        this.formatterDate = new SimpleDateFormat("MMMM dd");
        this.formatterDayOfWeek = new SimpleDateFormat("EEEE");
     }
    
    protected BufferedImage createImage() {
        BufferedImage result = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        
        this.graphics = (Graphics2D)result.getGraphics();
        //m_Graphics.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.None;
        //m_Graphics.TextRenderingHint = System.Drawing.Text.TextRenderingHint.SingleBitPerPixelGridFit;
        
        return result;
    }
    
    protected void drawHeader()
    {
        drawHeader(null);
    }
    
    protected void drawHeader(String headLine)
    {
        Calendar now = Calendar.getInstance();
        int currentLine = this.lineNumber;
        drawColoredLines(56, Color.RED);
        drawColoredLines(1, Color.WHITE);
        
        graphics.setColor(Color.WHITE);
        Font font = graphics.getFont();
        font = font.deriveFont(Font.BOLD);
        
        graphics.setFont(font.deriveFont(14f));
        graphics.drawString(formatterDayOfWeek.format(now.getTime()), 10, currentLine + 16);

        graphics.setFont(font.deriveFont(28f));
        graphics.drawString(headLine!=null ? headLine : formatterDate.format(now.getTime()), 10, currentLine + 44);

            graphics.setFont(font.deriveFont(14f));
        if (headLine == null)
            graphics.drawString("Kalenderwoche " + now.get(Calendar.WEEK_OF_YEAR), dimension.width - 150, currentLine + 16);
        else
            graphics.drawString("Tasks", dimension.width - 64, currentLine + 16);
    }

    protected void drawColoredLines(int n, Color c)
    {
        if (this.lineNumber == dimension.height) return;
        this.graphics.setColor(c);
        for (int i=0; i<n; i++)
        {
            this.graphics.drawLine(0, this.lineNumber, dimension.width-1, this.lineNumber);
            this.lineNumber++;
            if (this.lineNumber == dimension.height) return;
        }
    }

    protected void fillUpWithWhite()
    {
        while (this.lineNumber<dimension.height) drawColoredLines(1, Color.WHITE);
    }
}
