package com.fatihkoprucu.loaner.enums;

public enum RoleType {
    ROLE_ADMIN,
    ROLE_CUSTOMER;

    public String getValue() {
        return this.name();
    }
} 