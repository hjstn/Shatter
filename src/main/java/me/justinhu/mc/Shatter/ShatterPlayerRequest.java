package me.justinhu.mc.Shatter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.velocitypowered.api.util.GameProfile;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.UUID;

public class ShatterPlayerRequest {
    public final String loginId;
    public final String hostKey;

    public ShatterPlayerRequest(@JsonProperty("loginId") String loginId, @JsonProperty("hostKey") String hostKey) {
        this.loginId = loginId;
        this.hostKey = hostKey;
    }
}
