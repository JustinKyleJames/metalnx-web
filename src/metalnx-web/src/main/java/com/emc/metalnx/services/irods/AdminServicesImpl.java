/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.net.ConnectException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.MetalnxException;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.ConfigService;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class AdminServicesImpl implements AdminServices {

	@Autowired
	IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Autowired
	private ConfigService configService;

	private IRODSAccount irodsAccount;

	private static final Logger logger = LogManager.getLogger(AdminServicesImpl.class);

	@Override
	public UserAO getUserAO() throws DataGridConnectionRefusedException {
		try {
			// Returning UserAO instance
			return irodsAccessObjectFactory.getUserAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate UserAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public SpecificQueryAO getSpecificQueryAO() throws DataGridConnectionRefusedException {
		try {
			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getSpecificQueryAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public DataObjectAO getDataObjectAO() throws DataGridConnectionRefusedException {
		try {
			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getDataObjectAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}

		return null;
	}

	@Override
	public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
			throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}

		return null;
	}

	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	@Override
	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	@Override
	public ConfigService getConfigService() {
		return configService;
	}

	@Override
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Override
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	@Override
	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

}
