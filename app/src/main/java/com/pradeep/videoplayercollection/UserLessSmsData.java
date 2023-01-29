package com.pradeep.videoplayercollection;

public class UserLessSmsData {
    private String mUserName;
    private String body;

    UserLessSmsData(String mUserName, String body) {
        this.mUserName = mUserName;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public String getUserName() {
        return mUserName;
    }

}
