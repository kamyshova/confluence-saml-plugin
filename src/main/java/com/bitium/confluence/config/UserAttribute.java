package com.bitium.confluence.config;

import com.atlassian.user.User;

public enum UserAttribute {

    NICKNAME {
        @Override
        public String retrieveAttribute(final User user) {
            return user.getName();
        }
    },
    FULLNAME {
        @Override
        public String retrieveAttribute(final User user) {
            return user.getFullName();
        }
    },
    EMAIL {
        @Override
        public String retrieveAttribute(final User user) {
            return user.getEmail();
        }
    };

    abstract public String retrieveAttribute(final User user);
}
