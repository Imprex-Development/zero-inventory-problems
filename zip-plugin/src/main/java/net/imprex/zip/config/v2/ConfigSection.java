package net.imprex.zip.config.v2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigSection {

	/**
	 * Declared section name inside of the configuration file.
	 */
	String name();

	/**
	 * Will using the given configuration file without going into the defined
	 * section.
	 */
	boolean rootSection() default false;

	/**
	 * When child section is true it can only be loaded inside of an non child
	 * section.
	 */
	boolean childSection() default false;
}
