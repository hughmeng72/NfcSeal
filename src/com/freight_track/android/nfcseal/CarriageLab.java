package com.freight_track.android.nfcseal;

import java.util.ArrayList;

public class CarriageLab {
	private final int RESULT_SUCCESS = 1;
	
    private static CarriageLab sCarriageLab;
    
	private ArrayList<WsResultRecord> Result;
	private ArrayList<WsResultCarriage> Details;

    private CarriageLab() {
    	Details = new ArrayList<WsResultCarriage>();
    }

    public static CarriageLab get() {
        if (sCarriageLab == null) {
            sCarriageLab = new CarriageLab();
        }
        return sCarriageLab;
    }

    public ArrayList<WsResultCarriage> getCarriages() {
        return Details;
    }

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
	
}

