/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.authentication.NativeAuthPlugin;
import org.irods.irods4j.authentication.PamPasswordAuthPlugin;
import org.irods.irods4j.high_level.administration.IRODSUsers;
import org.irods.irods4j.high_level.administration.IRODSUsers.User;
import org.irods.irods4j.high_level.administration.IRODSUsers.UserType;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.jargon.core.connection.AuthScheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.TransactionException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridAuthenticationException;
import com.emc.metalnx.core.domain.exceptions.DataGridDatabaseException;
import com.emc.metalnx.core.domain.exceptions.DataGridServerException;
import com.emc.metalnx.services.interfaces.AuthenticationProviderService;
import com.emc.metalnx.services.interfaces.IRODSServices;

public class IRODSAuthenticationProvider implements AuthenticationProviderService, Serializable {
	private static final String IRODS_ANONYMOUS_ACCOUNT = "anonymous";

	@Autowired
	UserDao userDao;
	
	@Autowired
	private IRODSServices irodsServices;

	private String irodsHost;
	private String irodsPort;
	private String irodsZoneName;

	@Value("${irods.auth.scheme}")
	private String irodsAuthScheme;

	// Instance variables to be set to UserTokenDetails instance.
	private DataGridUser user;

	private static final Logger logger = LogManager.getLogger(IRODSAuthenticationProvider.class);

	private static final long serialVersionUID = -4984545776727334580L;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		logger.info("authenticate()");
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		UsernamePasswordAuthenticationToken authObject;

		RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
		
		AuthScheme authSchemeEnum = null;

		if (RequestContextHolder.getRequestAttributes() != null) {
			HttpServletRequest request = ((ServletRequestAttributes) attribs).getRequest();
			String authScheme = request.getParameter("authScheme");
			authSchemeEnum = AuthScheme.findTypeByString(authScheme);
			logger.info("authScheme:{}", authScheme);
		}

		if (authSchemeEnum == null) {
			String error_msg = String.format("no authScheme found in request");
			logger.error(error_msg);
			throw new DataGridAuthenticationException(error_msg);
		}

		logger.debug("Setting username {}", username);

		try {
			
			IRODSConnection conn = new IRODSConnection();
			conn.connect(this.irodsHost, Integer.parseInt(this.irodsPort), new QualifiedUsername(username, this.irodsZoneName));
			
			if (authSchemeEnum == AuthScheme.STANDARD) {
				// NATIVE
				conn.authenticate(new NativeAuthPlugin(), password);
			} else {
				// PAM
				conn.authenticate(new PamPasswordAuthPlugin(true), password);
			}

			// Retrieving logging user
			
			// TODO get user type and set grantedAuth appropriately
			User irodsAccount = new User(username, Optional.of(this.irodsZoneName));
			
			Optional<UserType> currentUserType = IRODSUsers.type(conn.getRcComm(), irodsAccount);

			GrantedAuthority grantedAuth;
			if (currentUserType.equals(UserType.RODSADMIN)) {
				grantedAuth = new IRODSAdminGrantedAuthority();
			} else if (currentUserType.equals(UserType.GROUPADMIN)) {
				grantedAuth = new IRODSGroupadminGrantedAuthority();
			} else {
				grantedAuth = new IRODSUserGrantedAuthority();
			}

			logger.info("granted authority:{}", grantedAuth);

			// Settings granted authorities
			List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
			grantedAuths.add(grantedAuth);

			// Returning authentication token with the access object factory injected
			authObject = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);

			// Creating UserTokenDetails instance for the current authenticated user
			UserTokenDetails userDetails = new UserTokenDetails();
			userDetails.setIrodsAccount(irodsAccount);
			userDetails.setUser(this.user);
			
			// TODO is there a better place to store the connection?
			userDetails.setiRODSConnection(conn);

			// Settings the user details object into the authentication object
			authObject.setDetails(userDetails);
		} catch (TransactionException e) {
			logger.error("Database not responding");
			throw new DataGridDatabaseException("database not responding", e);
		} catch (Exception e) {
			logger.error("Exception when authenticating", e);
			throw new DataGridServerException("Exception when authenticating", e);
			
		}

		return authObject;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return this.irodsHost;
	}

	/**
	 * @param irodsHost the irodsHost to set
	 */
	public void setIrodsHost(String irodsHost) {
		this.irodsHost = irodsHost;
	}

	/**
	 * @return the irodsPort
	 */
	public String getIrodsPort() {
		return this.irodsPort;
	}

	/**
	 * @param irodsPort the irodsPort to set
	 */
	public void setIrodsPort(String irodsPort) {
		this.irodsPort = irodsPort;
	}

	/**
	 * @return the irodsZoneName
	 */
	public String getIrodsZoneName() {
		return this.irodsZoneName;
	}

	/**
	 * @param irodsZoneName the irodsZoneName to set
	 */
	public void setIrodsZoneName(String irodsZoneName) {
		this.irodsZoneName = irodsZoneName;
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

	/*
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

	public String getIrodsAuthScheme() {
		return irodsAuthScheme;
	}

	public void setIrodsAuthScheme(String irodsAuthScheme) {
		this.irodsAuthScheme = irodsAuthScheme;
	}
}
