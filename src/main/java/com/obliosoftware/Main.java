package com.obliosoftware;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    public static void main(String[] args)
    {
        try {
            Dotenv env = Dotenv.configure()
                .directory("assets")
                .load();

            JSONArray products = new JSONArray()
                .put(new JSONObject()
                    .put("name", "Abonament")
                    .put("code", "")
                    .put("description", "")
                    .put("measuringUnit", "buc")
                    .put("currency", "RON")
                    .put("vatName", "Normala")
                    .put("vatPercentage", 19)
                    .put("vatIncluded", true)
                    .put("quantity", 2)
                    .put("productType", "Serviciu")
                    .put("price", 100)
                );

            JSONObject data = new JSONObject()
                .put("cif", env.get("CIF"))
                .put("client", new JSONObject()
                    .put("cif", "RO19")
                    .put("name", "BUCUR OBOR")
                    .put("code", "")
                    .put("state", "Bucuresti")
                    .put("city", "Sector 2")
                )
                .put("seriesName", env.get("SERIES_NAME"))
                .put("products", products);
            System.out.println(data.toString());

            OblioApi api = new OblioApi()
                .setEmail(env.get("API_EMAIL"))
                .setSecret(env.get("API_SECRET"))
                .setCif(env.get("CIF"))
                .build();

            // JSONObject result = api.nomenclature("companies", new HashMap<String, String>());
            JSONObject result = api.createDoc("invoice", data);
            System.out.println(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}