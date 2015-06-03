/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author thorsten
 */
public class GoogleCalendar
{
    private List<CalendarItem> items;
    private com.google.api.services.calendar.Calendar calendarConnection;

    public GoogleCalendar(String userId) throws IOException
    {
        items = new ArrayList<>();

        connectCalendar(userId);
        List<CalendarListEntry> calendars = calendarConnection.calendarList().list().execute().getItems();
	                       
        for (CalendarListEntry e : calendars)
        {
                if (e.getId().contains("weeknum")) break;
                com.google.api.services.calendar.Calendar.Events.List lr = calendarConnection.events().list(e.getId());

                Calendar cal = Calendar.getInstance();
                
                lr.setTimeMin(new DateTime(cal.getTime())); // today
                cal.add(Calendar.DAY_OF_YEAR, 15); //15 days into the future
                lr.setTimeMax(new DateTime(cal.getTime())); 
                lr.setSingleEvents(true);

                addEventsToItems(lr.execute());
        }
        Collections.sort(items);
    }
	               
    private boolean connectCalendar(String userId)
    {
        try
        {
            GoogleAuthorizationHelper helper = new GoogleAuthorizationHelper(userId);
            Credential credential = (Credential)helper.getCredentials();
            
            calendarConnection = new com.google.api.services.calendar.Calendar.Builder(
                GoogleAuthorizationHelper.HTTP_TRANSPORT,
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(GoogleAuthorizationHelper.APPLICATION_NAME)
                .build();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
    private void addEventsToItems(Events es)
    {
        for (Event e : es.getItems()) addEventToItems(e);
    }
	               
    private void addEventToItems(Event e)
    {
        boolean isWholeDay;
        Date start;

        if (e.getStart().getDateTime() == null)
        {
            isWholeDay = true;
            start = new Date(e.getStart().getDate().getValue());
        }
        else
        {
            isWholeDay = false;
            start = new Date(e.getStart().getDateTime().getValue());
        }

        //dirty: add title to description if title has more than 20 chars
        String description = e.getDescription() != null ? e.getDescription().trim() : null;
        String title = e.getSummary().trim();
        if (title.length()>20 && !isWholeDay)
        {
            if (description != null)
                    description = title + ", " + description;
            else 
                    description = title;
        }

        items.add(new CalendarItem(title, description, e.getLocation() != null ? e.getLocation().trim() : null, start, e.getEnd() != null && e.getEnd().getDateTime() != null ? new Date(e.getEnd().getDateTime().getValue()) : null, isWholeDay));
    }
	               
    public List<CalendarItem> nextCalendarItems()
    {
        return items;
    }
	
    public static class CalendarItem implements Comparable<CalendarItem>
    {
        public boolean wholeDay;
        public Date start;
        public Date end;
        public String title;
        public String description;
        public String location;

        public CalendarItem(String title, String desc, String location, Date start, Date end, boolean wholeDay)
        {
            this.wholeDay = wholeDay;
            this.end = end;
            this.start = start;
            this.title = title;
            this.description = desc;
            this.location = location;
        }

        @Override
        public int compareTo(CalendarItem o) {
            return start.compareTo(o.start);
        }
    }
}