/**
 * Confluence SAML Plugin - a confluence plugin to allow SAML 2.0
 *	authentication. 
 *
 *	Copyright (C) 2014 Bitium, Inc.
 *	
 *	This file is part of Confluence SAML Plugin.
 *	
 *	Confluence SAML Plugin is free software: you can redistribute it 
 *	and/or modify it under the terms of the GNU General Public License
 *	as published by the Free Software Foundation, either version 3 of
 *	the License, or (at your option) any later version.
 *	
 *	Confluence SAML Plugin is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *	
 *	You should have received a copy of the GNU General Public License
 *	along with Pineapple. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bitium.confluence.config;

import com.bitium.saml.config.SAMLConfig;
import org.apache.commons.lang.StringUtils;

public class SAMLConfluenceConfig extends SAMLConfig {

	public static final String DEFAULT_AUTOCREATE_USER_GROUP = "confluence-users";
	public static final String PLATFORM_UID_ATTRIBUTE_SETTING = "saml2.platformUidAttribute";

	public String getAlias() {
		return "confluenceSAML";
	}

	public void setPlatformUidAttribute(final String platformUidAttribute) {
		pluginSettings.put(PLATFORM_UID_ATTRIBUTE_SETTING, platformUidAttribute);
	}

	public String getPlatformUidAttribute() {
		return StringUtils.defaultString(
				(String) pluginSettings.get(PLATFORM_UID_ATTRIBUTE_SETTING),
				UserAttribute.EMAIL.name()
		);
	}
}
