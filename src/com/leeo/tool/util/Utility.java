/**
 *File:Untility.java
 *Date:2014-4-1
 *
 *  
 */
package com.leeo.tool.util;

import android.content.Context;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * 
 * @author luoxiaoyong
 */
public class Utility {
	
	private static int STANDARD_WIDTH = 1280;
	private static int STANDARD_HEIGHT = 720;
	
	/**
	 * 返回本机Ip地址
	 * 
	 * @return
	 */
	public static String GetIpAddress() {
		Enumeration<NetworkInterface> netInterfaces = null;
		try {
			netInterfaces = NetworkInterface.getNetworkInterfaces();
			while (netInterfaces.hasMoreElements()) {
				NetworkInterface ni = netInterfaces.nextElement();
				Enumeration<InetAddress> ips = ni.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ip = ips.nextElement();
					if (!ip.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
						return ip.getHostAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 判断网络是否可用
	 * 
	 * @param ctx
	 * @return
	 */
	public static boolean isNetworkAvailable(Context ctx) {
		ConnectivityManager connectivityManager = (ConnectivityManager) (ctx.getSystemService(Context.CONNECTIVITY_SERVICE));
		if (null == connectivityManager) {
			return false;
		}
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (null != info && info.isAvailable()) {
			String ipAddr = Utility.GetIpAddress();
			if (null != ipAddr && ipAddr.length() > 0) {
				return true;
			}
		}
		return false;
	}

	/**
	 * view to Rect
	 * @param view
	 * @return
	 */
	public static Rect viewToRect(View view){
		if(null != view){
			int []location = new int[2];
			view.getLocationInWindow(location);
			//view.getLocationOnScreen(location);
			int lx = location[0];
			int ly = location[1];
			int rx = lx + view.getWidth();
			int ry = ly + view.getHeight();
			return new Rect(lx,ly,rx,ry);
		}
		return null;
	}
}
