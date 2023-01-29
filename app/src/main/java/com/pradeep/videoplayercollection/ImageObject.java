package com.pradeep.videoplayercollection;

public class ImageObject {
    private String mPath;
    private boolean isSelect;
    public ImageObject(String path, boolean select){
        mPath = path;
        isSelect = select;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public String getmPath() {
        return mPath;
    }

    public void setmPath(String mPath) {
        this.mPath = mPath;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }

}
