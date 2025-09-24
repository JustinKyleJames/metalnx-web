 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Top level Metalnx exception
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class DataGridException extends Exception {

	private static final long serialVersionUID = 1L;
	private final int underlyingIRODSExceptionCode;

	public DataGridException(final String message) {
		super(message);
		underlyingIRODSExceptionCode = 0;
	}

	public DataGridException(final String message, final Throwable cause) {
		super(message, cause);
		underlyingIRODSExceptionCode = 0;
	}

	public DataGridException(final Throwable cause) {
		super(cause);
		underlyingIRODSExceptionCode = 0;
	}

	public DataGridException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause);
		this.underlyingIRODSExceptionCode = underlyingIRODSExceptionCode;
	}

	public DataGridException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause);
		this.underlyingIRODSExceptionCode = underlyingIRODSExceptionCode;
	}

	public DataGridException(final String message, final int underlyingIRODSExceptionCode) {
		super(message);
		this.underlyingIRODSExceptionCode = underlyingIRODSExceptionCode;
	}

	public int getUnderlyingIRODSExceptionCode() {
		return underlyingIRODSExceptionCode;
	}

}
