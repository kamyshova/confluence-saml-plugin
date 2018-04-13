package com.bitium.confluence.servlet;

import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceAuthenticator;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.confluence.user.UserAccessor;
import com.atlassian.seraph.auth.Authenticator;
import com.atlassian.seraph.auth.DefaultAuthenticator;
import com.atlassian.seraph.config.SecurityConfigFactory;
import com.atlassian.spring.container.ContainerManager;
import com.atlassian.user.Group;
import com.atlassian.user.User;
import com.atlassian.user.impl.DefaultUser;
import com.atlassian.user.search.SearchResult;
import com.atlassian.user.security.password.Credential;
import com.bitium.confluence.config.SAMLConfluenceConfig;
import com.bitium.saml.servlet.SsoLoginServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


public class SsoConfluenceLoginServlet extends SsoLoginServlet {
	protected void authenticateUserAndLogin(HttpServletRequest request,
			HttpServletResponse response, String email)
			throws Exception {

		Authenticator authenticator = SecurityConfigFactory.getInstance().getAuthenticator();

        if (authenticator instanceof ConfluenceAuthenticator) {
            User confluenceUser = fetchUser(email);

            if (confluenceUser == null) {
                log.error(String.format("Failed to find user by email: %s", email));
                redirectToLoginWithSAMLError(response, null, "user_not_found");
            }

            if (confluenceUser != null) {
                Boolean result = authoriseUserAndEstablishSession((DefaultAuthenticator) authenticator, confluenceUser, request, response);

                if (result) {
                    redirectToSuccessfulAuthLandingPage(request, response);
                    return;
                }
            }
        }

        redirectToLoginWithSAMLError(response, null, "user_not_found");
	}

	private User fetchUser(String email) {
        UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        SearchResult usersByEmail = userAccessor.getUsersByEmail(email);
        if (usersByEmail == null) {
            return null;
        }
        if (usersByEmail.pager().isEmpty()) {
            return null;
        }

        if (usersByEmail.pager().iterator().hasNext()) {
            Object next = usersByEmail.pager().iterator().next();
            log.error(next.getClass());
            if (next instanceof User) {
                return (User)next;
            }
        }
        return null;
    }

    protected ConfluenceUser tryCreateOrUpdateUser(String username) {
        if (saml2Config.getAutoCreateUserFlag()){
            UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");

            String fullName = credential.getAttributeAsString("cn");
            String email = credential.getAttributeAsString("mail");

            log.warn("Creating user account for " + username );
            ConfluenceUser createdUser = userAccessor.createUser(new DefaultUser(username, fullName, email), Credential.NONE);

            // Find the first administrator user and use it to add the user to the confluence-users group if it exists
            ConfluenceUser administratorUser = getAdministratorUser();

            String defaultGroup = saml2Config.getAutoCreateUserDefaultGroup();
            if (defaultGroup.isEmpty()) {
                defaultGroup = SAMLConfluenceConfig.DEFAULT_AUTOCREATE_USER_GROUP;
            }

            Group confluenceUsersGroup = userAccessor.getGroup(defaultGroup);
            if (administratorUser != null && confluenceUsersGroup != null) {
                AuthenticatedUserThreadLocal.set(administratorUser);
                userAccessor.addMembership(confluenceUsersGroup, createdUser);
            }
            return createdUser;
        } else {
            // not allowed to auto-create user
            log.error("User not found and auto-create disabled: " + username);
        }
        return null;
    }

    @Override
    protected String getDashboardUrl() {
        return saml2Config.getBaseUrl() + "/dashboard.action";
    }

    @Override
    protected String getLoginFormUrl() {
        return saml2Config.getBaseUrl() + "/login.action";
    }

    private ConfluenceUser getAdministratorUser() {
        UserAccessor userAccessor = (UserAccessor) ContainerManager.getComponent("userAccessor");
        List<String> administratorNames = userAccessor.getMemberNamesAsList(userAccessor.getGroup("confluence-administrators"));
        if (administratorNames != null && administratorNames.size() > 0) {
            return userAccessor.getUserByName(administratorNames.get(0));
        }
        return null;
    }
}
