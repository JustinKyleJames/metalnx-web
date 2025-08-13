/**
 * 
 */
package org.irodsext.dataprofiler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emc.metalnx.core.connection.IRODSAccount;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.MetalnxException;

/**
 * IRODS-EXT base implementation of a data profiler that can summarize a data
 * object or collection
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class IrodsextDataProfilerService extends DataProfilerService {

	/**
	 * logged in user identity
	 */
	private DataGridUser dataGridUser;

	public static final Logger log = LogManager.getLogger(IrodsextDataProfilerService.class);

	public IrodsextDataProfilerService(DataProfilerSettings defaultDataProfilerSettings,
			IRODSAccessObjectFactory irodsAccessObjectFactory, IRODSAccount irodsAccount) {
		super(defaultDataProfilerSettings, irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	protected void addStarringDataToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
	}

	@Override
	protected void addStarringDataToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
	}

	@Override
	protected void addTaggingAndCommentsToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("tagging not yet implemented");

	}

	@Override
	protected void addTaggingAndCommentsToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("tagging not yet implemented");

	}

	@Override
	protected void addSharingToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("sharing not yet implemented");

	}

	@Override
	protected void addSharingToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("sharing not yet implemented");

	}

	@Override
	protected void addTicketsToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("tickets not yet implemented");

	}

	@Override
	protected void addTicketsToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("tickets not yet implemented");

	}

	@Override
	protected void addMetadataTemplatesToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("templates not yet implemented");

	}

	@Override
	protected void addMetadataTemplatesToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws MetalnxException {
		log.warn("templates not yet implemented");

	}


	public DataGridUser getDataGridUser() {
		return dataGridUser;
	}

	public void setDataGridUser(DataGridUser dataGridUser) {
		this.dataGridUser = dataGridUser;
	}

}
