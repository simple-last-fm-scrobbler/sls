/**
 *  This file is part of Simple Last.fm Scrobbler.
 *
 *  Simple Last.fm Scrobbler is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Simple Last.fm Scrobbler is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Simple Last.fm Scrobbler.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  See http://code.google.com/p/a-simple-lastfm-scrobbler/ for the latest version.
 */

package com.adam.aslfms.util;

/**
 * Status is a "namespace class" that contains the definitions of various status
 * types. This class is not meant to be instantiated.
 * 
 * @author tgwizard
 * 
 */
public class AuthStatus {
	public static final int AUTHSTATUS_NOAUTH = 0;
	public static final int AUTHSTATUS_UPDATING = 1;
	public static final int AUTHSTATUS_BADAUTH = 2;
	public static final int AUTHSTATUS_FAILED = 3;
	public static final int AUTHSTATUS_RETRYLATER = 4;
	public static final int AUTHSTATUS_OK = 5;
	public static final int AUTHSTATUS_CLIENTBANNED = 6;
	public static final int AUTHSTATUS_NETWORKUNFIT = 7;

	public static class StatusException extends Exception {
		private static final long serialVersionUID = 7204759787220898684L;

		public StatusException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class ClientBannedException extends StatusException {
		private static final long serialVersionUID = 3452632996181451232L;

		public ClientBannedException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class BadAuthException extends StatusException {
		private static final long serialVersionUID = 1512908282760585728L;

		public BadAuthException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class BadSessionException extends StatusException {
		private static final long serialVersionUID = -1360166232828945639L;

		public BadSessionException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class TemporaryFailureException extends StatusException {
		private static final long serialVersionUID = -2810766166898051179L;

		public TemporaryFailureException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class UnknownResponseException extends StatusException {
		private static final long serialVersionUID = 7351097754868391707L;

		public UnknownResponseException(String detailMessage) {
			super(detailMessage);
		}
	}

	/**
	 * This class is not meant to be instantiated.
	 */
	private AuthStatus() {
	}
}
