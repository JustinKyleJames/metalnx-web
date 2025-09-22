 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


 package com.emc.metalnx.services.auth;

 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.administration.IRODSUsers.User;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
 import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
 
 public class IRODSLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {
 
	 private static final Logger logger = LogManager.getLogger(IRODSLogoutSuccessHandler.class);
 
	 @Override
	 public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			 throws IOException, ServletException {
 
		 logger.info("Logging out...");
 
		 try {
			 UserTokenDetails userTokenDetails = (UserTokenDetails) authentication.getDetails();
			 User irodsAccount = userTokenDetails.getIrodsAccount();
			 IRODSConnection conn = userTokenDetails.getiRODSConnection();
			 String username = irodsAccount.name;
 
			 logger.info("Closing session and eating all exceptions");
			 conn.disconnect();
 
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
 
			 response.sendRedirect("/metalnx/login/");
			 logger.info("User {} disconnected successfully", username);
		 } catch (Exception e) {
			 logger.info("User session is already expired. There is no need to clear session.");
		 }
 
		 super.onLogoutSuccess(request, response, authentication);
	 }
 
 }
 