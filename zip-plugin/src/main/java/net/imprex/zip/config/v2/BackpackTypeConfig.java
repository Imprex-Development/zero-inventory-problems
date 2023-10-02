package net.imprex.zip.config.v2;

import java.util.List;

@ConfigSection(name = "backpack")
public class BackpackTypeConfig {

	@ConfigEntry(name = "uniqueName")
	public String uniqueName;

	@ConfigEntry(name = "inventoryRows")
	public Integer inventoryRows;

	@ConfigEntry(name = "displayName")
	public String displayName;

	@ConfigEntry(name = "customModelData")
	public Integer customModelData;

	@ConfigEntry(name = "texture")
	public String texture;

	@ConfigEntry(name = "lore")
	public List<String> lore;

//	public RecipeConfig recipe;
}
