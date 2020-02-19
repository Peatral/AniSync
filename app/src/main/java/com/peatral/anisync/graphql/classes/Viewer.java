package com.peatral.anisync.graphql.classes;

import io.aexp.nodes.graphql.annotations.GraphQLProperty;

@GraphQLProperty(name = "Viewer")
public class Viewer {
    private String name;
    private int id;
    private UserAvatar avatar;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public UserAvatar getAvatar() {
        return avatar;
    }

    public void setAvatar(UserAvatar avatar) {
        this.avatar = avatar;
    }
}
