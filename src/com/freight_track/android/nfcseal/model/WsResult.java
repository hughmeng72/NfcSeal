package com.freight_track.android.nfcseal.model;

import java.util.ArrayList;

public class WsResult {
	
	private final int RESULT_SUCCESS = 1;
	
	private ArrayList<WsResultRecord> Result;
	private ArrayList<WsResultOperation> Details;
	
	public boolean isOK() {
		boolean ret = true;
		
		if (Result.size() == 0) {
			ret = false;
		}
		else {
			ret = (Result.get(0).getRESULT() == RESULT_SUCCESS);
		}
		
		return ret;
	}
	
	public int getResultOfFirstRecord() {
		return Result.get(0).getRESULT();
	}

	public ArrayList<WsResultOperation> getOperations() {
		return Details;
	}

}
