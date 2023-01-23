package com.pradeep.videoplayercollection;

import java.util.ArrayList;

public class SmsSend {
    public String UserName;
    public String UserNumber;
    ArrayList <smsSendData> listMessage = new ArrayList<>();

    public ArrayList getAllDetails() {
        return listMessage;
    }

    public String getUserName() {
        return UserName;
    }

    public String getUserNumber() {
        return UserNumber;
    }

    public void setAllDetails(smsSendData data) {
        listMessage.add(data);
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public void setUserNumber(String userNumber) {
        UserNumber = userNumber;
    }
}