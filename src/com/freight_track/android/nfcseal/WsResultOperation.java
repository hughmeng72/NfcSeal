package com.freight_track.android.nfcseal;

import android.content.Context;


public class WsResultOperation {

	public enum OperationTypeEnum {
		unknown, lock, unlock, signIn, exceptional
	}

	private int OperateId;
	private String OperateTime;
	private int SealOperate;
	private String Place;
	private String Operator;
	private String ImgNames;
	private String ExceptionCause;
	private String Coordinate;


	public WsResultOperation() {

	}

	public String getExceptionCause() {
		return ExceptionCause;
	}

	public void setExceptionCause(String exceptionCause) {
		ExceptionCause = exceptionCause;
	}

	public int getOperateId() {
		return OperateId;
	}

	public void setOperateId(int operateId) {
		OperateId = operateId;
	}

	public String getOperateTime() {
		return OperateTime;
	}

	public void setOperateTime(String operateTime) {
		OperateTime = operateTime;
	}

	public int getSealOperate() {
		return SealOperate;
	}

	public void setSealOperate(int sealOperate) {
		SealOperate = sealOperate;
	}

	public String getPlace() {
		return Place;
	}

	public void setPlace(String place) {
		Place = place;
	}

	public String getOperator() {
		return Operator;
	}

	public void setOperator(String operator) {
		Operator = operator;
	}

	public String getImgNames() {
		return ImgNames;
	}

	public void setImgNames(String imgNames) {
		ImgNames = imgNames;
	}

	public String getCoordinate() {
		return Coordinate;
	}

	public void setCoordinate(String coordinate) {
		Coordinate = coordinate;
	}

	@Override
	public String toString() {
		return OperateTime.toString();
	}
	
	public boolean isExceptional() {
		return (OperationTypeEnum.exceptional == OperationTypeEnum.values()[SealOperate]);
	}

	public String getSealOperateDesc(Context context) {
		String ret = null;

		try {
			OperationTypeEnum state = OperationTypeEnum.values()[SealOperate];

			switch (state) {
			case lock:
				ret = context.getString(R.string.prompt_tag_status_locked);
				break;
			case unlock:
				ret = context.getString(R.string.prompt_tag_status_unlocked);
				break;
			case signIn:
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

}
