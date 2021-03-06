package org.minimalj.security;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.backend.Persistence;
import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;
import org.minimalj.transaction.Role;
import org.minimalj.transaction.Transaction;
import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

public abstract class Authorization {
	private static final Logger LOG = Logger.getLogger(Persistence.class.getName());
	private static ThreadLocal<Serializable> securityToken = new ThreadLocal<>();
	private Map<UUID, Subject> userByToken = new HashMap<>();

	public static final Authorization defaultAuthorization = createDefault();
	
	private static Authorization createDefault() {
		String authorizationClassName = System.getProperty("MjAuthorization");
		if (!StringUtils.isBlank(authorizationClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Authorization> authorizationClass = (Class<? extends Authorization>) Class.forName(authorizationClassName);
				Authorization authorization = authorizationClass.newInstance();
				return authorization;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, LOG, "Set authorization failed");
			}
		} 
		
		String userFile = System.getProperty("MjUserFile");
		if (userFile != null) {
			return new TextFileAuthorization(userFile);
		}

		String jaasConfiguration = System.getProperty("MjJaasConfiguration");
		if (jaasConfiguration != null) {
			return new JaasAuthorization(jaasConfiguration);
		}

		return null;
	}
	
	public boolean isAllowed(Transaction<?> transaction) {
		Role role = getRole(transaction);
		boolean noRoleNeeded = role == null;
		return noRoleNeeded || getCurrentRoles().contains(role.value());
	}
	
	public static Role getRole(Transaction<?> transaction) {
		Role role = transaction.getClass().getAnnotation(Role.class);
		if (role != null) {
			return role;
		}
		role = transaction.getClass().getPackage().getAnnotation(Role.class);
		return role;
	}
	
	public void checkGrants(Grant.Privilege privilege, Class<?> clazz) {
		List<String> currentRoles = getCurrentRoles();
		@SuppressWarnings("unused")
		boolean allowed = false;
		Grant[] grantsOnClass = clazz.getAnnotationsByType(Grant.class);
		allowed |= isGranted(currentRoles, privilege, clazz, grantsOnClass);
		Grant[] grantsOnPackage = clazz.getPackage().getAnnotationsByType(Grant.class);
		allowed |= isGranted(currentRoles, privilege, clazz, grantsOnPackage);
		allowed |= isGranted(currentRoles, Privilege.ALL, clazz, grantsOnClass);
		allowed |= isGranted(currentRoles, Privilege.ALL, clazz, grantsOnPackage);
	}
	
	protected static boolean isGranted(List<String> currentRoles, Grant.Privilege privilege, Class<?> clazz, Grant[] grants) {
		if (grants != null) {
			for (Grant grant : grants) {
				if (grant.privilege() == privilege) {
					for (String roleGranted : grant.value()) {
						if (currentRoles.contains(roleGranted)) {
							return true;
						}
					}
					throw new IllegalStateException(privilege + " not allowed on " + clazz.getSimpleName());
				}
			}
		}
		return false;
	}

	protected List<String> getCurrentRoles() {
		Subject subject = Subject.getCurrent();
		return subject != null ? subject.getRoles() : Collections.emptyList();
	}
	
	/**
	 * @return null if login failed, empty list if user has no roles
	 */
	protected abstract List<String> retrieveRoles(UserPassword userPassword);

	/**
	 * 
	 * @return null if login failed, empty list if user has no roles
	 */
	public Subject login(UserPassword userPassword) {
		List<String> roles = retrieveRoles(userPassword);
		if (roles != null) {
			Subject subject = new Subject();
			subject.setName(userPassword.user);
			subject.getRoles().addAll(roles);
			UUID token = UUID.randomUUID();
			subject.setToken(token);
			userByToken.put(token, subject);
			return subject;
		} else {
			return null;
		}
	}

	public void logout() {
		userByToken.remove(securityToken.get());
	}

	public Subject getUserByToken(Serializable token) {
		return userByToken.get(token);
	}
	
}