package net.imprex.zip.config;

import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleSection;
import net.imprex.zip.config.v3.require.SimpleInteger;

@SimpleSection
public class BackpackConfig {

	static final Map<String, BackpackConfig> DEFAULT = Map.of(
			"big", new BackpackConfig(
					"big",
					"&7Big &eBackpack",
					6,
					3,
					"ewogICJ0aW1lc3RhbXAiIDogMTYzMzg2MjQwNTg4NSwKICAicHJvZmlsZUlkIiA6ICI1NjY3NWIyMjMyZjA0ZWUwODkxNzllOWM5MjA2Y2ZlOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaGVJbmRyYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kYTMzMTY5YjcyY2Y4OTE4YjgyYzViZDI3Y2JhOWVlNmMwZWI4OTE2NDA0MDQ4MGJiMjdmODUzNGUwZmE1ODA3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=",
					List.of("&aA Big Backpack"),
					new RecipeConfig(
							true,
							"",
							"LCL",
							"ECE",
							"DCD", Map.of(
									"L", Material.LEATHER,
									"E", Material.LEAD,
									"C", Material.CHEST,
									"D", Material.DIAMOND))),
			"medium", new BackpackConfig(
					"medium",
					"&7Medium &eBackpack",
					3,
					2,
					"ewogICJ0aW1lc3RhbXAiIDogMTY3NDM0NDQ0MzY4NywKICAicHJvZmlsZUlkIiA6ICIxMzdmMjg3MjUwOTE0ZmI4YjA0ZTYwYjg4MWUwZWE2YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJub3JtYWxpc2luZyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lZjhlZDY4NWIzYzNjYWI4NDM1ZDBmNmIwZDJkZGI0NmRmYTNhYzk2ZmUyZjlhNmQ3NDhmNzExOTFjMGI1MjJiIgogICAgfQogIH0KfQ==",
					List.of("&aA Medium Backpack"),
					new RecipeConfig(
							true,
							"",
							"LCL",
							"ECE",
							"ICI", Map.of(
									"L", Material.LEATHER,
									"E", Material.LEAD,
									"C", Material.CHEST,
									"S", Material.STICK,
									"I", Material.IRON_INGOT))),
			"small", new BackpackConfig(
					"small",
					"&7Small &eBackpack",
					1,
					1,
					"ewogICJ0aW1lc3RhbXAiIDogMTYxMzM4Nzk2NDUxNSwKICAicHJvZmlsZUlkIiA6ICJkZTU3MWExMDJjYjg0ODgwOGZlN2M5ZjQ0OTZlY2RhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTWluZXNraW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlMTg3MTQ1ZWU3MzJlN2MwMDkwNWE5YzE2ZWQxZTQzYmE4OGQ1NjI0YTZmNGFmODI5ZjEwNDUzZmNjOTE2NSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9",
					List.of("&aA Small Backpack"),
					new RecipeConfig(
							true,
							"",
							"LSL",
							"ECE",
							"LLL", Map.of(
									"L", Material.LEATHER,
									"E", Material.LEAD,
									"C", Material.CHEST,
									"S", Material.STICK))));

	@SimpleKey
	public String uniqueName;

	@SimpleKey
	public String displayName;

	@SimpleInteger(min = 1, max = 6)
	@SimpleKey
	public Integer inventoryRows;

	@SimpleInteger(min = 0)
	@SimpleKey
	public Integer customModelData;

	@SimpleKey
	public String texture;

	@SimpleKey
	public List<String> lore;

	@SimpleKey
	public RecipeConfig recipe;

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

}
