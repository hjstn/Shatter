package me.justinhu.mc.Shatter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ShatterPlayerRequest {
    public final String loginId;
    public final String hostKey;

    public ShatterPlayerRequest(@JsonProperty("loginId") String loginId, @JsonProperty("hostKey") String hostKey) {
        this.loginId = loginId;
        this.hostKey = hostKey;
    }

    public boolean equals(Object object) {
        if (!(object instanceof ShatterPlayerRequest)) return false;

        ShatterPlayerRequest shatterPlayerRequest = (ShatterPlayerRequest) object;

        return loginId.equals(shatterPlayerRequest.loginId) && hostKey.equals(shatterPlayerRequest.hostKey);
    }

    public int hashCode() {
        return Objects.hash(loginId, hostKey);
    }
}
