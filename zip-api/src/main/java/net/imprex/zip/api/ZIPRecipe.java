package net.imprex.zip.api;

import org.bukkit.Keyed;
import org.bukkit.inventory.Recipe;

public interface ZIPRecipe extends Recipe, Keyed {

	boolean canDiscover();
}
