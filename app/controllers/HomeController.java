package controllers;

import Model.Location;
import Model.Earthquake;
import Model.Coordinate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.*;
import views.html.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static play.libs.Json.toJson;

import com.google.gson.*;

import javax.inject.Inject;

public class HomeController extends Controller {
    @Inject
    private FormFactory formFactory;
    private Earthquake earthquake;
    private Location location;
    private Coordinate coordinate;
    //merge element

    public Result index() {
        return ok(index.render("Your new application is ready."));
    }

    public Result addLocation (Http.Request request) {
        Form<Earthquake> earthquakeForm = formFactory.form(Earthquake.class).bindFromRequest(request);
        Form<Location> locationForm = formFactory.form(Location.class).bindFromRequest(request);
        earthquake = earthquakeForm.get();
        location = locationForm.get();

        earthquake.save();
        location.save();
        try {
            coordinate = getGeoCoordinates(location);
        }
        catch(IOException e){

        }
        return redirect(routes.HomeController.returnHeatmap());
    }

    public Result deleteLocation(Long id){
        Location location  = Location.find.byId(id);
        Earthquake earthquake = Earthquake.find.byId(id);

        location.delete();
        earthquake.delete();
        return redirect(routes.HomeController.index());
    }

    public Result getHistory() {
        List<Location> location = Location.find.all();
        return ok(history.render(toJson(location).toString()));
        //return ok(toJson(coordinates));
    }

    public Coordinate getGeoCoordinates(Location location)  throws MalformedURLException, ProtocolException, IOException{
        String city = location.city.replace(" ","+");
        String country = location.country.replace(" ", "+");
        final String APIKey = "170ae8c810934cae9ec4179055994214";

        URL url = new URL("https://api.opencagedata.com/geocode/v1/json?q=" + city
                + "%2C+%2C+" + country + "&key=" + APIKey + "&pretty=1");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JsonParser JsonParser = new JsonParser();
        Gson g = new Gson();
        JsonElement jsonTree = JsonParser.parse(response.toString());

        JsonObject total = jsonTree.getAsJsonObject();
        JsonArray JsonArray = total.getAsJsonArray("results");
        JsonObject most = JsonArray.get(0).getAsJsonObject();
        JsonObject gemetry = most.get("geometry").getAsJsonObject();

        double latitude = gemetry.get("lat").getAsDouble();
        double longitude = gemetry.get("lng").getAsDouble();

        return new Coordinate(latitude,longitude);

    }

    public Result returnHeatmap() throws MalformedURLException, ProtocolException, IOException {
        ArrayList<JsonElement> coordinateList = new ArrayList<>();

        /*Defaults:*//*

        String startTime = "2017-01-01";
        String endTime = "2019-04-17";
        String latitude = "19.8968";
        String longitude = "-155.5828";
        int maxRadius = 100;
        int minMagnitude = 5;

        if (!earthquake.getStartTime().equals("2017-01-01")) {
            startTime = earthquake.getStartTime();
        }
        if (!earthquake.getEndTime().equals("2019-04-17")) {
            endTime = earthquake.getEndTime();
        }
        if (!earthquake.getLatitude().equals("19.8968")) {
            latitude = earthquake.getLatitude();
        }
        if (!earthquake.getLongitude().equals("-155.5828")) {
            longitude = earthquake.getLongitude();
        }
        if (earthquake.getmaxRadius() != 100) {
            maxRadius = earthquake.getmaxRadius();
        }
        if (earthquake.getMinMagnitude() != 5) {
            minMagnitude = earthquake.getMinMagnitude();
        }

        URL url = new URL("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson" +
        "&starttime=" + startTime +
        "&endtime=" + endTime +
        "&latitude=" + latitude +
        "&longitude=" + longitude +
        "&maxradiuskm=" + maxRadius +
        "&minmagnitude=" + minMagnitude);*/



        URL url = new URL("https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=" + earthquake.startTime +
                "&endtime=" + earthquake.endTime + "&latitude=" + coordinate.latitude + "&longitude=" + coordinate.longitude +
                "&maxradiuskm=" + earthquake.radius + "&minmagnitude=" + earthquake.minMagnitude);

        //http://chillyfacts.com/java-send-http-getpost-request-and-read-json-response
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        JsonParser JsonParser = new JsonParser();


        JsonElement jsonTree = JsonParser.parse(response.toString());
        System.out.println(jsonTree);
        JsonObject test = (JsonObject) jsonTree;

        JsonElement mainElement = test.get("features");

        JsonArray main = mainElement.getAsJsonArray();

        for (int i = 0; i < main.size(); i++) {

            JsonElement first = main.get(i);
            JsonObject inside = first.getAsJsonObject();

            JsonElement properties = inside.get("properties");
            JsonObject properties_object = properties.getAsJsonObject();

            JsonElement Time = properties_object.get("time");
            /*
            JsonElement Magnitude = properties_object.get("mag");
            JsonElement Place = properties_object.get("place");


            JsonElement geometry = inside.get("geometry");
            JsonObject coordinates_object = geometry.getAsJsonObject();
            JsonArray coordinates = coordinates_object.get("coordinates").getAsJsonArray();

            JsonElement Longitude = coordinates.get(0);
            JsonElement Latitude = coordinates.get(1);
            */
            JsonElement Title = properties_object.get("title");



            //https://stackoverflow.com/questions/17432735/convert-unix-time-stamp-to-date-in-java

            BigInteger bd = Time.getAsBigInteger();
            Long value = bd.divide(new BigInteger("1000")).longValue();

            java.util.Date unixSeconds =new java.util.Date((long)value * 1000);


            //JsonArray last = coordinates.getAsJsonArray();

            JsonArray combined = new JsonArray();
            combined.add(Title);
            combined.add(unixSeconds.toString());



            coordinateList.add(combined);
            //3.0 69km SSW of Kobuk, Alaska 2019-04-16 23:55:25 (UTC)
            //#1 MinMagnitude: textbox
            //#2 Start Date: textbox
            //#3 End Date: textbox
            //#4 Radius: textox
            //
            //#5 World: Radio Button
            //#6 Latitude: textbox
            //#7 Longitude: textbox
        }
        return ok(heatMap.render(coordinate, earthquake, coordinateList.toString()));

    }
}
