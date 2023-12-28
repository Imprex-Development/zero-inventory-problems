package net.imprex.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Repeatable(SimpleMigrationList.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface SimpleMigration {

	String beforeName();

	int beforeVersion() default -1;

	/**
	 * When value is defined the migration will checkout another section
	 * 
	 * @return
	 */
	Class<?> beforeSection() default SimpleMigration.class;

	/**
	 * When value is defined the migration will checkout another section
	 * 
	 * @return
	 */
	String beforeSectionName() default "";
}
