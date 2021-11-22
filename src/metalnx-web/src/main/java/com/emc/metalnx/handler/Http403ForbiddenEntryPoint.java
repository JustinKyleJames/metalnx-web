package com.emc.metalnx.handler;

import java.util.Map;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Enumeration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.TransactionException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.ConfigService;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridAuthenticationException;
import com.emc.metalnx.core.domain.exceptions.DataGridDatabaseException;
import com.emc.metalnx.core.domain.exceptions.DataGridServerException;
import com.emc.metalnx.services.interfaces.AuthenticationProviderService;

public class Http403ForbiddenEntryPoint implements AuthenticationEntryPoint {

    @Value("${irods.host}")
    private String host;

    @Value("${irods.port}")
    private int port;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${login.shib.user_attribute}")
    private String shib_user_attribute;

    @Value("${login.shib.user_re}")
    private String shib_user_re;

    @Value("${irods.admin.user}")
    private String admin_user;

    @Value("${irods.admin.password}")
    private String admin_password;

    @Value("${login.shib.group_attribute}")
    private String groupAttribute;

    @Value("${login.shib.group_delimiter}")
    private String groupDelimiter;

    @Value("${login.shib.required_group}")
    private String requiredGroup;

    @Value("${login.shib.group_mapping}")
    private String groupMapping;

    @Value("${irods.auth.scheme:STANDARD}")
    private String irodsAuthScheme;

    @Autowired
    IRODSAccessObjectFactory irodsAccessObjectFactory;

    @Autowired
    UserDao userDao;

	private static final Logger logger = LoggerFactory.getLogger(Http403ForbiddenEntryPoint.class);

