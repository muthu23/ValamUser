package com.delivery.app.Models;

import java.io.Serializable;

/**
 * Created by admin on 11/20/2017.
 */

public class Locations extends Throwable implements Serializable{

    String sAddress;
    String dAddress;
    String sLatitude;
    String dLatitude;
    String sLongitude;
    String dLongitude;
    String goods;
    String reciver_name;
    String reciver_number;
    String helper_count;


    public String getsAddress() {
        return sAddress;
    }

    public void setsAddress(String sAddress) {
        this.sAddress = sAddress;
    }

    public String getdAddress() {
        return dAddress;
    }

    public void setdAddress(String dAddress) {
        this.dAddress = dAddress;
    }

    public String getsLatitude() {
        return sLatitude;
    }

    public void setsLatitude(String sLatitude) {
        this.sLatitude = sLatitude;
    }

    public String getdLatitude() {
        return dLatitude;
    }

    public void setdLatitude(String dLatitude) {
        this.dLatitude = dLatitude;
    }

    public String getsLongitude() {
        return sLongitude;
    }

    public void setsLongitude(String sLongitude) {
        this.sLongitude = sLongitude;
    }

    public String getdLongitude() {
        return dLongitude;
    }

    public void setdLongitude(String dLongitude) {
        this.dLongitude = dLongitude;
    }

    public String getGoods() {
        return goods;
    }

    public void setGoods(String goods) {
        this.goods = goods;
    }

    @Override
    public String toString() {
        return "Locations{" +
                "sAddress='" + sAddress + '\'' +
                ", dAddress='" + dAddress + '\'' +
                ", sLatitude='" + sLatitude + '\'' +
                ", dLatitude='" + dLatitude + '\'' +
                ", sLongitude='" + sLongitude + '\'' +
                ", dLongitude='" + dLongitude + '\'' +
                ", goods='" + goods + '\'' +
                '}';
    }


    public String getReciver_name() {
        return reciver_name;
    }

    public void setReciver_name(String reciver_name) {
        this.reciver_name = reciver_name;
    }

    public String getReciver_number() {
        return reciver_number;
    }

    public void setReciver_number(String reciver_number) {
        this.reciver_number = reciver_number;
    }

    public String getHelper_count() {
        return helper_count;
    }

    public void setHelper_count(String helper_count) {
        this.helper_count = helper_count;
    }
}
