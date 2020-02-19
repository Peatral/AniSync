package com.peatral.anisync.graphql.classes;

import io.aexp.nodes.graphql.annotations.GraphQLArgument;
import io.aexp.nodes.graphql.annotations.GraphQLProperty;

import java.util.List;

@GraphQLProperty(name = "MediaListCollection", arguments = {@GraphQLArgument(name = "userId"), @GraphQLArgument(name = "type")})
public class MediaListCollection {
    private List<MediaListGroup> lists;
    private User user;

    public List<MediaListGroup> getLists() {
        return lists;
    }

    public void setLists(List<MediaListGroup> lists) {
        this.lists = lists;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
