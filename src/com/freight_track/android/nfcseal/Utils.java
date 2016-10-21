package com.freight_track.android.nfcseal;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.regex.Pattern;

import org.ksoap2.serialization.PropertyInfo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.text.Html;
import android.text.Spanned;

public class Utils {

	public static String getCurrentLanguage() {
		String langCode = Locale.getDefault().getLanguage();
		return (langCode.equals("zh") ? "zh-CN" : "en-US");

//		return "zh-CN";
	}

	public static String getWsNamespace() {
		return "http://www.freight-track.com/";
	}

	public static String getWsUrl() {
		return "http://www.freight-track.com/WebService/SealWebService.asmx";
	}

	public static String getWsSoapAction() {
		return "http://www.freight-track.com/";
	}

	public static String getWsNamespace2() {
		return "http://dev.freight-track.com/";
	}

	public static String getWsUrl2() {
		return "http://dev.freight-track.com/WebService/SealWebService.asmx";
	}

	public static String getWsSoapAction2() {
		return "http://dev.freight-track.com/";
	}

	public static String getUploadUrl() {
		return "http://www.freight-track.com/photohandler/sendphoto.aspx";
	}

	public static String getWsMethodOfUserAuthentication() {
		return "GetTokenByUserNameAndPassword";
	}

	public static String getWsMethodOfCreateEnterpriseManager() {
		return "CreateEnterpriseManager";
	}

	public static String getWsMethodOfLock() {
		return "CloseSeal";
	}

	public static String getWsMethodOfSealStateCheck() {
		return "CheckSealExisted";
	}

	public static String getWsMethodOfSignin() {
		return "UserSignin";
	}

	public static String getWsMethodOfLockOperation() {
		return "GetLockOperationInfoByTagUID";
	}

	public static String getWsMethodOfLockOperationBySealNo() {
		return "GetOperationInfoBySealNumber";
	}

	public static String getWsMethodOfUnlock() {
		return "OpenSeal";
	}

	public static String getWsMethodOfAdhocUnlock() {
		return "ReportException";
	}

	public static String getWsMethodOfCarriage() {
		return "GetFreightInfoByToken";
	}

	public static String getWsMethodOfOperation() {
		return "GetAllDynamicData";
	}

	public static String getWsMethodOfGetGooglePosition() {
		return "GetGooglePosition";
	}

	public static String getWsMethodOfGetAdjustedCoordinate() {
		return "GetAdjustedCoordinate";
	}

	public static PropertyInfo newPropertyInstance(String name, Object value, Object type) {
		PropertyInfo para = new PropertyInfo();
		para.setName(name);
		para.setValue(value);
		para.setType(type);

		return para;
	}

	public static String ByteArrayToHexString(byte[] inarray) {
		int i, j, in;
		String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		String out = "";

		for (j = 0; j < inarray.length; ++j) {
			in = (int) inarray[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
		}

		return out;
	}

	public static int bytestoInt(byte[] bytes, int offset, int dataLen) {
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dataLen <= 0)) {  
            return 0;  
        }  
        if ((offset >= bytes.length) || (bytes.length - offset < dataLen)) {  
            return 0;  
        }  
  
	    int iOutcome = 0;
	    byte bLoop;

	    for (int i = offset; i < offset + dataLen; i++) {
	        bLoop = bytes[i];
	        iOutcome += (bLoop & 0xFF) << (8 * i);
	    }

	    return iOutcome;
	}
	
    public static String bytesToAscii(byte[] bytes, int offset, int dataLen) { 
    	
        if ((bytes == null) || (bytes.length == 0) || (offset < 0) || (dataLen <= 0)) {  
            return null;  
        }  
        if ((offset >= bytes.length) || (bytes.length - offset < dataLen)) {  
            return null;  
        }  
  
        String asciiStr = null;  
        byte[] data = new byte[dataLen];  
        System.arraycopy(bytes, offset, data, 0, dataLen);  
        
        asciiStr = new String(data, Charset.forName("US-ASCII"));  
        
        return asciiStr;  
    }  
  
	public static String[][] getNfcTechList() {
		return new String[][] { new String[] { NfcA.class.getName(), NfcB.class.getName(), NfcF.class.getName(),
						NfcV.class.getName(), IsoDep.class.getName(), MifareClassic.class.getName(),
						MifareUltralight.class.getName(), Ndef.class.getName() } };
	}

	public static boolean isNetworkConnected(Context context) {
		boolean ret = false;

		ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mgr.getActiveNetworkInfo();

		if (info != null && info.isConnected()) {
			ret = true;
		}

		return ret;
	}

	public static Spanned getFormatedTitle(String title) {
		return Html.fromHtml("<font color=\"#FFFFFF\">" + title + "</font>");
	}

	  public static boolean isDouble(String str) {
	    Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");    
	    return pattern.matcher(str).matches();    
	  }  
	  
}
