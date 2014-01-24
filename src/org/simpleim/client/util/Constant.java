package org.simpleim.client.util;

import java.nio.charset.Charset;

public final class Constant {
	private Constant() {}

	public static final Charset UTF8 = Charset.forName("UTF-8");

	public static final String DATA_DIRECTORY_PATH = "data/";
	public static final String ACCOUNT_FILE_PATH = DATA_DIRECTORY_PATH + "account.json";
	public static final String FRIENDS_FILE_PATH = DATA_DIRECTORY_PATH + "friends.json";
}
