package org.minimalj.security.permissiontest.pkgrole;

import org.minimalj.model.annotation.Grant;
import org.minimalj.model.annotation.Grant.Privilege;

@Grant(privilege = Privilege.UPDATE, value = "UpdateClassRole")
public class H {

	public Object id;

}
