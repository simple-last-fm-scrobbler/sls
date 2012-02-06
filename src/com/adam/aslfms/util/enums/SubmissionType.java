/**
 * 
 */
package com.adam.aslfms.util.enums;

public enum SubmissionType {
	SCROBBLE(
			"status_last_scrobble", "status_nscrobbles"),
	NP(
			"status_last_np", "status_nnps");

	private final String lastPrefix;
	private final String numberOfPrefix;

	SubmissionType(String lastPrefix, String numberOfPrefix) {
		this.lastPrefix = lastPrefix;
		this.numberOfPrefix = numberOfPrefix;
	}

	public String getLastPrefix() {
		return lastPrefix;
	}

	public String getNumberOfPrefix() {
		return numberOfPrefix;
	}
}