	// Instance variables to be set to UserTokenDetails instance.
	private IRODSAccount irodsAccount;
	private DataGridUser user;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2)
			throws IOException, ServletException {

		logger.info("Http403ForbiddenEntryPoint : commence");
		logger.debug("Pre-authenticated entry point called. Rejecting access");

		logger.info("request url was:{}", request.getRequestURL());

        if (irodsAuthScheme.equalsIgnoreCase("shibboleth")) {

            String redirect = "";
            ModelAndView model = null;

            logger.info("shib_enabled");

            logger.info("==================");
            logger.info("attr: eppn = " + request.getHeader("ajp_" + shib_user_attribute));
            logger.info("attr: displayName = " + request.getHeader("ajp_displayName"));
            logger.info("attr: affiliation = " + request.getHeader("ajp_affiliation"));
            logger.info("attr: sn = " + request.getHeader("ajp_sn"));
            logger.info("attr: isMemberOf = " + request.getHeader("ajp_isMemberOf"));
            logger.info("attr: givenName = " + request.getHeader("ajp_givenName"));
            logger.info("attr: Shib-Identity-Provider = " + request.getHeader("ajp_Shib-Identity-Provider"));
            logger.info("attr: UID = " + request.getHeader("ajp_uid"));
            logger.info("==================");

            // We need the presets and shibboleth configuration if we're using shibboleth
            if (host == null || host.equals("") || port == 0 || zone == null || zone.equals("")) {
                throw new DataGridServerException("Host, port, and zone presets must be set if using Shibboleth");
            }

            ArrayList<String> expectedIrodsGroups = new ArrayList<String>();
            if (shib_user_attribute == null || shib_user_attribute.equals("") ||
               admin_user == null || admin_user.equals("") ||
               admin_password == null || admin_password.equals("")) {
                throw new DataGridServerException("login.shib.user_attribute, admin.shib.user, admin.shib.password must be set when using Shibboleth");
            }

            String userName = (String)request.getHeader("ajp_" + shib_user_attribute);
            if (userName == null || userName.equalsIgnoreCase("")) {
                throw new DataGridServerException("Could not read user name from shibboleth attributes");
            }

            // If a regular expression exists in login.shib.user_re
            // use this to parse the user name from userName
            if (shib_user_re != null && !shib_user_re.equals("")) {
                userName = userName.replaceAll(shib_user_re, "$1");
            }

            // See if there is a group attribute and required group for access.
            logger.info("groupAttribute = " + groupAttribute);
            if (groupAttribute != null && !groupAttribute.equals("")) {

                // We have a group attribute.  Get the required group.
                logger.info("requiredGroup = " + requiredGroup);
                if (requiredGroup != null && !requiredGroup.equals("")) {

                    // There is a required group.  Make sure the group in the attribute matches
                    // the required group.
                    String groupListStr = request.getHeader("ajp_" + groupAttribute) == null
                        ? ""
                        : (String)request.getHeader("ajp_" + groupAttribute);

                    logger.info("groupListStr = " + groupListStr);

                    List<String> groupList;

                    if (groupDelimiter != null && !groupDelimiter.equals("")) {
                        groupList = Arrays.asList(groupListStr.split(groupDelimiter));
                    } else {
                        groupList = new ArrayList<String>();
                        groupList.add(groupListStr);
                    }

                    boolean found = false;
                    for (String group : groupList) {
                        if (requiredGroup.equals(group)) {
                            found = true;
                        }
                    }

                    if (!found) {
                        logger.info("required group not found in group list string");
                        response.sendRedirect("/metalnx/images/noPermission.jpg");
                        return;
                    }

                    // Build a list of iRODS groups that this user should belong to.
                    if (groupMapping != null && !groupMapping.equals("")) {
                        logger.debug("groupMapping=" + groupMapping);
                        for (String entry : groupMapping.split("\\|")) {
                            logger.info("group entry=" + entry);
                            String irodsGroup = entry.split("=")[0];
                            String[] grouperGroups = entry.split("=")[1].split(";");

                            boolean foundAll = true;
                            for (String grouperGroup : grouperGroups) {
                                if (!groupList.contains(grouperGroup)) {
                                    foundAll = false;
                                }
                            }
                            if (foundAll) {
                                expectedIrodsGroups.add(irodsGroup);
                            }
                        }
                    }
                }
            }

            try {

                // Login as admin
                logger.info("irodsAdminAccount: admin_user = " + admin_user + " admin_password = " + admin_password);
                IRODSAccount irodsAdminAccount = IRODSAccount.instance(host, 1247, admin_user, admin_password, "", zone, "");
                irodsAdminAccount.setAuthenticationScheme(AuthScheme.findTypeByString("STANDARD"));
                try {
                    AuthResponse authResponse = this.irodsAccessObjectFactory.authenticateIRODSAccount(irodsAdminAccount);
                } catch (Exception e) {
                    logger.info("failed in authenticate");
                    response.sendRedirect("/metalnx/images/noPermission.jpg");
                    return;
                }

                // get user and group info
                //   select USER_NAME, USER_GROUP_NAME where USER_NAME = '*userName'
                IRODSGenQueryExecutor irodsGenQueryExecutor = this.irodsAccessObjectFactory.getIRODSGenQueryExecutor(irodsAdminAccount);

                IRODSGenQueryBuilder builder = new IRODSGenQueryBuilder(true, null);

                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_NAME);
                builder.addSelectAsGenQueryValue(RodsGenQueryEnum.COL_USER_GROUP_NAME);
                builder.addConditionAsGenQueryField(RodsGenQueryEnum.COL_USER_NAME,
                        QueryConditionOperators.EQUAL, userName);

                IRODSGenQueryFromBuilder query = builder.exportIRODSQueryFromBuilder(100);
                IRODSQueryResultSetInterface resultSet = irodsGenQueryExecutor
                            .executeIRODSQueryAndCloseResult(query, 0);

                if (resultSet.getResults().size() == 0) {

                    // Need to create the user
                    User user = new User();
                    user.setName(userName);
                    user.setUserType(UserTypeEnum.RODS_USER);
                    UserAO userAO = irodsAccessObjectFactory.getUserAO(irodsAdminAccount);
                    userAO.addUser(user);
                }

                // If there is a group mapping, adjust the groups.
                if (groupMapping != null && !groupMapping.equals("")) {

                    ArrayList<String> actualIrodsGroups = new ArrayList<String>();
                    List<IRODSQueryResultRow> rowList = resultSet.getResults();
                    for (IRODSQueryResultRow row : rowList) {
                        actualIrodsGroups.add(row.getColumn("USER_GROUP_NAME"));
                    }

                    // Remove "public" and <user> from actual group list.  We don't care about those.
                    actualIrodsGroups.remove(userName);
                    actualIrodsGroups.remove("public");

                    logger.info("-------------------------------------------");
                    logger.info("expectedIrodsGroups = " + expectedIrodsGroups);
                    logger.info("actualIrodsGroups = " + actualIrodsGroups);

                    // see what is in expectedIrodsGroups but not actualIrodsGroups
                    ArrayList<String> toBeAdded = new ArrayList<String>(expectedIrodsGroups);
                    toBeAdded.removeAll(actualIrodsGroups);

                    // see what is in actualIrodsGroups but not expectedIrodsGroups
                    ArrayList<String> toBeDeleted = new ArrayList<String>(actualIrodsGroups);
                    toBeDeleted.removeAll(expectedIrodsGroups);

                    logger.info("Groups toBeAdded = " + toBeAdded);
                    logger.info("Groups toBeDeleted = " + toBeDeleted);

                    logger.info("-------------------------------------------");

                    UserGroupAO userGroupAO = this.irodsAccessObjectFactory.getUserGroupAO(irodsAdminAccount);

                    for (String group : toBeAdded) {
                        try {
                            userGroupAO.addUserToGroup(group, userName, null);
                        } catch (JargonException e) {}
                    }

                    for (String group : toBeDeleted) {
                        try {
                            userGroupAO.removeUserFromGroup(group, userName, null);
                        } catch (JargonException e) {}
                    }

                }

                HttpSession session = request.getSession(true);

                // proxy to new user
                IRODSAccount irodsAccount = IRODSAccount.instanceWithProxy(host, port,
                        userName, admin_password, "", zone, "", admin_user, zone);

                AuthResponse authResponse;
                try {
                    authResponse = this.irodsAccessObjectFactory.authenticateIRODSAccount(irodsAccount);
                } catch (Exception e) {
                    logger.info("failed in authenticate");
                    response.sendRedirect("/metalnx/images/noPermission.jpg");
                    return;
                }

                logger.info("auth with proxy successful");

                // Settings iRODS account
                this.irodsAccount = authResponse.getAuthenticatedIRODSAccount();

                // Retrieving logging user
                User irodsUser = new User();

                try {
                    irodsUser = this.irodsAccessObjectFactory.getUserAO(this.irodsAccount).findByName(userName);
                    logger.debug("irodsUser:{}", userName);
                } catch (JargonException e) {
                    logger.error("Could not find user: " + e.getMessage());
                }

                GrantedAuthority grantedAuth;
                if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)) {
                    grantedAuth = new IRODSAdminGrantedAuthority();
                } else if (irodsUser.getUserType().equals(UserTypeEnum.GROUP_ADMIN)) {
                    grantedAuth = new IRODSGroupadminGrantedAuthority();
                } else {
                    grantedAuth = new IRODSUserGrantedAuthority();
                }

                logger.info("granted authority:{}", grantedAuth);

                // Settings granted authorities
                List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
                grantedAuths.add(grantedAuth);

                // Returning authentication token with the access object factory injected
                UsernamePasswordAuthenticationToken authObject;

                authObject = new UsernamePasswordAuthenticationToken(userName, "", grantedAuths);

                // Creating UserTokenDetails instance for the current authenticated user
                UserTokenDetails userDetails = new UserTokenDetails();
                userDetails.setIrodsAccount(this.irodsAccount);

                // Settings the user details object into the authentication object
                authObject.setDetails(userDetails);

                SecurityContext sc = SecurityContextHolder.getContext();
                sc.setAuthentication(authObject);
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

                // If the user is found
                if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)
                        || irodsUser.getUserType().equals(UserTypeEnum.GROUP_ADMIN)
                        || irodsUser.getUserType().equals(UserTypeEnum.RODS_USER)) {

                    // If the user is not yet persisted in our database
                    DataGridUser user = this.userDao.findByUsernameAndZone(irodsUser.getName(), irodsUser.getZone());

                    if (user == null) {
                        user = new DataGridUser();
                        user.setUsername(irodsUser.getName());
                        user.setAdditionalInfo(irodsUser.getZone());
                        user.setDataGridId(Long.parseLong(irodsUser.getId()));
                        user.setEnabled(true);
                        user.setFirstName("");
                        user.setLastName("");
                        if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)) {
                            logger.debug("setting user type admin:{}", irodsUser.getUserType());
                            user.setUserType(UserTypeEnum.RODS_ADMIN.getTextValue());
                        } else if (irodsUser.getUserType().equals(UserTypeEnum.GROUP_ADMIN)) {
                            logger.debug("setting user type groupadmin:{}", irodsUser.getUserType());
                            user.setUserType(UserTypeEnum.GROUP_ADMIN.getTextValue());
                        } else {
                            logger.debug("setting user type rodsuser:{}", irodsUser.getUserType());
                            user.setUserType(UserTypeEnum.RODS_USER.getTextValue());
                        }
                        this.userDao.save(user);
                    } else {
                        // check for an update of user type
                        
                        user.setUsername(irodsUser.getName());
                        user.setAdditionalInfo(irodsUser.getZone());
                        user.setDataGridId(Long.parseLong(irodsUser.getId()));

                        if (user.getUserType() != irodsUser.getUserType().getTextValue()) {
                            logger.info("updating user type based on iRODS current value");
                            user.setUserType(irodsUser.getUserType().getTextValue());
                            this.userDao.merge(user);
                            logger.info("updated user type in db");
                        }
                    }

                    this.user = user;
                    userDetails.setUser(this.user);
                }
            } catch (GenQueryBuilderException e) {
                logger.error("GenQueryBuilderException caught");
                response.sendRedirect("/metalnx/httpError/serverNotResponding");
                return;
            } catch (JargonQueryException e) {
                logger.error("JargonQueryException caught");
                response.sendRedirect("/metalnx/httpError/serverNotResponding");
                return;
            } catch (TransactionException e) {
                logger.error("TransactionException caught");
                response.sendRedirect("/metalnx/httpError/500");
                return;
            } catch (InvalidUserException | org.irods.jargon.core.exception.AuthenticationException e) {
                logger.info("AuthenticationException caught");
                response.sendRedirect("/metalnx/images/noPermission.jpg");
                return;
            } catch (JargonException e) {
                logger.error("JargonException caught");
                response.sendRedirect("/metalnx/httpError/serverNotResponding");
                return;
            }

            response.sendRedirect("/metalnx/login/");
            return;

        }

		// Redirect to the login page without the exception message if
		// the URL path ends with /metalnx or /metalnx/.
		if (urlPathEndsWithAppRootName(request)) {
		    response.sendRedirect("/metalnx/login/");
		    return;
		}

		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
		logger.info("last saved request was:{}", savedRequest);

		response.sendRedirect("/metalnx/login/exception/");
	}
	
	private boolean urlPathEndsWithAppRootName(HttpServletRequest request)
	{
	    try {
	        String p = new URL(request.getRequestURL().toString()).getPath();
	        return p.endsWith("/metalnx") || p.endsWith("/metalnx/");
	    }
	    catch (MalformedURLException e) {}
	    
	    return false;
	}

	/**
	 * Temporary implementation of the GrantedAuthority interface for Admin
	 * authentication
	 */
	private class IRODSAdminGrantedAuthority implements GrantedAuthority {

		private static final long serialVersionUID = 357603546013216540L;

		@Override
		public String getAuthority() {
			return "ROLE_ADMIN";
		}
	}

	/**
	 * Temporary implementation of the GrantedAuthority interface for GroupAdmin
	 * authentication
	 */
	private class IRODSGroupadminGrantedAuthority implements GrantedAuthority {

		private static final long serialVersionUID = 1L;

		@Override
		public String getAuthority() {
			return "ROLE_GROUPADMIN";
		}
	}

	/**
	 * Temporary implementation of the GrantedAuthority interface for User
	 * authentication
	 */
	private class IRODSUserGrantedAuthority implements GrantedAuthority {

		private static final long serialVersionUID = 357603546013216540L;

		@Override
		public String getAuthority() {
			return "ROLE_USER";
		}
	}

}
