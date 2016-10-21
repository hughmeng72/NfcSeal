package com.freight_track.android.nfcseal;

import android.content.Context;

public class WsResultCarriage {
	
	private int ProductId;
	private String ProductCode;
	private String ProductState;
	
	private enum StateEnum {
		lockable,
		locked,
		unlocked,
		signIned,
		exceptional
	}
	
	public WsResultCarriage() {
		
	}

	public int getProductId() {
		return ProductId;
	}

	public void setProductId(int productId) {
		ProductId = productId;
	}

	public String getProductCode() {
		return ProductCode;
	}

	public void setProductCode(String productCode) {
		ProductCode = productCode;
	}

	public String getProductState() {
		return ProductState;
	}

	public void setProductState(String productState) {
		ProductState = productState;
		
	}
	
	public String getProductStateDesc(Context context) {
		String ret = null;
		
		try {
			StateEnum state = StateEnum.values()[Integer.parseInt(ProductState)];
			
			switch (state) {
			case locked:
				ret = context.getString(R.string.prompt_tag_status_locked);
				break;
			case unlocked:
				ret = context.getString(R.string.prompt_tag_status_unlocked);
				break;
			case signIned:
				ret = context.getString(R.string.prompt_tag_status_signedin);
				break;
			case exceptional:
				ret = context.getString(R.string.prompt_tag_status_exceptional);
				break;
			default:
				ret = context.getString(R.string.prompt_tag_status_other);
				break;
			}
		}
		catch (Exception e) {
			ret = context.getString(R.string.prompt_tag_status_other);
		}
		finally {
		}
	
		return ret;
	}
	
	public boolean isExceptional() {
		return (StateEnum.values()[Integer.parseInt(ProductState)] == StateEnum.exceptional);
	}
	
	@Override
	public String toString() {
		return ProductCode;
	}

}
