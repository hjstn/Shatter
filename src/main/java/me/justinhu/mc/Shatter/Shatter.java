package me.justinhu.mc.Shatter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.ProxyServer;
import me.justinhu.mc.Shatter.utils.MojangAPI;
import me.justinhu.mc.Shatter.utils.ShatterAPI;
import me.justinhu.mc.Shatter.utils.ShatterToml;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Plugin(id = "shatter", name = "Shatter", version = "1.0-SNAPSHOT", url = "https://mc.justinhu.me",
        description = "Low-effort hybrid authentication.", authors = {"hjstn"})
public class Shatter {
    private final ProxyServer server;
    private final Logger logger;

    private final ShatterToml shatterToml;

    private final ShatterAPI shatterAPI;
    private final MojangAPI mojangAPI;

    private final Toml config;
    private final Toml shatterConfig;

    private final Cache<InboundConnection, ShatterPlayer> playerCache;

    @Inject
    public Shatter(ProxyServer server, Logger logger, @DataDirectory Path folder) {
        shatterToml = new ShatterToml(folder, logger);

        config = this.shatterToml.loadConfig();
        shatterConfig = this.config.getTable("Shatter");

        this.server = server;
        this.logger = logger;

        shatterAPI = new ShatterAPI(this.config.getTable("ShatterAPI"), folder, logger);
        mojangAPI = new MojangAPI(this.config.getTable("MojangAPI"));

        playerCache = CacheBuilder.newBuilder()
                .maximumSize(shatterConfig.getLong("cache_size", 500L))
                .expireAfterAccess(shatterConfig.getLong("cache_expiry", 60L), TimeUnit.SECONDS)
                .build();
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onPreLogin(PreLoginEvent event) {
        // Prevent overriding Geyser/Floodgate.
        if (event.getResult().isForceOfflineMode()) return;

        Optional<InetSocketAddress> hostName = event.getConnection().getVirtualHost();

        if (hostName.isEmpty()) return;

        ShatterPlayerRequest shatterPlayerRequest = new ShatterPlayerRequest(event.getUsername(),
                hostName.get().getHostString());

        final ShatterPlayer player;

        try {
            player = shatterAPI.requestPlayer(shatterPlayerRequest);
        } catch(Exception e) {
            logger.error("An error occurred while accessing the Shatter API.", e);

            return;
        }

        if (player == null) return;

        try {
            if (mojangAPI.hasUUID(player.getName())) {
                event.setResult(PreLoginEvent.PreLoginComponentResult
                        .denied(Component.text("Shatter: Username taken, please change your username.")));

                return;
            }
        } catch (Exception e) {
            logger.error("An error occurred while checking username availability.", e);

            event.setResult(PreLoginEvent.PreLoginComponentResult
                    .denied(Component.text("Shatter: Could not verify username uniqueness, please rejoin.")));

            return;
        }

        event.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());

        playerCache.put(event.getConnection(), player);
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onGameProfileRequest(GameProfileRequestEvent event) {
        ShatterPlayer player = playerCache.getIfPresent(event.getConnection());

        if (player != null) {
            playerCache.invalidate(event.getConnection());

            player.setGameProfile(event.getGameProfile());
            event.setGameProfile(player.getShatterProfile());
        }
    }
}
