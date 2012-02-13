package ch.openech.mj.db.model;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import ch.openech.mj.db.model.annotation.Boolean;
import ch.openech.mj.db.model.annotation.Date;
import ch.openech.mj.db.model.annotation.FormatName;
import ch.openech.mj.db.model.annotation.Int;
import ch.openech.mj.db.model.annotation.NonNegative;
import ch.openech.mj.db.model.annotation.Varchar;
import ch.openech.mj.util.StringUtils;

public class Formats {

	private static Formats instance = new Formats();
	
	private Map<String, Format> formats = new HashMap<String, Format>();
	
	private Formats() {
		// nothing to do
	}
	
	public static Formats getInstance() {
		return instance;
	}
	
	//

	public void register(String name, Format format) {
		formats.put(name, format);
	}
	
	public void registerCode(String name, ResourceBundle resourceBundle) {
		registerCode(name, resourceBundle, name);
	}

	public void registerCode(String name, ResourceBundle resourceBundle, String prefix) {
		formats.put(name, new Code(resourceBundle, prefix));
	}

	/**
	 * Registers an internal code (based on a enumeration class) under the name
	 * of the clazz
	 * 
	 * @param clazz
	 */
	public <T extends Enum<? extends CodeValue>> void registerCode(Class<T> clazz) {
		String name = (StringUtils.lowerFirstChar(clazz.getSimpleName()));
		registerCode(name, clazz);
	}

	public <T extends Enum<? extends CodeValue>> void registerCode(String name, Class<T> clazz) {
		formats.put(name, new InternalCode(clazz));
	}

	public Format getFormat(AccessorInterface accessor) {
		Format format = getAnnotatedFormat(accessor);
		if (format != null) {
			return format;
		} else {
			String formatName = getFormatName(accessor);
			return getFormat(formatName);
		}
	}

	private Format getFormat(String name) {
		return formats.get(name);
	}

	private String getFormatName(AccessorInterface accessor) {
		FormatName type = accessor.getAnnotation(FormatName.class);
		if (type != null) {
			return type.value();
		} else {
			return accessor.getName();
		}
	}
	
	private Format getAnnotatedFormat(AccessorInterface accessor) {
		Varchar varchar = accessor.getAnnotation(Varchar.class);
		if (varchar != null) {
			return new PlainFormat(varchar.value());
		}
		Int jnt = accessor.getAnnotation(Int.class);
		if (jnt != null) {
			boolean nonNegative = accessor.getAnnotation(NonNegative.class) != null;
			return new IntegerFormat(Integer.class, jnt.value(), nonNegative);
		}
		Date date = accessor.getAnnotation(Date.class);
		if (date != null) {
			return new DateFormat(date.partialAllowed());
		}
		Boolean bulean = accessor.getAnnotation(Boolean.class);
		if (bulean != null) {
			return new BooleanFormat(bulean.nullable());
		}
		return null;
	}

}
