package com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.github.giorgosathanasopoulos.distributed_systems_aueb.android.backend.uid.UID;

public class Product implements Parcelable {

    @SerializedName("Name")
    private final String c_Name;

    @SerializedName("Type")
    private final String c_Type;

    @SerializedName("Quantity")
    private int m_Quantity;

    @SerializedName("Price")
    private final double c_Price;

    @SerializedName("Visible")
    private boolean m_Visible; // NOTE: remember to hide only from client!

    @SerializedName("Id")
    private final int c_Id;

    public Product(
            String p_Name,
            String p_Type,
            int p_Quantity,
            double p_Price) {
        this.c_Name = p_Name;
        this.c_Type = p_Type;
        this.m_Quantity = p_Quantity;
        this.c_Price = p_Price;
        this.m_Visible = true;
        c_Id = UID.getNextUID();
    }

    protected Product(Parcel in) {
        c_Name = in.readString();
        c_Type = in.readString();
        m_Quantity = in.readInt();
        c_Price = in.readDouble();
        m_Visible = in.readByte() != 0;
        c_Id = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    public String getName() {
        return c_Name;
    }

    public String getType() {
        return c_Type;
    }

    public int getQuantity() {
        return m_Quantity;
    }

    public double getPrice() {
        return c_Price;
    }

    public int getId() {
        return c_Id;
    }

    public void setQuantity(int p_Quantity) {
        this.m_Quantity = p_Quantity;
    }

    public boolean isVisible() {
        return m_Visible;
    }

    public void setHidden(boolean hidden) {
        m_Visible = !hidden;
    }

    public void toggleHidden() {
        m_Visible = !m_Visible;
    }

    public boolean decreaseQuantity(int p_Amount) {
        if (p_Amount <= 0 || p_Amount > m_Quantity) {
            return false;
        }

        m_Quantity -= p_Amount;
        return true;
    }

    public boolean increaseQuantity(int p_Amount) {
        if (p_Amount <= 0) {
            return false;
        }

        m_Quantity += p_Amount;
        return true;
    }

    @NonNull
    @Override
    public String toString() {
        return "{\n" +
                "\t\"Name\": \"" + c_Name + "\"\n" +
                "\t\"Type\": \"" + c_Type + "\"\n" +
                "\t\"Quantity\": " + m_Quantity + "\n" +
                "\t\"Price\": " + c_Price + "\n" +
                "\t\"Visible\": " + m_Visible + "\n" +
                "},\n";
    }

    public boolean getVisible() {
        return m_Visible;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(c_Name);
        dest.writeString(c_Type);
        dest.writeInt(m_Quantity);
        dest.writeDouble(c_Price);
        dest.writeByte((byte) (m_Visible ? 1 : 0));
        dest.writeInt(c_Id);
    }
}
