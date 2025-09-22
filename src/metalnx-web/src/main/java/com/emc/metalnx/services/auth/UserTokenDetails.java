 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.auth;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.administration.IRODSUsers.User;
import org.irods.irods4j.high_level.connection.IRODSConnection;

/**
 * The object that is encapsulated in the user session
 *
 */
public class UserTokenDetails {

	private DataGridUser user;
	private User irodsAccount;
	private IRODSConnection iRODSConnection;   // TODO should this be stored elsewhere?
	
	private static final Logger logger = LogManager.getLogger(UserTokenDetails.class);
	
	/**
	 * @return the irodsAccount
	 */
	public User getIrodsAccount() {
		return irodsAccount;
	}
	/**
	 * @param irodsAccount the irodsAccount to set
	 */
	public void setIrodsAccount(User irodsAccount) {
		this.irodsAccount = irodsAccount;
	}


	/**
	 * @return the user
	 */
	public DataGridUser getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(DataGridUser user) {
		this.user = user;
	}
	public IRODSConnection getiRODSConnection() {
		return iRODSConnection;
	}
	public void setiRODSConnection(IRODSConnection iRODSConnection) {
		this.iRODSConnection = iRODSConnection;
	}
	
}
