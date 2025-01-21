package com.yingxue.lesson.utils;

import java.util.UUID;

public class PasswordUtils {

	//验证输入的明文密码是否与存储的加密密码匹配
	//rawPassword - 前端输入过来的密码
	//encPassword - 数据库里保存的密码
	public static boolean matches(String salt, String rawPass, String encPass) {
		return new PasswordEncoder(salt).matches(encPass, rawPass);
	}

	//用于加密输入的明文密码。它将盐和明文密码连接起来，然后使用
	public static String encode(String rawPass, String salt) {
		return new PasswordEncoder(salt).encode(rawPass);
	}

	//用于生成一个随机的盐值。这个盐值是一个随机的 UUID 字符串，并去除了连字符，截取前20个字符。
	public static String getSalt() {
		return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);
	}
}
