package net.imprex.zip.config.v3.example;

import net.imprex.zip.config.v3.SimpleComment;
import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleSectionRoot;

@SimpleSectionRoot(name = "example", version = 4)
public class ExampleConfig {

	@SimpleComment("Just some testing")
	@SimpleKey("testBoolean")
	public Boolean tested;

	@SimpleKey(value = "general")
	public GeneralConfig general;
}