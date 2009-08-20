package com.adam.aslfms;

public class Status {
	public static final int AUTHSTATUS_NOAUTH = 0;
	public static final int AUTHSTATUS_UPDATING = 1;
	public static final int AUTHSTATUS_BADAUTH = 2;
	public static final int AUTHSTATUS_FAILED = 3;
	public static final int AUTHSTATUS_RETRYLATER = 4;
	public static final int AUTHSTATUS_OK = 5;

	public static class StatusException extends Exception {
		private static final long serialVersionUID = 7204759787220898684L;

		public StatusException() {
			super();
		}

		public StatusException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class BadAuthException extends StatusException {
		private static final long serialVersionUID = 1512908282760585728L;

		public BadAuthException() {
			super();
		}

		public BadAuthException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class BadSessionException extends StatusException {
		private static final long serialVersionUID = -1360166232828945639L;

		public BadSessionException() {
			super();
		}

		public BadSessionException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class TemporaryFailureException extends StatusException {
		private static final long serialVersionUID = -2810766166898051179L;

		public TemporaryFailureException() {
			super();
		}

		public TemporaryFailureException(String detailMessage) {
			super(detailMessage);
		}
	}

	public static class FailureException extends StatusException {
		private static final long serialVersionUID = 7351097754868391707L;

		public FailureException() {
			super();
		}

		public FailureException(String detailMessage) {
			super(detailMessage);
		}
	}

}
