package net.imprex.zip.config.v3.example;

import java.util.List;

import net.imprex.zip.config.v3.SimpleComment;
import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleMigration;
import net.imprex.zip.config.v3.SimpleSection;
import net.imprex.zip.config.v3.SimpleUnused;
import net.imprex.zip.config.v3.require.SimpleInteger;
import net.imprex.zip.config.v3.require.SimpleString;

@SimpleSection
public class GeneralConfig {

	@SimpleMigration(beforeName = "checkingForNewerVersions", beforeVersion = 1, beforeSection = ExampleConfig.class)
	@SimpleMigration(beforeName = "checkingForNewUpdates", beforeVersion = 2, beforeSectionName = "default")
	@SimpleMigration(beforeName = "checkingForNewUpdates", beforeVersion = 3)
	@SimpleComment("Change this value to true to enable update notifications.")
	@SimpleKey("checkForUpdates")
	public Boolean checkForUpdates = true;

	@SimpleComment("Change a test value to someting else")
	@SimpleComment(" ")
	@SimpleComment("Just added some space")
	@SimpleComment(" ")
	@SimpleInteger(min = 10, max = 100, defaultValue = 60)
	@SimpleKey("testCountKey xD")
	public int testCount;

	@SimpleComment("Set your username")
	@SimpleString(regex = "[A-Z]*", defaultValue = "INGRIM")
	@SimpleKey("username")
	public String username;

	@SimpleUnused
	public String unsuedValue;

	@SimpleKey
	public List<String> randomList = List.of("Just", "Some", "Random", "Input");
}