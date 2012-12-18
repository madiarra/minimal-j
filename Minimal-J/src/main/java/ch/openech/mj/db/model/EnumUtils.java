package ch.openech.mj.db.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class EnumUtils {

	public static <T extends Enum<T>> T createEnum(Class<T> clazz, String name) {
		try {
			@SuppressWarnings("rawtypes")
			Constructor con = null;
			for (Constructor<?> c : clazz.getDeclaredConstructors()) {
				if (c.getParameterTypes().length == 2) {
					con = c;
					break;
				}
			}
			if (con == null) throw new IllegalArgumentException(clazz.getName() + " must have empty constructor");

			Method[] methods = con.getClass().getDeclaredMethods();
			for (Method m : methods) {
				if (m.getName().equals("acquireConstructorAccessor")) {
					m.setAccessible(true);
					m.invoke(con, new Object[0]);
				}
			}
			Field[] fields = con.getClass().getDeclaredFields();
			Object ca = null;
			for (Field f : fields) {
				if (f.getName().equals("constructorAccessor")) {
					f.setAccessible(true);
					ca = f.get(con);
				}
			}
			Method m = ca.getClass().getMethod("newInstance",
					new Class[] { Object[].class });
			m.setAccessible(true);
			
			@SuppressWarnings("unchecked")
			T v = (T) m.invoke(ca, new Object[] { new Object[] { name,
					Integer.MAX_VALUE } });
			return v;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
	
	public static <T extends Enum<T>> T getDefault(Class<T> enumClass) {
		return EnumUtils.valueList(enumClass).get(0);
	}
	
	public static <T extends Enum<T>> List<T> valueList(Class<T> enumClass) {
		try {
			Method method = enumClass.getMethod("values");
			@SuppressWarnings("unchecked")
			T[] values = (T[]) method.invoke(null);
			return (List<T>) Arrays.asList(values);
		} catch (Exception x) {
			x.printStackTrace();
			throw new RuntimeException(x);
		}
	}
	
	public static <T extends Enum<T>> T valueOf(Class<T> clazz, String name) {
		if (name == null) return null;
		try {
			return Enum.valueOf(clazz, name);
		} catch (IllegalArgumentException iae) {
			return createEnum(clazz, name);
		}
	}
	
	public static <T extends Enum<T>> String getText(T enumElement) {
		if (enumElement == null) {
			return null;
		}
		
		String bundleName = enumElement.getClass().getName();
		try {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(bundleName);
			return resourceBundle.getString(enumElement.name());
		} catch (MissingResourceException mre) {
			return enumElement.name();
		}
	}
	
//	private static <T> Map<Class<T>, List<CodeItem<T>>> itemLists = new HashMap<Class<T>, List<CodeItem<T>>>();
	private static Map itemLists = new HashMap();

	public static <T extends Enum<T>> List<CodeItem<T>> itemList(Class<T> enumClass) {
		if (!itemLists.containsKey(enumClass)) {
			List<T> values = valueList(enumClass);
			List<CodeItem<T>> itemList = itemList(values);
			itemLists.put(enumClass, itemList);	
		}
		return (List<CodeItem<T>>) itemLists.get(enumClass);
	}
	
	public static <T extends Enum<T>> List<CodeItem<T>> itemList(List<T> values) {
		List<CodeItem<T>> itemList = new ArrayList<>(values.size());
		for (T value : values) {
			CodeItem<T> item = new CodeItem<T>(value, getText(value));
			itemList.add(item);
		}
		return itemList;
	}

	
}