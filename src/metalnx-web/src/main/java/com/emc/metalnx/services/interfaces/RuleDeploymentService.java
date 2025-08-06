 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import org.irods.irods4j.low_level.api.MetalnxException;
import org.springframework.web.multipart.MultipartFile;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

public interface RuleDeploymentService {

	/**
	 * Deploys a rule into the grid
	 * 
	 * @param file
	 *            rule file to deploy
	 * @throws MetalnxException
	 */
	void deployRule(MultipartFile file) throws DataGridException, MetalnxException;

	/**
	 * Finds the rule cache directory in the grid
	 * 
	 * @return String representing the rule cache directory in the grid
	 */
	String getRuleCachePath();

	/**
	 * Creates the rule cache directory in the grid (/<zone>/.rulecache)
	 * 
	 * @throws DataGridException
	 *             if the creation of the cache directory fails
	 */
	void createRuleCache() throws DataGridException;

	/**
	 * Checks whether or not the rule cache directory already exists in the grid.
	 * 
	 * @return True, if /<zone>/.rulecache path exists. False, otherwise.
	 * @throws MetalnxException
	 * @throws DataGridConnectionRefusedException
	 */
	boolean ruleCacheExists() throws DataGridConnectionRefusedException, MetalnxException;
}
