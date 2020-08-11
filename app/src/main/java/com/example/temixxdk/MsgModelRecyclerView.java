package com.example.temixxdk;

public class MsgModelRecyclerView {

    int image;
    String msgStatus;
    String msgVal;

    public int getImage() {
        return image;
    }

    public String getMsgStatus() {
        return msgStatus;
    }

    public String getMsgVal() {
        return msgVal;
    }

    public MsgModelRecyclerView(int image, String msgStatus, String msgVal){
        this.image = image;
        this.msgStatus = msgStatus;
        this.msgVal = msgVal;
    }
}
