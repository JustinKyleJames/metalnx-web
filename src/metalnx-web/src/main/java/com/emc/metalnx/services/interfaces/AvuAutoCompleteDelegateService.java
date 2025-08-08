package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.MetalnxException;

public interface AvuAutoCompleteDelegateService {

	public String getMetadataAttrs(final String prefix, final int offset, final AvuTypeEnum avuTypeEnum)
			throws MetalnxException;
	public String getAvailableValues(final String forAttribute, final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum)
			throws MetalnxException;
}
