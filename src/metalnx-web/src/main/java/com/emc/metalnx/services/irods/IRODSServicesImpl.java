/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.net.ConnectException;

import org.irodsext.gallery.GalleryListServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.MetalnxException;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.IRODSServices;

@Service("irodsServices")
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class IRODSServicesImpl implements IRODSServices {

	@Autowired
	IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Autowired
	MidTierConfiguration midTierConfiguration;

	private UserTokenDetails userTokenDetails;
	private IRODSAccount irodsAccount;

	private static final Logger logger = LogManager.getLogger(IRODSServicesImpl.class);

	public IRODSServicesImpl() {
		/*
		 * This is a shim to support testing, probably a dependency on seccontextholder
		 * is unwarranted and should be factored out of this layer in the future - mcc
		 */
		try {
			this.userTokenDetails = (UserTokenDetails) SecurityContextHolder.getContext().getAuthentication()
					.getDetails();
			this.irodsAccount = this.userTokenDetails.getIrodsAccount();

		} catch (NullPointerException npe) {
			logger.warn("null pointer getting security context, assume running outside of container", npe);
		}
	}

	public IRODSServicesImpl(IRODSAccount acct) {
		this.irodsAccount = acct;
	}

	@Override
	public TicketAdminService getTicketAdminService() throws DataGridConnectionRefusedException {
		TicketAdminService tas = null;

		try {
			TicketServiceFactory tsf = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
			tas = tsf.instanceTicketAdminService(irodsAccount);
		} catch (MetalnxException e) {
			logger.error("Could not instantiate ticket admin service: ", e.getMessage());

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}

		return tas;
	}

	@Override
	public TrashOperationsAO getTrashOperationsAO() throws DataGridConnectionRefusedException, MetalnxException {
		return irodsAccessObjectFactory.getTrashOperationsAO(irodsAccount);
		// return (TrashOperationsAO)
		// irodsAccessObjectFactory.getBulkFileOperationsAO(irodsAccount);
	}

	@Override
	public String findIRodsVersion() throws DataGridConnectionRefusedException {
		String version = "";

		try {
			EnvironmentalInfoAO envInfoAO = irodsAccessObjectFactory.getEnvironmentalInfoAO(irodsAccount);
			IrodsVersion iv = envInfoAO.getIRODSServerPropertiesFromIRODSServer().getIrodsVersion();
			version = String.format("%s.%s.%s", iv.getMajorAsString(), iv.getMinorAsString(), iv.getPatchAsString());
		} catch (MetalnxException e) {
			logger.error("Could not find iRODS version: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}

		return version;
	}

	@Override
	public JargonZipService getJargonZipService() throws MetalnxException {

		logger.info("getJargonZipService()");
		ZipServiceConfiguration zipServiceConfiguration = new ZipServiceConfiguration();
		return new JargonZipServiceImpl(zipServiceConfiguration, irodsAccessObjectFactory, irodsAccount);

	}

	@Override
	public BulkFileOperationsAO getBulkFileOperationsAO() throws DataGridConnectionRefusedException {
		BulkFileOperationsAO bulkFileOperationsAO = null;

		try {
			// Returning UserAO instance
			bulkFileOperationsAO = irodsAccessObjectFactory.getBulkFileOperationsAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate UserAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}

		return bulkFileOperationsAO;
	}

	@Override
	public String getCurrentUser() {
		return irodsAccount.getUserName();
	}

	@Override
	public String getCurrentUserZone() {
		return irodsAccount.getZone();
	}

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
	public UserGroupAO getGroupAO() throws DataGridConnectionRefusedException {
		try {

			// Returning UserAO instance
			return irodsAccessObjectFactory.getUserGroupAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate UserAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public CollectionAO getCollectionAO() throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAO instance
			return irodsAccessObjectFactory.getCollectionAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public FileSamplerService getFileSamplerService() throws DataGridConnectionRefusedException {
		return new FileSamplerServiceImpl(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
			throws DataGridConnectionRefusedException {

		logger.info("getCollectionAndDataObjectListAndSearchAO()");

		try {

			logger.debug("irodsAccount used:{}", irodsAccount);
			logger.debug("authScheme:{}", irodsAccount.getAuthenticationScheme());

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

			throw new DataGridConnectionRefusedException(e.getMessage());

		}

	}

	@Override
	public IRODSFileSystemAO getIRODSFileSystemAO() throws DataGridConnectionRefusedException {

		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getIRODSFileSystemAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public IRODSFileFactory getIRODSFileFactory() throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate IRODSFileFactory: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public DataTransferOperations getDataTransferOperations() throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getDataTransferOperations(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate DataTransferOperations: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}

		return null;
	}

	@Override
	public Stream2StreamAO getStream2StreamAO() throws DataGridConnectionRefusedException {

		try {

			return irodsAccessObjectFactory.getStream2StreamAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate Stream2StreamAO: ", e);

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
	public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO() throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getRemoteExecutionOfCommandsAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate RemoteExecutionOfCommandsAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public ResourceAO getResourceAO() throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getResourceAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public AvuAutocompleteService getAvuAutocompleteService() throws MetalnxException {
		// Returning AvuAutocompleteServiceImpl instance
		return new AvuAutocompleteServiceImpl(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public ShoppingCartService getShoppingCartService() throws MetalnxException {
		// TODO Auto-generated method stub

		IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
		DataCacheServiceFactory dataCacheServiceFactory = new DataCacheServiceFactoryImpl(
				irodsFileSystem.getIRODSAccessObjectFactory());
		return new ShoppingCartServiceImpl(irodsFileSystem.getIRODSAccessObjectFactory(), irodsAccount,
				dataCacheServiceFactory);
	}

	@Override
	public ZoneAO getZoneAO() throws DataGridConnectionRefusedException {
		try {

			// Returning CollectionAndDataObjectListAndSearchAO instance
			return irodsAccessObjectFactory.getZoneAO(irodsAccount);

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
	public RuleProcessingAO getRuleProcessingAO() throws DataGridConnectionRefusedException {
		try {
			// Returning RuleProcessingAO instance
			return irodsAccessObjectFactory.getRuleProcessingAO(irodsAccount);

		} catch (MetalnxException e) {
			logger.error("Could not instantiate RuleProcessingAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public void setDefaultStorageResource(String newResourceName) {
		this.irodsAccount.setDefaultStorageResource(newResourceName);
	}

	@Override
	public String getDefaultStorageResource() {
		return this.irodsAccount.getDefaultStorageResource();
	}

	@Override
	public GalleryListService getGalleryListService() throws MetalnxException {

		GalleryListService galleryListService = new GalleryListServiceImpl(irodsAccessObjectFactory, this.irodsAccount);
		return galleryListService;
	}

	@Override
	public EnvironmentalInfoAO getEnvironmentalInfoAO() throws DataGridConnectionRefusedException {
		EnvironmentalInfoAO env = null;

		try {
			env = irodsAccessObjectFactory.getEnvironmentalInfoAO(this.irodsAccount);
		} catch (MetalnxException e) {
			logger.error("Could not instantiate EnvironmentalInfoAO: ", e);

			if (e.getCause() instanceof ConnectException) {
				throw new DataGridConnectionRefusedException(e.getMessage());
			}
		}
		return env;
	}

	@Override
	public boolean isAtLeastIrods420() throws DataGridConnectionRefusedException {
		boolean isAtLeastIrods420 = false;

		try {
			EnvironmentalInfoAO env = irodsAccessObjectFactory.getEnvironmentalInfoAO(this.irodsAccount);
			if (env != null)
				isAtLeastIrods420 = env.getIRODSServerPropertiesFromIRODSServer().isAtLeastIrods420();
		} catch (MetalnxException e) {
			logger.error("Could not get environmental information from grid: {}", e.getMessage());
		}

		return isAtLeastIrods420;
	}

	@Override
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	@Override
	public boolean isActingAsAdmin() {
		return this.getUserTokenDetails().getUser().isAdmin();
	}

	public UserTokenDetails getUserTokenDetails() {
		return userTokenDetails;
	}

	public void setUserTokenDetails(UserTokenDetails userTokenDetails) {
		this.userTokenDetails = userTokenDetails;
	}

	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}

	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}

	@Override
	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
