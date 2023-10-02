package net.imprex.zip.config.v3.require;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface SimpleInteger {

	int defaultValue() default 0;

	int min() default Integer.MIN_VALUE;
	int max() default Integer.MAX_VALUE;
}