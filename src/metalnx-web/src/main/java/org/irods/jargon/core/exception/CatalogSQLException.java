/**
 *
 */
package org.irods.jargon.core.exception;

/**
 * -806000 error in iRODS caused by catalog sql exception.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class CatalogSQLException extends InternalIrodsOperationException {

	private static final long serialVersionUID = 6150226404159251373L;

	public CatalogSQLException(final String message) {
		super(message);
	}

	public CatalogSQLException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CatalogSQLException(final Throwable cause) {
		super(cause);
	}

	public CatalogSQLException(final String message, final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public CatalogSQLException(final Throwable cause, final int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public CatalogSQLException(final String message, final int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
