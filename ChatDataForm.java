package com.example.user.sadajura;

public class ChatDataForm {
    private String mem_id;
    private String time;
    private int roomNo;
    private String chat_message,nickName,recive_id,product_img;

    public ChatDataForm(String product_img, String mem_id, String time, int roomNo , String chat_message, String nickName , String recive_id) {
        this.nickName =nickName;
        this.mem_id = mem_id;
        this.time = time;
        this.roomNo = roomNo;
        this.chat_message = chat_message;
        this.recive_id = recive_id;
        this.product_img =product_img;
    }

    public String getProduct_img() {
        return product_img;
    }

    public void setProduct_img(String product_img) {
        this.product_img = product_img;
    }

    public String getRecive_id() {
        return recive_id;
    }

    public void setRecive_id(String recive_id) {
        this.recive_id = recive_id;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getChat_message() {
        return chat_message;
    }

    public void setChat_message(String chat_message) {
        this.chat_message = chat_message;
    }


    public String getMem_id() {
        return mem_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getRoomNo() {
        return roomNo;
    }

    public void setRoomNo(int roomNo) {
        this.roomNo = roomNo;
    }
}
