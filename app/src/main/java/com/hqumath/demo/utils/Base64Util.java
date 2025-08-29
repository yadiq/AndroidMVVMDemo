package com.hqumath.demo.utils;

import android.util.Base64;

public class Base64Util {

	public static String encode(byte[] data) {
		//return Base64.encodeToString(data, Base64.DEFAULT);//可能有换行符
		return Base64.encodeToString(data, Base64.NO_WRAP);
	}

	public static byte[] decode(String str) {
		//return Base64.decode(str, Base64.DEFAULT);////可能有换行符
		return Base64.decode(str, Base64.NO_WRAP);
	}
}

