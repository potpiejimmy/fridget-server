/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.swing.ImageIcon;

/**
 * Holds static utility methods.
 * @author thorsten
 */
public class Utils {
    /**
     * Reads all bytes available on the input stream and writes them
     * to the output stream
     *
     * @param	is input stream
     * @param	os output stream
     * @throws java.io.IOException
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
    
    public static void writeFile(File file, byte[] data) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();
    }
    
    /**
     * Read a given resource to memory and return it in a byte array
     * @param in input stream
     * @return byte array
     * @throws java.io.IOException
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
     * Rotates the given image 90 degrees clockwise or counter clockwise.
     * @param image an image
     * @param clockwise clockwise rotation
     * @return rotated image
     */
    public static BufferedImage rotateImage90(Image image, boolean clockwise) {
	int w = image.getWidth(null);
	int h = image.getHeight(null);
	BufferedImage bi = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
	Graphics2D bg = bi.createGraphics();
	bg.rotate(Math.toRadians(clockwise ? 90 : -90));
	bg.drawImage(image, clockwise ? 0 : -w, clockwise ? -h : 0,null);
	bg.dispose();//cleans up resources
        return bi;
    }
    
    /**
     * Rotates the given image to the desired orientation (landscape or portrait).
     * If the image is already in the given orientation, it is returned unchanged.
     * Otherwise, the image is rotated 90 degrees clockwise if changing from
     * landscape to portrait mode or 90 degrees counter clockwise if changing
     * from portrait to landscape mode.
     * 
     * @param <T> image type
     * @param image an image
     * @param portraitOrientation desired orientation, false equals landscape mode, true equals portrait mode
     * @return image in desired orientation
     */
    public static <T extends Image> T setImageOrientation(T image, boolean portraitOrientation) {
        if ((image.getHeight(null)>image.getWidth(null)) == portraitOrientation) return image;
        return (T)rotateImage90(image, portraitOrientation);
    }
    
    /**
     * Returns a scaled buffered image of the given image data, keeping aspect ratio
     * and centering it into the given width and height. Uses background WHITE to
     * fill the rest.
     * @param imageData image data
     * @param width new width
     * @param height new height
     * @param rotateToFit if set to true, the input image is rotated 90 degress clockwise if necessary to fit
     * @return scaled image instance as a buffered image
     */
    public static BufferedImage getScaledBufferedImage(byte[] imageData, int width, int height, boolean rotateToFit)
    {
        Image image = new ImageIcon(imageData).getImage();
        if (rotateToFit) image = setImageOrientation(image, height>width);
        float fac = Math.max(((float)image.getWidth(null))/width, ((float)image.getHeight(null))/height);
        int newWidth = Math.round(image.getWidth(null)/fac);
        int newHeight = Math.round(image.getHeight(null)/fac);
        Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        img.getGraphics().setColor(Color.WHITE);
        img.getGraphics().fillRect(0, 0, width, height);
        img.getGraphics().drawImage(new ImageIcon(scaledImage).getImage(), (width - newWidth) / 2, (height - newHeight) / 2, null);
        
        return img;
    }
    
    /**
     * Returns the encoding of the given buffered image
     * @param bufferedImage an image
     * @param type image type such as png, jpeg etc.
     * @return byte array
     * @throws java.io.IOException
     */
    public static byte[] encodeImage(BufferedImage bufferedImage, String type) throws IOException
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
