package net.imprex.config;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(TYPE)
public @interface SimpleSectionRoot {

	// Default file name can include file suffix
	String name();

	// Default configuration directory
	String directory() default "";

	String fileSuffix() default ".yml";

	// Plugin.jar default configuration location. Will be copied from the jar into the file system when no configuration file exists!
	String defaultConfigPath() default "";

	// Currently configuration version
	int version();
}