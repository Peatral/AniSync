package com.peatral.anisync.graphql;

import com.peatral.anisync.graphql.classes.MediaList;

public interface SyncListener {
    void message(String text, int id);
}
