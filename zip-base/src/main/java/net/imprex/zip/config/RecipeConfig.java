package net.imprex.zip.config;

import java.util.List;

import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSection;
import net.imprex.config.require.SimpleString;

@SimpleSection
public class RecipeConfig {

	@SimpleKey
	private Boolean discover = true;

	@SimpleKey
	private String group;

	@SimpleKey
	@SimpleString(regex = "[a-zA-Z]{1,3}")
	private String patternOne;

	@SimpleKey
	@SimpleString(regex = "[a-zA-Z]{1,3}")
	private String patternTwo;

	@SimpleKey
	@SimpleString(regex = "[a-zA-Z]{1,3}")
	private String patternThree;

	@SimpleKey
	private List<RecipePatternConfig> pattern;

	RecipeConfig() {
	}

	RecipeConfig(Boolean discover, String group, String patternOne, String patternTwo, String patternThree,
			List<RecipePatternConfig> pattern) {
		this.discover = discover;
		this.group = group;
		this.patternOne = patternOne;
		this.patternTwo = patternTwo;
		this.patternThree = patternThree;
		this.pattern = pattern;
	}

	public Boolean getDiscover() {
		return this.discover;
	}

	public String getGroup() {
		return this.group;
	}

	public String getPatternOne() {
		return this.patternOne;
	}

	public String getPatternTwo() {
		return this.patternTwo;
	}

	public String getPatternThree() {
		return this.patternThree;
	}

	public List<RecipePatternConfig> getPattern() {
		return this.pattern;
	}
}
