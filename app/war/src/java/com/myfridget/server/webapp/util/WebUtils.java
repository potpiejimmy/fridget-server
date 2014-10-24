/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.webapp.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Iterator;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.servlet.http.HttpServletRequest;
import javax.swing.ImageIcon;

/**
 * Contains static utility methods.
 */
public class WebUtils
{
    public static int getCurrentUserId(HttpServletRequest hsr)
    {
        Principal p = hsr.getUserPrincipal();
        return (p==null ? -1 : Integer.parseInt(p.getName()));
    }

    public static int getCurrentUserId()
    {
        return getCurrentUserId((HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest());
    }
    
    public static String removeQuotes(String in)
    {
        if (in.startsWith("\"") || in.startsWith("'"))
            in = in.substring(1, in.length()-1);
        return in;
    }
    
    public static void addFacesMessage(String msg)
    {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(msg));
    }

    public static void addFacesMessage(Throwable ex)
    {
        while(ex.getCause() != null) ex = ex.getCause();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,  ex.toString(), "");
    	FacesContext.getCurrentInstance().addMessage(null, facesMessage);
    }
    
    /**
     * Reads all bytes available on the input stream and writes them
     * to the output stream
     *
     * @param	is input stream
     * @param	os output stream
     */
    public static void writeThrough(InputStream is, OutputStream os) throws IOException
    {
        byte[] buf = new byte[1024];
        int read = is.read(buf);
        while (read!=-1)
        {
            os.write(buf, 0, read);
            read = is.read(buf);
        }
    }
    
    /**
     * Read a given resource to memory and return it in a byte array
     */
    public static byte[] readAll(InputStream in) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        writeThrough(in, baos);
        baos.close();
        in.close();
        return baos.toByteArray();
    }
    
    /**
     * Returns a scaled buffered image of the given image data, keeping aspect ratio
     * and centering it into the given width and height. Uses background WHITE to
     * fill the rest.
     * @param imageData image data
     * @param width new width
     * @param height new height
     * @return scaled image instance as a buffered image
     */
    public static BufferedImage getScaledBufferedImage(byte[] imageData, int width, int height)
    {
        ImageIcon image = new ImageIcon(imageData);
        float fac = Math.max(((float)image.getIconWidth())/width, ((float)image.getIconHeight())/height);
        int newWidth = Math.round(image.getIconWidth()/fac);
        int newHeight = Math.round(image.getIconHeight()/fac);
        Image scaledImage = image.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        img.getGraphics().setColor(Color.WHITE);
        img.getGraphics().fillRect(0, 0, width, height);
        img.getGraphics().drawImage(new ImageIcon(scaledImage).getImage(), (width - newWidth) / 2, (height - newHeight) / 2, null);
        
        return img;
    }
    
    public static void makeSpectra3Color(BufferedImage img) {
        Graphics gr = img.getGraphics();
        int width = img.getWidth();
        int height = img.getHeight();
        for (int x=0; x<width; x++) {
            for (int y=0; y<height; y++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb&0xff0000)>>16;
                int g = (rgb&0xff00)>>8;
                int b = (rgb&0xff);
                gr.setColor(Color.WHITE);
                if (r>g+50 && r>b+50) {
                    gr.setColor(Color.RED);
                } else if (r+g+b<384) {
                    gr.setColor(Color.BLACK);
                }
                gr.drawLine(x, y, x, y);
            }
        }
    }
    
    /**
     * Returns the encoding of the given buffered image
     * @param bufferedImage an image
     * @return byte array
     */
    public static byte[] getEncodedImage(BufferedImage bufferedImage, String type) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        
        Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(type);
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
//        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
//        param.setCompressionQuality(quality);
        writer.setOutput(ImageIO.createImageOutputStream(bos));
        writer.write(null, new IIOImage(bufferedImage, null, null), param);
        
        bos.close();
        return bos.toByteArray();
    }
    
}
