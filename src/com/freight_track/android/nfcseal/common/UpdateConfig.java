package com.freight_track.android.nfcseal.common;

import android.content.Context;

import com.freight_track.android.nfcseal.R;
import com.freight_track.android.nfcseal.model.ResponseResult;
import com.freight_track.android.nfcseal.model.Version;
import com.freight_track.android.nfcseal.update.ParseData;
import com.freight_track.android.nfcseal.update.UpdateHelper;
import com.freight_track.android.nfcseal.update.bean.Update;
import com.freight_track.android.nfcseal.update.type.RequestType;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Created by wayne on 10/11/2016.
 */

public class UpdateConfig {

    /**
     * Update check via Http Get
     */
    public static void initGet(Context context) {
        UpdateHelper.init(context);

        String url = Utils.getUpdateCheckUrl();
        UpdateHelper.getInstance()
                .setMethod(RequestType.get)
                .setCheckUrl(url)
                .setDialogLayout(R.layout.dialog_update)
                .setCheckJsonParser(new ParseData() {
                    @Override
                    public Update parse(String response) {
                        Update update = new Update();

                        ResponseResult<Version> result;

                        GsonBuilder gson = new GsonBuilder();
                        Type resultType = new TypeToken<ResponseResult<Version>>() {
                        }.getType();

                        try {
                            result = gson.create().fromJson(response, resultType);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return update;
                        }

                        Version version = result.getEntity();

                        update.setUpdateUrl(version.getUpdateUrl());
                        update.setVersionCode(version.getVersionCode());
                        update.setApkSize(version.getApkSize());
                        update.setVersionName(version.getVersionName());
                        update.setUpdateContent(version.getUpdateContent());
                        update.setForce(version.isForce());

                        return update;
                    }
                });
    }
}