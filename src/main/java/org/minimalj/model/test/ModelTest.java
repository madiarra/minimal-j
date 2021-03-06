package org.minimalj.model.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.minimalj.application.DevMode;
import org.minimalj.model.EnumUtils;
import org.minimalj.model.View;
import org.minimalj.model.annotation.AnnotationUtil;
import org.minimalj.model.properties.FlatProperties;
import org.minimalj.model.properties.Properties;
import org.minimalj.model.properties.PropertyInterface;
import org.minimalj.util.Codes;
import org.minimalj.util.FieldUtils;
import org.minimalj.util.GenericUtils;
import org.minimalj.util.IdUtils;
import org.minimalj.util.StringUtils;
import org.minimalj.util.resources.Resources;

/**
 * Test some restricitions on model classes.<p>
 * 
 * These tests are called by JUnit tests but also by Persistence.
 * They are fast and its better to see problems at startup of an application.
 */
public class ModelTest {

	private final Collection<Class<?>> mainClasses;
	private Set<Class<?>> testedClasses = new HashSet<Class<?>>();
	
	private final List<String> problems = new ArrayList<String>();
	private final SortedSet<String> missingResources = new TreeSet<String>();
	
	public ModelTest(Class<?>... modelClasses) {
		this(Arrays.asList(modelClasses));
	}
	
	public ModelTest(Collection<Class<?>> modelClasses) {
		this.mainClasses = modelClasses;

		for (Class<?> clazz : modelClasses) {
			testClass(clazz);
		}
		if (DevMode.isActive()) {
			reportMissingResources();
		}
	}
	
	public List<String> getProblems() {
		return problems;
	}
	
	public boolean isValid() {
		return problems.isEmpty();
	}

	private void testClass(Class<?> clazz) {
		if (!testedClasses.contains(clazz)) {
			testedClasses.add(clazz);
			testName(clazz);
			testNoSuperclass(clazz);
			testId(clazz);
			testVersion(clazz);
			testConstructor(clazz);
			testFields(clazz);
			if (!IdUtils.hasId(clazz)) {
				testNoListFields(clazz);
			}
			if (DevMode.isActive()) {
				testResources(clazz);
			}
		}
	}

	private void testInlineClass(Class<?> clazz) {
		testName(clazz);
		testNoSuperclass(clazz);
		testFields(clazz);
		// TODO testNoInlineRecursion(clazz);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void testConstructor(Class<?> clazz)  {
		if (Enum.class.isAssignableFrom(clazz)) {
			try {
				EnumUtils.createEnum((Class<Enum>) clazz, "Test");
			} catch (Exception e) {
				problems.add("Not possible to create runtime instance of enum " + clazz.getName() + ". Possibly there is no empty constructor");
			}
		} else {
			try {
				Constructor<?> constructor = clazz.getConstructor();
				if (!Modifier.isPublic(constructor.getModifiers())) {
					problems.add("Constructor of " + clazz.getName() + " not public");
				}
			} catch (NoSuchMethodException e) {
				problems.add(clazz.getName() + " has no public empty constructor");
			}
		}
	}

	private boolean isMain(Class<?> clazz) {
		return mainClasses.contains(clazz);
	}
	
	private void testNoSuperclass(Class<?> clazz) {
		if (clazz.getSuperclass() != Object.class && (clazz.getSuperclass() != Enum.class || isMain(clazz))) {
			problems.add(clazz.getName() + ": Domain classes must not extends other classes");
		}
	}
				
	private void testId(Class<?> clazz) {
		try {
			PropertyInterface property = FlatProperties.getProperty(clazz, "id");
			if (Codes.isCode(clazz)) {
				if (!FieldUtils.isAllowedCodeId(property.getClazz())) {
					problems.add(clazz.getName() + ": Code id must be of Integer, String or Object");
				}
			} else {
				if (property.getClazz() != Object.class) {
					problems.add(clazz.getName() + ": Id must be Object");
				}				
			}
		} catch (IllegalArgumentException e) {
			if (Codes.isCode(clazz)) {
				problems.add(clazz.getName() + ": Code classes must have an id field of Integer, String or Object");
			} else if (isMain(clazz)) {
				problems.add(clazz.getName() + ": Domain classes must have an id field of type object");
			}
		}
	}

	private void testVersion(Class<?> clazz) {
		try {
			Field fieldVersion = clazz.getField("version");
			if (isMain(clazz) && !Codes.isCode(clazz)) {
				if (fieldVersion.getType() == Integer.class) {
					problems.add(clazz.getName() + ": Domain classes version must be of primitiv type int");
				}
				if (!FieldUtils.isPublic(fieldVersion)) {
					problems.add(clazz.getName() + ": field version must be public");
				}
			} else {
				problems.add(clazz.getName() + ": Only main entities are allowed to have an version field");
			}
		} catch (NoSuchFieldException e) {
			// thats ok, version is not mandatory
		} catch (SecurityException e) {
			problems.add(clazz.getName() + " makes SecurityException with the id field");
		}
	}
	
