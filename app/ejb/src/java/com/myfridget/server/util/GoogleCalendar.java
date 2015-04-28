/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    
    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;
    
    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    public GoogleCalendar() throws IOException
    {
        items = new ArrayList<>();

        connectCalendar();
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
            isWholeDay = true;
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

        items.add(new CalendarItem(title, description, e.getLocation() != null ? e.getLocation().trim() : null, start, new Date(e.getEnd().getDateTime().getValue()), isWholeDay));
    }
	               
    public List<CalendarItem> nextCalendarItems()
    {
        return items;
    }
	
    private boolean connectCalendar()
    {
        GoogleClientSecrets secrets = new GoogleClientSecrets();
        secrets.set("ClientId", "389854474115-t74hoj958dnj0924sjmc70pj310b4m54.apps.googleusercontent.com");
        secrets.set("ClientSecret", "etmItjZlt6Uunv7HBaTyMasP");

        try
        {
            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY, secrets, Arrays.asList(CalendarScopes.CALENDAR_READONLY))
                    .build();
            Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");

            calendarConnection = new com.google.api.services.calendar.Calendar.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName("MyProject")
                .build();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
        return true;
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