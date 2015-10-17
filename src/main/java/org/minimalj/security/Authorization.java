package org.minimalj.security;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.minimalj.util.LoggingRuntimeException;
import org.minimalj.util.StringUtils;

public abstract class Authorization {
	private static final Logger logger = Logger.getLogger(Authorization.class.getName());

	private static Authorization instance;

	private static ThreadLocal<Serializable> securityToken = new ThreadLocal<>();
	private Map<UUID, Subject> userByToken = new HashMap<>();

	private static boolean available = true;
	
	public static Authorization createAuthorization() {
		if (!available) {
			return null;
		}
		
		String userFile = System.getProperty("MjUserFile");
		if (userFile != null) {
			return new TextFileAuthorization(userFile);
		}
		
		String authorizationClassName = System.getProperty("MjAuthorization");
		if (!StringUtils.isBlank(authorizationClassName)) {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Authorization> authorizationClass = (Class<? extends Authorization>) Class.forName(authorizationClassName);
				Authorization authorization = authorizationClass.newInstance();
				return authorization;
			} catch (Exception x) {
				throw new LoggingRuntimeException(x, logger, "Set authorization failed");
			}
		}

		available = false;
		return null;
	}
	
	public static Authorization getInstance() {
		if (instance == null) {
			instance = createAuthorization();
		}
		return instance;
	}
	
	public static boolean isAvailable() {
		getInstance();
		return available;
	}
	
	protected abstract List<String> retrieveRoles(UserPassword login);

	public Subject login(UserPassword login) {
		List<String> roles = retrieveRoles(login);
		Subject user = new Subject();
		if (roles != null) {
			user.setName(login.user);
			user.getRoles().addAll(roles);
			UUID token = UUID.randomUUID();
			user.setToken(token);
			userByToken.put(token, user);
		}
		return user;
	}

	public void logout() {
		userByToken.remove(securityToken.get());
	}

	public Subject getUserByToken(Serializable token) {
		return userByToken.get(token);
	}
	
	public void setSecurityToken(Serializable securityToken) {
		Authorization.securityToken.set(securityToken);	
	}
	
	public Subject getSubject() {
		return userByToken.get(securityToken.get());
	}
	
	public static class LoginFailedException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		
		public LoginFailedException() {
			super("Login failed");
		}
	}
	
}