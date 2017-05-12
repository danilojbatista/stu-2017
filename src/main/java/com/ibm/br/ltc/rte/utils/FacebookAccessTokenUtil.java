package com.ibm.br.ltc.rte.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import facebook4j.auth.AccessToken;

public class FacebookAccessTokenUtil {

	private static final String PATH_STR = "";

	public static AccessToken retrieveExistingAccessToken() {
		FileInputStream fileInputStream = null;
		ObjectInputStream objectInputStream = null;
		AccessToken accessToken = null;

		try {
			fileInputStream = new FileInputStream(PATH_STR + "fb.accessToken");
			objectInputStream = new ObjectInputStream(fileInputStream);
			accessToken = (AccessToken) objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
		} finally {
			if (objectInputStream != null) {
				try {
					objectInputStream.close();
				} catch (IOException e) {
				}
			}
		}

		return accessToken;
	}

	public static void saveAccessToken(AccessToken accessToken) {
		FileOutputStream fileOutputStream = null;
		ObjectOutputStream objectOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(PATH_STR + "fb.accessToken");
			objectOutputStream = new ObjectOutputStream(fileOutputStream);
			objectOutputStream.writeObject(accessToken);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (objectOutputStream != null) {
				try {
					objectOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