	private void testFields(Class<?> clazz) {
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			testField(field);
		}
	}

	private void testField(Field field) {
		if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field) && !StringUtils.equals(field.getName(), "id", "version")) {
			testName(field);
			testTypeOfField(field);
			testNoMethodsForPublicField(field);
			Class<?> fieldType = field.getType();
			if (fieldType == String.class) {
				if (!View.class.isAssignableFrom(field.getDeclaringClass())) {
					testSize(field);
				}
			} 
		}
	}
	
	private void testNoListFields(Class<?> clazz) {
		FlatProperties.getProperties(clazz).values();
		Field[] fields = clazz.getFields();
		for (Field field : fields) {
			if (FieldUtils.isPublic(field) && !FieldUtils.isStatic(field) && !FieldUtils.isTransient(field)) {
				Class<?> fieldType = field.getType();
				if (List.class.equals(fieldType)) {
					problems.add("List in " + clazz.getName()  + ": not allowed. Only classes with id (or inlines of classes with id) may contain lists");
				} else if (FieldUtils.isFinal(field) && !FieldUtils.isAllowedPrimitive(fieldType)) {
					testNoListFields(fieldType);
				}
			}
		}
	}
	
	private void testName(Field field) {
		String name = field.getName();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();
		testName(name, messagePrefix);
	}

	private void testName(Class<?> clazz) {
		String name = clazz.getSimpleName();
		String messagePrefix = "Class " + clazz.getSimpleName();
		testName(name, messagePrefix);
	}

	private void testName(String name, String messagePrefix) {
		for (int i = 0; i<name.length(); i++) {
			char c = name.charAt(i);
			if (isIdentifierChar(c)) continue;
			problems.add(messagePrefix + " has an invalid name. " + c + " is not allowed");
			break;
		}
	}

	private boolean isIdentifierChar(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

	private void testTypeOfField(Field field) {
		Class<?> fieldType = field.getType();
		String messagePrefix = field.getName() + " of " + field.getDeclaringClass().getName();

		if (fieldType == List.class) {
			testTypeOfListField(field, messagePrefix);
		} else if (fieldType == Set.class) {
			if (!FieldUtils.isFinal(field)) {
				problems.add(messagePrefix + " must be final (" + fieldType.getSimpleName() + " Fields must be final)");
			}
			testTypeOfSetField(field, messagePrefix);
		} else {
			testTypeOfField(field, messagePrefix);
		}
	}

	private void testTypeOfListField(Field field, String messagePrefix) {
		Class<?> listType = null;
		try {
			listType = GenericUtils.getGenericClass(field);
		} catch (Exception x) {
			// silent
		}
		if (listType != null) {
			messagePrefix = "Generic of " + messagePrefix;
			testTypeOfListField(listType, messagePrefix);
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}

	private void testTypeOfSetField(Field field, String messagePrefix) {
		@SuppressWarnings("rawtypes")
		Class setType = null;
		try {
			setType = GenericUtils.getGenericClass(field);
		} catch (Exception x) {
			// silent
		}
		if (setType != null) {
			if (!Enum.class.isAssignableFrom(setType)) {
				problems.add("Set type must be an enum class: " + messagePrefix);
			}
			@SuppressWarnings("unchecked")
			List<?> values = EnumUtils.itemList(setType);
			if (values.size() > 32) {
				problems.add("Set enum must not have more than 32 elements: " + messagePrefix);
			}
		} else {
			problems.add("Could not evaluate generic of " + messagePrefix);
		}
	}
	
	private void testTypeOfField(Field field, String messagePrefix) {
		Class<?> fieldType = field.getType();
		if (FieldUtils.isAllowedPrimitive(fieldType)) {
			return;
		}
		if (fieldType.isPrimitive()) {
			problems.add(messagePrefix + " has invalid Type");
		}
		if (Modifier.isAbstract(fieldType.getModifiers())) {
			problems.add(messagePrefix + " must not be of an abstract Type");
		}
		if (fieldType.isArray()) {
			problems.add(messagePrefix + " is an array which is not allowed (except for byte[])");
		}
		if (FieldUtils.isFinal(field)) {
			testInlineClass(fieldType);
		} else {
			testClass(fieldType);
		}
	}

	private void testTypeOfListField(Class<?> fieldType, String messagePrefix) {
		if (fieldType.isPrimitive()) {
			problems.add(messagePrefix + " has invalid Type");
			return;
		}
		if (Modifier.isAbstract(fieldType.getModifiers())) {
			problems.add(messagePrefix + " must not be of an abstract Type");
			return;
		}
		if (fieldType.isArray()) {
			problems.add(messagePrefix + " is an array which is not allowed");
			return;
		}
		if (Codes.isCode(fieldType)) {
			problems.add(messagePrefix + " is a list of codes which is not allowed");
			return;
		}
		testClass(fieldType);
	}
	
	private void testSize(Field field) {
		PropertyInterface property = Properties.getProperty(field);
		try {
			AnnotationUtil.getSize(property);
		} catch (IllegalArgumentException x) {
			problems.add("Missing size for: " + property.getDeclaringClass().getName() + "." + property.getPath());
		}
	}
		
	private void testNoMethodsForPublicField(Field field) {
		PropertyInterface property = Properties.getProperty(field);
		if (property != null) {
			if (property.getClass().getSimpleName().startsWith("Method")) {
				problems.add("A public attribute must not have getter or setter methods: " + field.getDeclaringClass().getName() + "." + field.getName());
			}
		} else {
			problems.add("No property for " + field.getName());
		}
	}
	
	private void testResources(Class<?> clazz) {
		for (PropertyInterface property : FlatProperties.getProperties(clazz).values()) {
			if (StringUtils.equals(property.getName(), "id", "version")) continue;
			String resourceText = Resources.getPropertyName(property);
			if (resourceText.startsWith("'") && resourceText.endsWith("'")) {
				missingResources.add(resourceText.substring(1, resourceText.length()-1));
			}
		}
	}
	
	public void reportMissingResources() {
		for (String key : missingResources) {
			Resources.reportMissing(key, true);
		}
	}

}
