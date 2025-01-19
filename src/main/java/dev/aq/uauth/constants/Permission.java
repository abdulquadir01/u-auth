package dev.aq.uauth.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

  ADMIN_READ("admin:read"),
  ADMIN_UPDATE("admin:update"),
  ADMIN_CREATE("admin:create"),
  ADMIN_DELETE("admin:create"),

  MANAGER_READ("management:read"),
  MANAGER_UPDATE("management:update"),
  MANAGER_CREATE("management:create"),
  MANAGER_DELETE("management:create");

  private final String permissionName;

}
