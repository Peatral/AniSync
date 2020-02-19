package com.peatral.anisync.graphql.classes;

import com.peatral.anisync.graphql.enums.MediaListStatus;
import io.aexp.nodes.graphql.annotations.GraphQLProperty;

import java.util.List;

@GraphQLProperty(name = "MediaListGroup")
public class MediaListGroup {
    private List<MediaList> entries;
    private String name;
    private MediaListStatus status;

    public List<MediaList> getEntries() {
        return entries;
    }

    public void setEntries(List<MediaList> entries) {
        this.entries = entries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaListStatus getStatus() {
        return status;
    }

    public void setStatus(MediaListStatus status) {
        this.status = status;
    }
}
