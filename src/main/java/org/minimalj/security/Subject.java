package org.minimalj.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;

public class Subject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final ThreadLocal<Subject> subject = new ThreadLocal<>();
	private String name;
	private Serializable token;
	
	private final List<String> roles = new ArrayList<>();

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public Serializable getToken() {
		return token;
	}
	
	public void setToken(Serializable token) {
		this.token = token;
	}
	
	public List<String> getRoles() {
		return roles;
	}
	
	public static boolean hasRoleFor(Transaction<?> transaction) {
		Role role = getRole(transaction);
		boolean noRoleNeeded = role == null;
		return noRoleNeeded || hasRole(role.value());
	}
	
	public static boolean hasRole(String... roleNames) {
		Subject subject = getCurrent();
		if (subject != null) {
			for (String roleName : roleNames) {
				if (subject.roles.contains(roleName)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Role getRole(Transaction<?> transaction) {
		Role role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = transaction.getClass().getPackage().getAnnotation(Role.class);
		return role;
	}
	
	public static void checkPermission(Grant.Privilege privilege, Class<?> clazz) {
		boolean allowed = false;
		Grant[] grantsOnClass = clazz.getAnnotationsByType(Grant.class);
		allowed |= checkPermission(privilege, clazz, grantsOnClass);
		Grant[] grantsOnPackage = clazz.getPackage().getAnnotationsByType(Grant.class);
		allowed |= checkPermission(privilege, clazz, grantsOnPackage);
		allowed |= checkPermission(Privilege.ALL, clazz, grantsOnClass);
		allowed |= checkPermission(Privilege.ALL, clazz, grantsOnPackage);
	}
	
	private static boolean checkPermission(Grant.Privilege privilege, Class<?> clazz, Grant[] grants) {
		if (grants != null) {
			for (Grant grant : grants) {
				if (grant.privilege() == privilege) {
					if (hasRole(grant.value())) {
						return true;
					} else {
						throw new IllegalStateException(privilege + " not allowed on " + clazz.getSimpleName());
					}
				}
			}
		}
		return false;
	}
	
	
	public static void setCurrent(Subject subject) {
		Subject.subject.set(subject);
	}
	
	public static Subject getCurrent() {
		return subject.get();
	}

}
