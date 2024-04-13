package com.example.ojsandbox.security;

import cn.hutool.core.io.FileUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Permission;
import java.util.List;

public class TestSecurityManager extends SecurityManager{

    public static void main(String[] args) {
        System.setSecurityManager(new MySecurityManager());
        FileUtil.writeString("aa","aaa", Charset.defaultCharset());

    }
}
