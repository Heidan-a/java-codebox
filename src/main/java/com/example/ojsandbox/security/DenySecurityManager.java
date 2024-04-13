package com.example.ojsandbox.security;

import java.security.Permission;

public class DenySecurityManager extends SecurityManager{

    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限不足");
    }
}
