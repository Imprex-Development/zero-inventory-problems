package net.imprex.zip.config.v3;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Repeatable(SimpleCommentList.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface SimpleComment {

	String value();
}
