package com.mohistmc.banner.injection.server.players;

import java.util.Date;

public interface InjectionBanListEntry {

    default Date getCreated() {
        return null;
    }
}
