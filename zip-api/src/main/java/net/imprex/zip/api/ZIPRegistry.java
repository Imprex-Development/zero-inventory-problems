package net.imprex.zip.api;

import java.util.Collection;

import org.bukkit.entity.Player;

public interface ZIPRegistry {

	ZIPBackpackType getTypeByName(String name);

	default void discoverRecipes(Player player) {
		this.discoverRecipes(player, false);
	}

	void discoverRecipes(Player player, boolean force);

	Collection<ZIPBackpackType> getType();
}
