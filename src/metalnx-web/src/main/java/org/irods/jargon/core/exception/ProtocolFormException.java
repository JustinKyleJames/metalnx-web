package org.irods.jargon.core.exception;

/**
 * The data being sent for the protocol operation is inconsistent, this is
 * amplified by the detail error message.
 *
 * @author Mike Conway - DICE (www.irods.org)
 *
 */
public class ProtocolFormException extends ProtocolException {

	private static final long serialVersionUID = -810097979277699143L;

	public ProtocolFormException(final String message) {
		super(message);
	}

	public ProtocolFormException(final Throwable cause) {
		super(cause);
	}

	public ProtocolFormException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
