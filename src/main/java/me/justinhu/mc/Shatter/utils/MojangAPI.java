package me.justinhu.mc.Shatter.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.rmi.UnexpectedException;
import java.util.concurrent.TimeUnit;

public class MojangAPI {
    private final String API_HAS_UUID;

    private final Toml config;

    private final HttpClient httpClient;

    private final Cache<String, Boolean> hasUUIDCache;

    public MojangAPI(Toml config) {
        this.config = config;

        API_HAS_UUID = this.config.getString("api_has_uuid",
                "https://api.mojang.com/users/profiles/minecraft/");

        httpClient = HttpClient.newHttpClient();

        hasUUIDCache = CacheBuilder.newBuilder()
                .maximumSize(config.getLong("cache_size", 500L))
                .expireAfterAccess(config.getLong("cache_expiry", 60L), TimeUnit.SECONDS)
                .build();
    }

    public boolean hasUUID(String username) throws IOException, InterruptedException {
        Boolean uuidExists = hasUUIDCache.getIfPresent(username);

        if (uuidExists == null) {
            uuidExists = fetchHasUUID(username);
            hasUUIDCache.put(username, uuidExists);
        }

        return uuidExists;
    }

    private boolean fetchHasUUID(String username) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(API_HAS_UUID
                + URLEncoder.encode(username, StandardCharsets.UTF_8.name())))
                .build();

        HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

        int status = response.statusCode();

        if (status == HttpURLConnection.HTTP_OK) {
            return true;
        } else if (status == HttpURLConnection.HTTP_NO_CONTENT) {
            return false;
        }

        throw new UnexpectedException("Improper status code.");
    }
}