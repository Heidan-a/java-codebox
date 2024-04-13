package com.example.ojsandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{

    @Override
    public void checkPermission(Permission perm) {
        System.out.println("不做权限限制");
        System.out.println(perm);
        super.checkPermission(perm);
    }
}
