package net.imprex.zip.api;

import java.util.Collection;

public interface ZIPRegistry {

	ZIPBackpackType getTypeByName(String name);

	Collection<ZIPBackpackType> getType();
}
