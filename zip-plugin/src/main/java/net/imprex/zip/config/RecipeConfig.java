package net.imprex.zip.config;

import java.util.Map;

import org.bukkit.Material;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleSection;
import net.imprex.zip.config.v3.require.SimpleString;

@SimpleSection
public class RecipeConfig {

	@SimpleKey
	public Boolean discover = true;

	@SimpleKey
	public String group;

	@SimpleString(regex = "[a-zA-Z]{1,3}")
	@SimpleKey
	public String patternOne;

	@SimpleString(regex = "[a-zA-Z]{1,3}")
	@SimpleKey
	public String patternTwo;

	@SimpleString(regex = "[a-zA-Z]{1,3}")
	@SimpleKey
	public String patternThree;

	@SimpleKey
	public Map<String, Material> patternMapping;

	RecipeConfig() {
	}

	RecipeConfig(Boolean discover, String group, String patternOne, String patternTwo, String patternThree,
			Map<String, Material> patternMapping) {
		this.discover = discover;
		this.group = group;
		this.patternOne = patternOne;
		this.patternTwo = patternTwo;
		this.patternThree = patternThree;
		this.patternMapping = patternMapping;
	}
}
