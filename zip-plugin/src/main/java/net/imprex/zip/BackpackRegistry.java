package net.imprex.zip;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;

import net.imprex.zip.api.ZIPBackpackType;
import net.imprex.zip.api.ZIPRegistry;
import net.imprex.zip.config.BackpackConfig;
import net.imprex.zip.config.BackpackTypeConfig;
import net.imprex.zip.config.BackpackTypeListConfig;
import net.imprex.zip.util.ZIPLogger;

public class BackpackRegistry implements ZIPRegistry {

	private final BackpackPlugin plugin;

	private final Map<String, BackpackType> backpackType = new HashMap<>();

	public BackpackRegistry(BackpackPlugin plugin) {
		this.plugin = plugin;
	}

	public void register() {
		BackpackConfig configManager = this.plugin.getBackpackConfig();
		BackpackTypeListConfig typeListConfig = configManager.typeList();

		for (BackpackTypeConfig typeConfig : typeListConfig.type) {
			BackpackType backpackType = new BackpackType(this.plugin, typeConfig);
			this.backpackType.put(typeConfig.uniqueName.toLowerCase(Locale.ROOT), backpackType);

			BackpackRecipe recipe = backpackType.getRecipe();
			Bukkit.addRecipe(recipe);
		}
	}

	public void unregister() {
		for (BackpackType type : this.backpackType.values()) {
			try {
				Bukkit.removeRecipe(type.getRecipe().getKey());
			} catch (Exception e) {
				ZIPLogger.error("Unable to unregister recipes", e);
			}
		}
		this.backpackType.clear();
	}

	@Override
	public BackpackType getTypeByName(String name) {
		return this.backpackType.get(name.toLowerCase(Locale.ROOT));
	}

	@Override
	public Collection<ZIPBackpackType> getType() {
		return Collections.unmodifiableCollection(this.backpackType.values());
	}
}