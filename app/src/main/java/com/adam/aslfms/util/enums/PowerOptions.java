/**
 * 
 */
package com.adam.aslfms.util.enums;

public enum PowerOptions {
	BATTERY(
			"", new AdvancedOptions[] { AdvancedOptions.STANDARD, AdvancedOptions.BATTERY_SAVING,
					AdvancedOptions.CUSTOM }),
	PLUGGED_IN(
			"_plugged", new AdvancedOptions[] { AdvancedOptions.SAME_AS_BATTERY, AdvancedOptions.STANDARD,
					AdvancedOptions.CUSTOM });

	private final String settingsPath;
	private final AdvancedOptions[] applicableOptions;

	private PowerOptions(String settingsPath, AdvancedOptions[] applicableOptions) {
		this.settingsPath = settingsPath;
		this.applicableOptions = applicableOptions;
	}

	public String getSettingsPath() {
		return settingsPath;
	}

	public AdvancedOptions[] getApplicableOptions() {
		return applicableOptions;
	}

}