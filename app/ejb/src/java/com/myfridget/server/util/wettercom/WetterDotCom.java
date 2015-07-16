/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.myfridget.server.util.wettercom;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author thorsten
 */
public class WetterDotCom {
    
    protected final static DateFormat WEATHER_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    
    public static enum WeatherState {
            sonnig,
            leicht_bewölkt,
            wolkig,
            bedeckt,
            regen,
            gewitter,
            klar,
            bewölkt_nachts,
            UNKNOWN
    };
	
    public static class DayWeather
    {
        public String tempMin;
        public String tempMax;

        public WeatherState morning;
        public WeatherState noon;
        public WeatherState evening;
        public WeatherState night;

        public DayWeather(JsonObject node)
        {
            tempMin = node.getString("tn");
            tempMax = node.getString("tx");

            morning = getWeatherStateFromNumber(Integer.parseInt(node.getJsonObject("06:00").getString("w")));
            noon    = getWeatherStateFromNumber(Integer.parseInt(node.getJsonObject("11:00").getString("w")));
            evening = getWeatherStateFromNumber(Integer.parseInt(node.getJsonObject("17:00").getString("w")));
            night   = getWeatherStateFromNumber(Integer.parseInt(node.getJsonObject("23:00").getString("w")), true);		
        }

        private WeatherState getWeatherStateFromNumber(int n)
        {
                return getWeatherStateFromNumber(n, false);
        }

        private WeatherState getWeatherStateFromNumber(int n, boolean night)
        {
            if (n==0) return night ? WeatherState.klar : WeatherState.sonnig;
            if (n==1) return night ? WeatherState.bewölkt_nachts : WeatherState.leicht_bewölkt;
            if (n==2) return WeatherState.wolkig;
            if (n==3 || n==4) return WeatherState.bedeckt;
            if (n>4 && n<9) return WeatherState.regen;
            if (n==9) return WeatherState.gewitter;
            if (n>=50 && n<=86) return WeatherState.regen;
            return WeatherState.UNKNOWN;
        }
    }
        
    public String city;
    public DayWeather today;
    public DayWeather tomorrow;

    public String creditString;
    public String creditLink;

    public void updateData(String plz) throws IOException
    {
        // first get city code
        JsonObject locationResult = getAnswerFromWeatherCom("http://api.wetter.com/location/index/search/", plz);
        JsonArray locations = locationResult.getJsonObject("search").getJsonArray("result");
        String citycode = locations.getJsonObject(0).getString("city_code");

        JsonObject weatherResult = getAnswerFromWeatherCom("http://api.wetter.com/forecast/weather/city/", citycode);

        Calendar day = Calendar.getInstance();
        
        JsonObject xnList = weatherResult.getJsonObject("city").getJsonObject("forecast");

        today = new DayWeather(xnList.getJsonObject(WEATHER_DATE_FORMATTER.format(day.getTime())));
        day.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow = new DayWeather(xnList.getJsonObject(WEATHER_DATE_FORMATTER.format(day.getTime())));

        creditString = weatherResult.getJsonObject("city").getJsonObject("credit").getString("text");
        creditLink = weatherResult.getJsonObject("city").getJsonObject("credit").getString("link");

        city = weatherResult.getJsonObject("city").getString("name");
    }		

    private JsonObject getAnswerFromWeatherCom(String baseUrl, String searchString) throws IOException
    {
        //fridget is the project name I created in my Wetter.Com account
        //671.... is an MD5 hash code created from "fridget<APIKey><CityCode>"
        //API key is fedf9983e0f8481924e2ef0ea17d11c7 provided by wetter.com

        String autKey = md5Hex("fridget"+"fedf9983e0f8481924e2ef0ea17d11c7"+searchString);
        URL url = new URL(baseUrl+searchString+"/project/fridget/cs/"+autKey+"?output=json");

        HttpURLConnection request = (HttpURLConnection)url.openConnection();
        request.addRequestProperty("Accept", "application/json");
        
        // Open the stream using a JsonReader for easy access.
        JsonReader reader = Json.createReader(request.getInputStream());
        // Read the content.
        JsonObject responseFromServer = reader.readObject();
        // Clean up the streams.
        reader.close();
        request.disconnect();

        return responseFromServer;
    }

    protected static String md5Hex(String in) {
        StringBuilder md5 = new StringBuilder(new BigInteger(1, md5(in)).toString(16));
        while (md5.length()<32) md5.insert(0, '0');
        return md5.toString();
    }
    
    protected static byte[] md5(String in) {
        try {
            return MessageDigest.getInstance("MD5").digest(in.getBytes());
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void main(String[] args) throws Exception {
        WetterDotCom wetter = new WetterDotCom();
        wetter.updateData("60598");
    }
}
