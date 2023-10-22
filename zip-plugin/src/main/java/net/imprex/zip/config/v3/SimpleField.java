package net.imprex.zip.config.v3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imprex.zip.util.ZIPLogger;

public class SimpleField<Type> {

	public static SimpleField<?>[] getFields(Class<?> fieldClass) {
		return Arrays.stream(fieldClass.getDeclaredFields())
				.filter(field -> field.getAnnotation(SimpleUnused.class) != null)
				.map(SimpleField::new)
				.toArray(SimpleField[]::new);
	}

	private final Field field;
	private final Class<?> genericClass;

	public SimpleField(Field field) {
		this.field = field;

		ParameterizedType parameterizedType = (ParameterizedType) field.getType().getGenericSuperclass();
		this.genericClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];

		this.setAccessible(true);
	}

	public void setAccessible(Object instance) {
		if (!this.field.canAccess(instance)) {
			this.field.setAccessible(true);
		}
	}

	public void validateValue(Object value) {
		Class<?> valueClass = value.getClass();
		if (!valueClass.isAssignableFrom(this.genericClass)) {
			throw new IllegalArgumentException(String.format("%s is not type of %s",
					valueClass.getSimpleName(),
					this.genericClass.getSimpleName()
					));
		}
	}

	@SuppressWarnings("unchecked")
	public Type getField(Object instance) {
		try {
			return (Type) this.field.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ZIPLogger.error("Unable to get field value!", e);
			return null;
		}
	}

	public void setField(Object instance, Object value) {
		this.validateValue(value);

		try {
			this.field.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			ZIPLogger.error("Unable to set field value!", e);
		}
	}

	public List<String> getComments(boolean inlineComments) {
		List<String> comments = new ArrayList<>();

		SimpleCommentList commentList = field.getAnnotation(SimpleCommentList.class);
		if (commentList != null) {
			for (SimpleComment comment : commentList.value()) {
				if (comment.inline() && inlineComments) {
					comments.add(comment.value());
				} else if (!comment.inline() && !inlineComments) {
					comments.add(comment.value());
				}
			}
		}

		SimpleComment comment = field.getAnnotation(SimpleComment.class);
		if (comment != null) {
			if (comment.inline() && inlineComments) {
				comments.add(comment.value());
			} else if (!comment.inline() && !inlineComments) {
				comments.add(comment.value());
			}
		}

		return comments;
	}

	public <Require extends Annotation> Require getRequire(SimpleTranslator<?, Require> translator) {
		Class<Require> require = translator.require();
		if (require != null) {
			return this.field.getAnnotation(require);
		}
		return null;
	}

	public SimpleKey getKey() {
		return this.field.getAnnotation(SimpleKey.class);
	}

	public boolean isSection() {
		Class<?> configClass = this.getType();
		return configClass.getAnnotation(SimpleSection.class) != null
				|| configClass.getAnnotation(SimpleSectionRoot.class) != null;
	}

	public boolean isChildSection() {
		return this.getType().getAnnotation(SimpleSection.class) != null;
	}

	public Class<?> getType() {
		return this.field.getType();
	}

	public String getFieldName() {
		return this.field.getName();
	}
}
