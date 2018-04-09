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

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.Group;
import org.apache.commons.lang.StringUtils;
import com.atlassian.confluence.core.ConfluenceActionSupport;

public class ConfigureAction extends ConfluenceActionSupport {
	private static final long serialVersionUID = 1L;

	private String autoCreateUser;
	private String defaultAutoCreateUserGroup;
	private String idpRequired;
	private String redirectUrl;
	private String maxAuthenticationAge;
	private String spEntityId;
	private ArrayList<String> existingGroups;

	private SAMLConfluenceConfig saml2Config;

	public void setSaml2Config(SAMLConfluenceConfig saml2Config) {
		this.saml2Config = saml2Config;
	}

	public ConfigureAction() {
	}

	public String getIdpRequired() {
		return idpRequired;
	}

	public void setIdpRequired(String idpRequired) {
		this.idpRequired = idpRequired;
	}

	public String getAutoCreateUser() {
		return autoCreateUser;
	}

	public void setAutoCreateUser(String autoCreateUser) {
		this.autoCreateUser = autoCreateUser;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public void setRedirectUrl(String redirectUrl) {
		this.redirectUrl = redirectUrl;
	}
	
	public void setMaxAuthenticationAge(String maxAuthenticationAge) {
		this.maxAuthenticationAge=maxAuthenticationAge;
	}
	
	public String getMaxAuthenticationAge() {
		return this.maxAuthenticationAge;
	}

	public String getDefaultAutoCreateUserGroup() {
		return defaultAutoCreateUserGroup;
	}

	public void setDefaultAutoCreateUserGroup(String defaultAutoCreateUserGroup) {
		this.defaultAutoCreateUserGroup = defaultAutoCreateUserGroup;
	}

	public ArrayList<String> getExistingGroups() {
		UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
		List<Group> groupObjects = userAccessor.getGroupsAsList();
		existingGroups = new ArrayList<String>();
		for (Group groupObject : groupObjects) {
			existingGroups.add(groupObject.getName());
		}
		setExistingGroups(existingGroups);
		return existingGroups;
	}

	public void setExistingGroups(ArrayList<String> existingGroups) {
		this.existingGroups = existingGroups;
	}

	public void setSpEntityId(final String spEntityId) {
		this.spEntityId = spEntityId;
	}

	public String getSpEntityId() {
		return spEntityId;
	}

	/**
	 * @return Metadata federation file or null if none was passed to a form.
     */
    public File getMetadataFile() {
        final MultiPartRequestWrapper wrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
		final File[] files = wrapper.getFiles("metadata");
        return files != null && files.length > 0
                ? files[0]
                : null;
    }

	protected List<String> getPermissionTypes() {
		List<String> requiredPermissions = super.getPermissionTypes();
		requiredPermissions.add("ADMINISTRATECONFLUENCE");
		return requiredPermissions;
	}

	@Override
	public void validate() {
		if (StringUtils.isBlank(getSpEntityId())) {
			addActionError(getText("saml2plugin.admin.spEntityIdIsMissing"));
		}

		if (getMetadataFile() == null) {
			addActionError(getText("saml2plugin.admin.metadataFileIsMissing"));
		}

		if (StringUtils.isBlank(getIdpRequired())) {
			setIdpRequired("false");
		} else {
			setIdpRequired("true");
		}

		if (StringUtils.isBlank(getAutoCreateUser())) {
			setAutoCreateUser("false");
		} else {
			setAutoCreateUser("true");
		}
		
		if(StringUtils.isBlank(getMaxAuthenticationAge()) || (!StringUtils.isNumeric(getMaxAuthenticationAge()))){
			addActionError(getText("saml2plugin.admin.maxAuthenticationAgeInvalid"));
		}

		super.validate();
	}

	public String doDefault() throws Exception {
		setRedirectUrl(saml2Config.getRedirectUrl());
		setSpEntityId(saml2Config.getSpEntityId());
		long maxAuthenticationAge = saml2Config.getMaxAuthenticationAge();
		
		//Default Value
		if(maxAuthenticationAge==Long.MIN_VALUE){
			setMaxAuthenticationAge("7200");
		}
		//Stored Value
		else{
			setMaxAuthenticationAge(String.valueOf(maxAuthenticationAge));
		}
				
		String idpRequired = saml2Config.getIdpRequired();

		if (idpRequired != null) {
			setIdpRequired(idpRequired);
		} else {
			setIdpRequired("false");
		}

		String autoCreateUser = saml2Config.getAutoCreateUser();
		if (autoCreateUser != null) {
			setAutoCreateUser(autoCreateUser);
		} else {
			setAutoCreateUser("false");
		}

		String defaultAutocreateUserGroup = saml2Config.getAutoCreateUserDefaultGroup();
		if (defaultAutocreateUserGroup.isEmpty()) {
			// NOTE: Set the default to "confluence-users".
			// This is used when configuring the plugin for the first time and no default was set
			defaultAutocreateUserGroup = SAMLConfluenceConfig.DEFAULT_AUTOCREATE_USER_GROUP;
		}
		setDefaultAutoCreateUserGroup(defaultAutocreateUserGroup);
		return super.doDefault();
	}

	public String execute() throws Exception {
		saml2Config.setIdpRequired(getIdpRequired());
		saml2Config.setRedirectUrl(getRedirectUrl());
		saml2Config.setAutoCreateUser(getAutoCreateUser());
		saml2Config.setAutoCreateUserDefaultGroup(getDefaultAutoCreateUserGroup());
		saml2Config.setMaxAuthenticationAge(Long.parseLong(getMaxAuthenticationAge()));
		saml2Config.setSpEntityId(getSpEntityId());
		saml2Config.setMetadataFile(getMetadataFile());

		addActionMessage(getText("saml2plugin.admin.message.saved"));
		return "success";
	}

}