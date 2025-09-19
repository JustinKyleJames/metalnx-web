 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.auth;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.administration.IRODSUsers.User;

/**
 * The object that is encapsulated in the user session
 *
 */
public class UserTokenDetails {

	private DataGridUser user;
	private User irodsAccount;
	
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
	 * @return the irodsFileSystem
	 */
	public IRODSFileSystem getIrodsFileSystem() {
		try {
			return IRODSFileSystem.instance();
		} catch (JargonException e) {
			logger.error("Could not get instance of IRODSFileSystem: ", e);
		}
		return null;
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		try {
			return this.getIrodsFileSystem().getIRODSAccessObjectFactory();
		} catch (JargonException e) {
			logger.error("Could not get Access Object Factory from IRODS: ", e);
		}
		return null;
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
	
}
