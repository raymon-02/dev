package com.w6.external_api;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.w6.data.Article;
import com.w6.data.Response;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class Geolocator {

    private final Gson gson = new GsonBuilder().create();

    @Autowired
    private Geocoder geocoder;

    public LatLng findLocation(final Article article) throws IOException {
        Response fromJson = gson.fromJson(article.getResponse(), Response.class);

        for (String where : fromJson.getTable().getWhere()) {
            GeocoderRequest request = new GeocoderRequest(where, "EN");
            GeocodeResponse geocode = geocoder.geocode(request);
            if (geocode.getStatus() == GeocoderStatus.OK) {
                return geocode.getResults().get(0).getGeometry().getLocation();
            }
        }
        return null;
    }
}
