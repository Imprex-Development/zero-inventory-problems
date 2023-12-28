package net.imprex.zip.config;

import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSection;
import net.imprex.config.require.SimpleInteger;

@SimpleSection
public class RecipePatternConfig {

	@SimpleKey
	private String key;

	@SimpleKey
	private String material;

	@SimpleKey
	@SimpleInteger(min = 0)
	private Integer customModelData;

	@SimpleKey
	@SimpleInteger(min = 0)
	private Integer durability;

	public RecipePatternConfig() {
	}

	public RecipePatternConfig(String key, String material, Integer customModelData) {
		this.key = key;
		this.material = material;
		this.customModelData = customModelData;
	}

	public String getKey() {
		return this.key;
	}

	public String getMaterial() {
		return this.material;
	}

	public Integer getCustomModelData() {
		return this.customModelData;
	}
}
