package com.peatral.anisync.graphql.classes;

import io.aexp.nodes.graphql.annotations.GraphQLProperty;

import java.util.List;

@GraphQLProperty(name = "Page")
public class Page {
    private PageInfo pageInfo;
    private List<Media> media;

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public List<Media> getMedia() {
        return media;
    }

    public void setMedia(List<Media> media) {
        this.media = media;
    }
}
