package com.emc.metalnx.services.interfaces;

public interface AvuAutoCompleteDelegateService {

	public String getMetadataAttrs(final String prefix, final int offset, final AvuTypeEnum avuTypeEnum)
			throws JargonException;
	public String getAvailableValues(final String forAttribute, final String prefix, final int offset,
			final AvuTypeEnum avuTypeEnum)
			throws JargonException;
}
