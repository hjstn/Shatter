package me.justinhu.mc.Shatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velocitypowered.api.util.GameProfile;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public class ShatterPlayer {
    private final String loginId;
    private final UUID uuid;

    private final String name;

    private GameProfile shatterProfile;

    public ShatterPlayer(@JsonProperty("loginId") String loginId, @JsonProperty("name") String name) {
        this.loginId = loginId;
        this.name = name;

        uuid = UUID.nameUUIDFromBytes(("ShatterPlayer:" + loginId).getBytes());
    }

    public void setGameProfile(GameProfile gameProfile) {
       this.shatterProfile = new GameProfile(this.uuid, this.name, gameProfile.getProperties());
    }

    public GameProfile getShatterProfile() {
        return this.shatterProfile;
    }

    public String getName() {
        return this.name;
    }
}
