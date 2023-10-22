package net.imprex.zip.config.v3.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.imprex.zip.config.v3.SimpleComment;
import net.imprex.zip.config.v3.SimpleKey;
import net.imprex.zip.config.v3.SimpleSectionRoot;

@SimpleSectionRoot(name = "example", version = 4)
public class ExampleConfig {

	private static final Map<String, List<String>> EXAMPLE_MAP = new HashMap<>();

	static {
		EXAMPLE_MAP.put("a", List.of("java", "html", "csharp"));
		EXAMPLE_MAP.put("b", List.of("lua", "python", "scss"));
		EXAMPLE_MAP.put("c", List.of("html", "css"));
	}

	@SimpleComment(value = "Just some testing", inline = true)
	@SimpleKey("testBoolean")
	public Boolean tested;

	@SimpleKey
	public GeneralConfig general;

	@SimpleKey
	public Map<String, List<String>> exampleMap = EXAMPLE_MAP;
}