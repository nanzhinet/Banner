package com.mohistmc.banner.injection.network.connection;

import com.mojang.authlib.properties.Property;

import java.net.SocketAddress;
import java.util.UUID;

public interface InjectionConnection {

    default UUID bridge$getSpoofedUUID() {
        return null;
    }

    default void banner$setSpoofedUUID(UUID spoofedUUID) {
    }

    default Property[] bridge$getSpoofedProfile() {
        return new Property[0];
    }

    default void bridge$setSpoofedProfile(Property[] spoofedProfile) {
    }

    default String bridge$hostname() {
        return null;
    }

    default void banner$setHostName(String hostName) {

    }

    default SocketAddress getRawAddress() {
        return null;
    }
}
