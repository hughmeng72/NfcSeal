package com.freight_track.android.nfcseal.model;

import java.util.List;

/**
 * Created by wayne on 10/2/2016.
 */

public class ResponseResults<T> {
    private List<T> list;

    private ResponseBase error;

    public List<T> getList() {
        return list;
    }

    public ResponseBase getError() {
        return error;
    }
}
