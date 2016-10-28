package com.freight_track.android.nfcseal.model;

public class WsResultRecord {

	private int RESULT;
	private int EFFECTIVETOKEN;
	private String ERRORINFO;
	
	public WsResultRecord() {
		
	}

	public int getRESULT() {
		return RESULT;
	}

	public void setRESULT(int rESULT) {
		RESULT = rESULT;
	}

	public int getEFFECTIVETOKEN() {
		return EFFECTIVETOKEN;
	}

	public void setEFFECTIVETOKEN(int eFFECTIVETOKEN) {
		EFFECTIVETOKEN = eFFECTIVETOKEN;
	}

	public String getERRORINFO() {
		return ERRORINFO;
	}

	public void setERRORINFO(String eRRORINFO) {
		ERRORINFO = eRRORINFO;
	}
	
	
}
