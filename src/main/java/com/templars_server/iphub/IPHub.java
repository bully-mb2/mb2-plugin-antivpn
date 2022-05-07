package com.templars_server.iphub;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class IPHub {

    private static final String API_URL = "https://v2.api.iphub.info/ip/";
    private static final Duration API_TIMEOUT = Duration.ofMillis(500);

    private final String apiKey;
    private final ObjectMapper mapper;

    public IPHub(String apiKey) {
        this.apiKey = apiKey;
        this.mapper = new ObjectMapper();
    }

    public boolean checkIp(String ip) throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(API_URL + ip))
                .GET()
                .header("X-Key", apiKey)
                .build();

        HttpResponse<String> response = HttpClient.newBuilder()
                .connectTimeout(API_TIMEOUT)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response == null) {
            return false;
        }

        IPHubResponse json = mapper.readValue(response.body(), IPHubResponse.class);
        if (json.getError() != null) {
            throw new IOException("Error checking IP " + json.getError());
        }

        return json.getBlock() == 1;
    }

}
