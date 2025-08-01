package com.emc.metalnx.services.irods.utils;

import com.emc.metalnx.core.domain.exceptions.UnsupportedDataGridFeatureException;

/**
 * Represents a factory that can produce specific query generating services for
 * a given database flavor
 * 
 * @author Mike Conway - NIEHS
 *
 */
public interface SpecificQueryProviderFactory {

	/**
	 * Given iRODS {@link ClientHints} return the provider that can create
	 * appropriate sql statements
	 * 
	 * @param clientHints
	 *            {@link ClientHints}
	 * @return {@link SpecificQueryProvider} to create various sql statements needed
	 *         by metalnx
	 * @throws UnsupportedDataGridFeatureException
	 *             if the provider cannot be found
	 */
	SpecificQueryProvider instance(ClientHints clientHints) throws UnsupportedDataGridFeatureException;

	/**
	 * Given iRODS {@link icatTypeEnum} return the provider that can create
	 * appropriate sql statements
	 * 
	 * @param icatTypeEnum
	 *            {@link icatTypeEnum}
	 * @return {@link SpecificQueryProvider} to create various sql statements needed
	 *         by metalnx
	 * @throws UnsupportedDataGridFeatureException
	 *             if the provider cannot be found
	 */
	SpecificQueryProvider instance(IcatTypeEnum icatTypeEnum) throws UnsupportedDataGridFeatureException;

}