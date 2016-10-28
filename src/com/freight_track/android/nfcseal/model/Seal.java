package com.freight_track.android.nfcseal.model;

import java.util.Date;

import android.content.Context;

import com.freight_track.android.nfcseal.R;

public class Seal {
	private static String TAG = "Seal";

	public enum StateEnum {
		notExisted, lockable, locked, unlocked, invalid
	}

	private String mTagId = "";
	private String mSealId = "";
	private String mSealNo = "";
	private String mCarriageNo = "";
	private String mLocation = "";
	private String mPhotoName1 = "";
	private String mPhoto1FilePath = "";
	private String mPhotoName2 = "";
	private String mPhoto2FilePath = "";
	private String mPhotoName3 = "";
	private String mPhoto3FilePath = "";
	private String mUnlockPhotoName = "";
	private String mUnlockPhotoFilePath = "";

	private String mExceptionReason = "";
	
	public String getExceptionReason() {
		return mExceptionReason;
	}

	public void setExceptionReason(String exceptionReason) {
		mExceptionReason = exceptionReason;
	}


	private Date mOperationTime;
	private String mPlace = "";

	private StateEnum mState;

	
	
	public StateEnum getState() {
		return mState;
	}

	public void setState(StateEnum state) {
		mState = state;
	}

	
	private Context mContext;

	public String getLocation() {
		return mLocation;
	}

	public void setLocation(String location) {
		mLocation = location;
	}

	public Seal(Context context) {
		mContext = context;
		mOperationTime = new Date();
	}

	
	public String getSealId() {
		return mSealId;
	}

	public void setSealId(String sealId) {
		mSealId = sealId;
	}

	public String getSealNo() {
		return mSealNo;
	}

	public void setSealNo(String sealNo) {
		mSealNo = sealNo;
	}

	public String getTagId() {
		return mTagId;
	}

	public void setTagId(String tagId) {
		mTagId = tagId;
	}

	public String getCarriageNo() {
		return mCarriageNo;
	}

	public void setCarriageNo(String carriageNo) {
		mCarriageNo = carriageNo;
	}

	public String getPhotoName1() {
		return mPhotoName1;
	}

	public void setPhotoName1(String photoName1) {
		mPhotoName1 = photoName1;
	}

	public String getPhoto1FilePath() {
		return mPhoto1FilePath;
	}

	public void setPhoto1FilePath(String photo1FilePath) {
		mPhoto1FilePath = photo1FilePath;

		this.setPhotoName1(null);

		String subString[] = mPhoto1FilePath.split("/");

		if (subString != null && subString.length > 0) {
			this.setPhotoName1(subString[subString.length - 1]);
		}
	}

	public String getPhotoName2() {
		return mPhotoName2;
	}

	public void setPhotoName2(String photoName2) {
		mPhotoName2 = photoName2;
	}

	public String getPhoto2FilePath() {
		return mPhoto2FilePath;
	}

	public void setPhoto2FilePath(String photo2FilePath) {
		mPhoto2FilePath = photo2FilePath;

		this.setPhotoName2(null);

		String subString[] = mPhoto2FilePath.split("/");

		if (subString != null && subString.length > 0) {
			this.setPhotoName2(subString[subString.length - 1]);
		} 
	}

	public String getPhotoName3() {
		return mPhotoName3;
	}

	public void setPhotoName3(String photoName3) {
		mPhotoName3 = photoName3;
	}

	public String getPhoto3FilePath() {
		return mPhoto3FilePath;
	}

	public void setPhoto3FilePath(String photo3FilePath) {
		mPhoto3FilePath = photo3FilePath;

		this.setPhotoName3(null);

		String subString[] = mPhoto3FilePath.split("/");

		if (subString != null && subString.length > 0) {
			this.setPhotoName3(subString[subString.length - 1]);
		} 
	}

	public String getUnlockPhotoName() {
		return mUnlockPhotoName;
	}

	public void setUnlockPhotoName(String unlockPhotoName) {
		mUnlockPhotoName = unlockPhotoName;
	}

	public String getUnlockPhotoFilePath() {
		return mUnlockPhotoFilePath;
	}

	public void setUnlockPhotoFilePath(String unlockPhotoFilePath) {
		mUnlockPhotoFilePath = unlockPhotoFilePath;

		String subString[] = mUnlockPhotoFilePath.split("/");

		if (subString != null && subString.length > 0) {
			this.setUnlockPhotoName(subString[subString.length - 1]);
		} else {
			this.setUnlockPhotoName(null);
		}
	}

	public Date getOperationTime() {
		return mOperationTime;
	}

	public void setOperationTime(Date operationTime) {
		mOperationTime = operationTime;
	}

	public String getPlace() {
		return mPlace;
	}

	public void setPlace(String place) {
		mPlace = place;
	}

	public String getPhotoNames() {
		String photoNames = "";

		if (mPhotoName1 != null && !mPhotoName1.isEmpty()) {
			photoNames += mPhotoName1 + ";";
		}

		if (mPhotoName2 != null && !mPhotoName2.isEmpty()) {
			photoNames += mPhotoName2 + ";";
		}

		if (mPhotoName3 != null && !mPhotoName3.isEmpty()) {
			photoNames += mPhotoName3 + ";";
		}

		if (mPhotoName1 == null || mPhotoName1.isEmpty()) {
			return null;
		} else {
			return (photoNames.substring(0, photoNames.length() - 1));
		}
	}

	public boolean isLockValidated() {
		// TODO: Lock validation

		return true;
	}

	public boolean isUnlockValidated() {
		// TODO: Lock validation

		return true;
	}

	public String getStateWarningDescription(StateEnum state) {

		String ret = "";

		switch (state) {
		case notExisted:
			ret = mContext.getString(R.string.prompt_tag_no_record);
			break;
		case lockable:
			ret = mContext.getString(R.string.prompt_tag_lockable);
			break;
		case locked:
			ret = mContext.getString(R.string.prompt_tag_unlockable);
			break;
		case unlocked:
			ret = mContext.getString(R.string.prompt_tag_unlocked);
			break;
		case invalid:
			ret = mContext.getString(R.string.prompt_tag_invalid);
			break;
		}

		return ret;
	}

}
