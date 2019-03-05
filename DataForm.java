package com.example.user.sadajura;

public class DataForm {
    private String name,mem_id,photo_path;

    private int no;


    public DataForm(String name,String mem_id,  String photo_path , int no) {
        this.name = name;
        this.photo_path = photo_path;
        this.no = no;
        this.mem_id = mem_id;

    }

    public String getMem_id() {
        return mem_id;
    }



    public String getName() {
        return name;
    }


    public int getNo() {
        return no;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhoto_path(String photo_path) {
        this.photo_path = photo_path;
    }

    public void setNo(int no) {
        this.no = no;
    }




    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }
}