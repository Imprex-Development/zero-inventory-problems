package net.imprex.zip.config.v3.example;

import java.nio.file.Path;

import net.imprex.zip.config.v3.SimpleConfig;

public class SimpleTestInstance {

	public static void main(String[] args) throws Exception {
		SimpleConfig<ExampleConfig> config = new SimpleConfig<>(Path.of("./simple"), ExampleConfig.class);
		ExampleConfig loaded = config.getOrDeserializeConfig();
		System.out.println("tested: " + loaded.tested);
		System.out.println("general.username: " + loaded.general.username);
		System.out.println("general.testCount: " + loaded.general.testCount);
	}
}