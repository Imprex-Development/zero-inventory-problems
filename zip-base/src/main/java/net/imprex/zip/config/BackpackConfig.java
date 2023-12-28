package net.imprex.zip.config;

import java.util.List;

import net.imprex.config.SimpleKey;
import net.imprex.config.SimpleSection;
import net.imprex.config.require.SimpleInteger;

@SimpleSection
public class BackpackConfig {

	@SimpleKey
	private String uniqueName;

	@SimpleKey
	private String displayName;

	@SimpleInteger(min = 1, max = 6)
	@SimpleKey
	private Integer inventoryRows;

	@SimpleInteger(min = 0)
	@SimpleKey
	private Integer customModelData;

	@SimpleKey
	private String texture;

	@SimpleKey
	private List<String> lore;

	@SimpleKey
	@SimpleInteger(min = 0, max = 64)
	private Integer maxLoreCount = 10;

	@SimpleKey
	private RecipeConfig recipe;

	BackpackConfig() {
	}

	BackpackConfig(String uniqueName, String displayName, Integer inventoryRows, Integer customModelData,
			String texture, List<String> lore, RecipeConfig recipe) {
		this.uniqueName = uniqueName;
		this.displayName = displayName;
		this.inventoryRows = inventoryRows;
		this.customModelData = customModelData;
		this.texture = texture;
		this.lore = lore;
		this.recipe = recipe;
	}

	public String getUniqueName() {
		return this.uniqueName;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	public Integer getInventoryRows() {
		return this.inventoryRows;
	}

	public Integer getCustomModelData() {
		return this.customModelData;
	}

	public String getTexture() {
		return this.texture;
	}

	public List<String> getLore() {
		return this.lore;
	}

	public int getMaxLoreCount() {
		return this.maxLoreCount;
	}

	public RecipeConfig getRecipe() {
		return this.recipe;
	}
}
