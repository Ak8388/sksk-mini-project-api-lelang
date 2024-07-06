package jawa.sinaukoding.sk.model;

import jawa.sinaukoding.sk.entity.User;

public record Authentication(Long id, User.Role role, boolean isAuthenticated) {

    
}
