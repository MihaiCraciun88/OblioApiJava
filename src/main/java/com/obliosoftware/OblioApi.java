package com.obliosoftware;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;

public class OblioApi {
    
    protected String _cif                 = "";
    protected String _email               = "";
    protected String _secret              = "";
    protected String _baseURL             = "https://www.oblio.eu";
    protected AccessTokenHandlerInterface _accessTokenHandler  = null;

    public OblioApi setEmail(String email)
    {
        _email = email;
        return this;
    }

    public OblioApi setSecret(String secret)
    {
        _secret = secret;
        return this;
    }

    public OblioApi setAccessTokenHandler(AccessTokenHandlerInterface accessTokenHandler)
    {
        _accessTokenHandler = accessTokenHandler;
        return this;
    }

    public OblioApi setCif(String cif)
    {
        _cif = cif;
        return this;
    }

    public OblioApi build()
    {
        if (_accessTokenHandler == null) {
            setAccessTokenHandler(new AccessTokenHandlerFileStorage());
        }
        return this;
    }

    /**
     *  @param type String companies/vat_rates/products/clients/series/languages/management
     *  @param data Map filters
     *  @return JSONObject
     */
    public JSONObject nomenclature(String type, Map<String, String> filters) throws Exception
    {
        String cif = "";
        switch (type) {
            case "companies":
                break;
            case "vat_rates":
            case "products":
            case "clients":
            case "series":
            case "languages":
            case "management":
                cif = _getCif();
                break;
            default:
                throw new Exception("Type not implemented");
        }

        URIBuilder uriBuilder = new URIBuilder(_baseURL + "/api/nomenclature/" + type);
        if (!cif.equals("")) {
            uriBuilder.addParameter("cif", cif);
        }
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (!entry.getValue().equals("")) {
                uriBuilder.addParameter(entry.getKey(), entry.getValue());
            }
        }
        
        HttpRequest request = buildRequest()
            .uri(URI.create(uriBuilder.build().toString()))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        _checkResponse(response);

        return new JSONObject(response.body());
    }

    /**
     *  @param type String invoice/proforma/notice/receipt
     *  @param data JSONObject payload
     *  @return JSONObject
     */
    public JSONObject createDoc(String type, JSONObject data) throws Exception
    {
        _checkType(type);
        if (!data.has("cif") && !_cif.equals("")) {
            data.put("cif", _cif);
        }
        if (!data.has("cif")) {
            throw new Exception("Empty cif");
        }
        
        HttpRequest request = buildRequest()
            .uri(URI.create(_baseURL + "/api/docs/" + type))
            .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
            .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        _checkResponse(response);

        return new JSONObject(response.body());
    }

    public HttpRequest.Builder buildRequest() throws Exception
    {
        AccessToken accessToken = getAccessToken();
        return HttpRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .header("Authorization", accessToken.token_type + " " + accessToken.access_token);
    }

    public AccessToken getAccessToken() throws Exception
    {
        AccessToken accessToken = _accessTokenHandler.get();
        if (accessToken == null) {
            accessToken = _generateAccessToken();
            _accessTokenHandler.set(accessToken);
        }
        return accessToken;
    }
    protected AccessToken _generateAccessToken() throws Exception
    {
        JSONObject payload = new JSONObject();
        payload.put("client_id", _email);
        payload.put("client_secret", _secret);
        payload.put("grant_type", "client_credentials");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(_baseURL + "/api/authorize/token"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
            .build();

        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        AccessToken accessToken = new AccessToken()
            .fromJsonString(response.body());
        return accessToken;
    }

    protected String _getCif() throws Exception
    {
        if (_cif == "") {
            throw new Exception("Empty cif");
        }
        return _cif;
    }

    protected void _checkType(String type) throws Exception
    {
        if (!Arrays.asList(new String[] {"invoice", "proforma", "notice", "receipt"}).contains(type)) {
            throw new Exception("Type not supported");
        }
    }

    protected void _checkResponse(HttpResponse<String> response) throws Exception
    {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            JSONObject jsonResponse = new JSONObject(response.body());

            if (!jsonResponse.has("statusMessage")) {
                jsonResponse.put("statusMessage",
                    String.format("Error! HTTP response status: %d", response.statusCode()));
            }
            throw new Exception(jsonResponse.getString("statusMessage"));
        }
    }
}