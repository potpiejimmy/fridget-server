/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.google;

import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 *
 * @author thorsten
 */
public class GoogleTasksRenderer extends BaseRenderer {
    
    private GoogleTasks tasks = null;
    
    public GoogleTasksRenderer(Dimension dimension) {
        super(dimension);

        forceOrientation(true); // force landscape rendering
    }
    
    public BufferedImage renderTasks(String userId, String taskListId) {
        BufferedImage result = createImage();

        this.tasks = new GoogleTasks(userId);
        
        TaskList list = tasks.getTaskListById(taskListId);
        
        drawHeader(list.getTitle());
        
        tasks.getTasks(taskListId).forEach(t->drawItem(t));
        
        fillUpWithWhite();
        
        return result;
    }
    
    private void drawItem(Task task)
    {
        if (task.getDeleted()!=null && task.getDeleted()) return;
        if (task.getCompleted()!=null) return;
        int currentLine = this.lineNumber;
        drawColoredLines(32, Color.WHITE);
        drawColoredLines(2, Color.RED);
        graphics.setColor(Color.BLACK);
        graphics.setFont(graphics.getFont().deriveFont(20f));
        graphics.drawString("\u2610 "+task.getTitle(), 10, currentLine+22);				
        currentLine += 25;		
    }
}
