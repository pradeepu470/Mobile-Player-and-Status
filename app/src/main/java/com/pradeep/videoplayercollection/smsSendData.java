package com.pradeep.videoplayercollection;

public class smsSendData {
    private String id;
    private String msg;
    private String readState;
    private String time;
    private String folderName;

    public smsSendData(String id,String msg,String readState,String time,String folderName) {
        this.id = id;
        this.msg = msg;
        this.readState = readState;
        this.time = time;
        this.folderName = folderName;
    }
    public String getId(){
        return id;
    }
    public String getMsg(){
        return msg;
    }
    public String getReadState(){
        return readState;
    }
    public String getTime(){
        return time;
    }
    public String getFolderName(){
        return folderName;
    }
}
