 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


 package com.emc.metalnx.services.auth;

 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.irods.jargon.core.connection.IRODSAccount;
 import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
 import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
 
 public class SC2iIRODSLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {
 
	 @Autowired
	 IRODSAccessObjectFactory irodsAccessObjectFactory;

     @Value("${irods.auth.scheme:STANDARD}")
     private String irodsAuthScheme;

	 @Value("${login.shib.logout_url}")
     private String logout_url;
 
	 private static final Logger logger = LoggerFactory.getLogger(SC2iIRODSLogoutSuccessHandler.class);
 
	 @Override
	 public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			 throws IOException, ServletException {
 
		 logger.info("Logging out...");
 
		 try {
			 IRODSAccount irodsAccount = ((UserTokenDetails) authentication.getDetails()).getIrodsAccount();
			 String username = irodsAccount.getUserName();
 
			 logger.info("Closing session and eating all exceptions");
			 irodsAccessObjectFactory.closeSessionAndEatExceptions(irodsAccount);
			 irodsAccessObjectFactory.closeSessionAndEatExceptions();
 
			 logger.debug("Removing current session temporary directory for file upload");
			 try {
				 File tmpSessionDir = new File(username);
				 if (tmpSessionDir.exists()) {
					 FileUtils.forceDelete(tmpSessionDir);
				 }
			 } catch (Exception e) {
				 logger.error("User {} temporary directory for upload does not exist.", username);
			 }
 
			 logger.info("invalidating session");
			 request.getSession().invalidate();
 
             if (irodsAuthScheme.equalsIgnoreCase("shibboleth")) {
			     logger.info("Redirect to {}", logout_url);
			     response.sendRedirect(logout_url);
             } else {
			     response.sendRedirect("/metalnx/login/");
            }
			 logger.info("User {} disconnected successfully", username);
		 } catch (Exception e) {
			 logger.info("User session is already expired. There is no need to clear session.");
		 }
 
		 super.onLogoutSuccess(request, response, authentication);
	 }
 
 }
 
