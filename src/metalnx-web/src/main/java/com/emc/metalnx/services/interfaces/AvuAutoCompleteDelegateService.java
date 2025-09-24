package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridException;

public interface AvuAutoCompleteDelegateService {

	public String getMetadataAttrs(final String prefix, final int offset, final AvuTypeEnum avuTypeEnum)
			throws DataGridException;
	public String getAvailableValues(final String forAttribute, final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum)
			throws DataGridException;
}
