package me.justinhu.mc.Shatter.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.moandjiezana.toml.Toml;
import me.justinhu.mc.Shatter.ShatterPlayer;
import me.justinhu.mc.Shatter.ShatterPlayerRequest;
import org.slf4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.rmi.UnexpectedException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ShatterAPI {
    private final String API_BASE_URL;

    private final Toml config;
    private final Logger logger;

    private ObjectMapper mapper;

    private final HttpClient httpClient;

    private final Cache<ShatterPlayerRequest, Optional<ShatterPlayer>> shatterPlayerCache;

    public ShatterAPI(Toml config, Path pluginFolder, Logger logger) {
        this.config = config;
        this.logger = logger;

        API_BASE_URL = config.getString("api_base_url");

        mapper = new ObjectMapper();

        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();

        try {
            File certFile = new File(pluginFolder.toFile(), config.getString("cert_file"));

            Certificate certificate = CertificateFactory.getInstance("X.509")
                    .generateCertificate(new FileInputStream(certFile));

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("server", certificate);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());

            httpClientBuilder.sslContext(sslContext);
        } catch (Exception e) {
            this.logger.error("An error occurred loading certificate, using default trust manager.", e);
        }

        httpClient = httpClientBuilder.build();

        shatterPlayerCache = CacheBuilder.newBuilder()
                .maximumSize(config.getLong("cache_size", 500L))
                .expireAfterAccess(config.getLong("cache_expiry", 60L), TimeUnit.SECONDS)
                .build();
    }

    public ShatterPlayer requestPlayer(ShatterPlayerRequest shatterPlayerRequest)
            throws IOException, InterruptedException {
        Optional<ShatterPlayer> shatterPlayer = shatterPlayerCache.getIfPresent(shatterPlayerRequest);
        if (shatterPlayer != null) return shatterPlayer.get();

        String serializedRequest = mapper.writeValueAsString(shatterPlayerRequest);

        HttpRequest request = HttpRequest.newBuilder(URI.create(API_BASE_URL)
                .resolve("/authenticate"))
                .POST(HttpRequest.BodyPublishers.ofString(serializedRequest))
                .setHeader("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int status = response.statusCode();

        if (status == HttpURLConnection.HTTP_NO_CONTENT) {
            shatterPlayer = Optional.empty();
        } else if (status == HttpURLConnection.HTTP_OK) {
            shatterPlayer = Optional.of(mapper.readValue(response.body(), ShatterPlayer.class));
        } else {
            throw new UnexpectedException("Invalid status code " + status);
        }

        shatterPlayerCache.put(shatterPlayerRequest, shatterPlayer);
        return shatterPlayer.get();
    }
}