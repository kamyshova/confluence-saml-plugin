package com.bitium.confluence.config;

import org.opensaml.common.xml.SAMLConstants;

import java.util.HashMap;
import java.util.Map;

public enum BindingUri {

    SAML2_POST_BINDING_URI("POST", SAMLConstants.SAML2_POST_BINDING_URI),
    SAML2_REDIRECT_BINDING_URI("REDIRECT", SAMLConstants.SAML2_REDIRECT_BINDING_URI);

    private static final Map<String, BindingUri> aliasToBinding = new HashMap<String, BindingUri>();
    static {
        aliasToBinding.put("POST", SAML2_POST_BINDING_URI);
        aliasToBinding.put("REDIRECT", SAML2_REDIRECT_BINDING_URI);
    }

    final String alias;
    final String value;


    BindingUri(String alias, String value) {
        this.alias = alias;
        this.value = value;
    }

    public static String getByAlias(String alias) {
        BindingUri bindingUri = aliasToBinding.get(alias);
        if (bindingUri == null) {
            throw new IllegalArgumentException("There is no binding URI with alias: " + alias);
        }
        return bindingUri.value;
    }
}
