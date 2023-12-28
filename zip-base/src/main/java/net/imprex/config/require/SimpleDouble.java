package net.imprex.config.require;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(FIELD)
public @interface SimpleDouble {

	double defaultValue() default 0;

	double min() default Double.MIN_VALUE;
	double max() default Double.MAX_VALUE;
}