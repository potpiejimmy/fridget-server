/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class GoogleTasks {
    
    protected Tasks taskService = null;
    
    public GoogleTasks(String userId) {
        
        this.taskService = getTasksService(userId);
    }
    
    /**
     * Build and return an authorized Tasks client service.
     * @param userId a user ID
     * @return an authorized Tasks client service
     */
    private Tasks getTasksService(String userId) {
        GoogleAuthorizationHelper helper = new GoogleAuthorizationHelper(userId);
        Credential credential = (Credential)helper.getCredentials();
        
        return new Tasks.Builder(
                GoogleAuthorizationHelper.HTTP_TRANSPORT, 
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(GoogleAuthorizationHelper.APPLICATION_NAME)
                .build();
    }
    
    public List<GoogleTaskList> getTaskLists() {
        try {
            List<TaskList> lists = taskService.tasklists().list().execute().getItems();
            List<GoogleTaskList> result = new ArrayList<>(lists.size());
            for (TaskList list : lists) result.add(new GoogleTaskList(list.getId(), list.getTitle()));
            return result;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
    }
    
    public TaskList getTaskListById(String taskListId) {
        try {
            return taskService.tasklists().get(taskListId).execute();
        } catch (IOException ioe) {
            return null;
        }
    }
    
    public List<Task> getTasks(String taskListId) {
        try {
            return taskService.tasks().list(taskListId).execute().getItems();
        } catch (IOException ioe) {
            return null;
        }
    }
    
    public static class GoogleTaskList {
        public String id;
        public String title;
        public GoogleTaskList(String id, String title) {
            this.id = id;
            this.title = title;
        }
    }
}
