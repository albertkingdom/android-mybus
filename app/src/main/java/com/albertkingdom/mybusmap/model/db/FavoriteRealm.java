package com.albertkingdom.mybusmap.model.db;

import javax.annotation.Nullable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class FavoriteRealm extends RealmObject {
    @PrimaryKey
    private String name;
    private String stationID;
    public FavoriteRealm() {}
    public FavoriteRealm(String name, @Nullable String stationID) {
        this.name = name;
        this.stationID = stationID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }
}
