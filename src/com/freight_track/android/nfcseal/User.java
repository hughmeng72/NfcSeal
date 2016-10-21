package com.freight_track.android.nfcseal;


public class User {
	
	private int EFFECTIVETOKEN;
	private String EMAIL;
	private String ENTERPRISE;
	private String ERRORINFO;
	private String FREIGHTOWNER;
	private String IMGURL;
	private String ISEMAIL;
	private String ISSMS;
	private String MOBILE;
	private String REALNAME;
	private int RESULT;
	private String ROLEID;
	private String TOKEN = "";
	private String USERID;
	private String USERNAME;
	
    private static User sUser;

	public String getTOKEN() {
		return TOKEN;
	}
	
	private User() {
		// Nothing
	}
	
    public static User get() {
        if (sUser == null) {
        	sUser = new User();
        }
        return sUser;
    }
    
    public static void setUser(User user) {
    	sUser = user;
    }

}
