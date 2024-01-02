package com.obliosoftware;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AccessTokenHandlerFileStorage implements AccessTokenHandlerInterface {
    private final String filepath = "assets/acces_token.json";

    @Override
    public AccessToken get()
    {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filepath));
            String jsonString = reader.readLine();
            AccessToken accessToken = new AccessToken()
                .fromJsonString(jsonString);

            long unixTime = System.currentTimeMillis() / 1000L;

            if (accessToken.request_time + accessToken.expires_in > unixTime) {
                return accessToken;
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public void set(AccessToken accessToken)
    {
        FileWriter file = null;
        try {
            file = new FileWriter(filepath);
            file.write(accessToken.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
