package com.example.user.sadajura;

public class ShoppingBasketDataForm {

    private String product_title,product_time,product_image_path;
    private String  product_price;

    public ShoppingBasketDataForm(String product_title, String product_time, String product_image_path, String product_price) {
        this.product_title = product_title;
        this.product_time = product_time;
        this.product_image_path = product_image_path;
        this.product_price = product_price;
    }

    public String getProduct_title() {
        return product_title;
    }

    public void setProduct_title(String product_title) {
        this.product_title = product_title;
    }

    public String getProduct_time() {
        return product_time;
    }

    public void setProduct_time(String product_time) {
        this.product_time = product_time;
    }

    public String getProduct_image_path() {
        return product_image_path;
    }

    public void setProduct_image_path(String product_image_path) {
        this.product_image_path = product_image_path;
    }

    public String getProduct_price() {
        return product_price;
    }

    public void setProduct_price(String product_price) {
        this.product_price = product_price;
    }
}
