package org.atlasapi.remotesite.youtube.entity;

/**
 * Allowed : 1<br>
 * Denied : 0<br>
 * Moderated : 2<br>
 * Undefined: -1
 * 
 * @author augusto
 * 
 */
public enum Permission {
    ALLOWED("allowed"), DENIED("denied"), MODERATED("moderated"), UNDEFINED("undefined");
    private String permission;

    private Permission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public Permission value(String value) {
        if ("moderated".equalsIgnoreCase(value)) {
            return Permission.MODERATED;
        } else if ("denied".equalsIgnoreCase(value)) {
            return Permission.DENIED;
        } else if ("allowed".equalsIgnoreCase(value)) {
            return Permission.ALLOWED;
        }
        return Permission.UNDEFINED;
    }
}
