package com.example.parkingagent.utils.scanner.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Darren(ZENG DONG YANG)
 * @e-mail darren1009@qq.com
 * @date 2018/04/10
 */
public class Entity<T> implements Parcelable {
    private String clazz;
    private T bean = null;

    public Entity(T bean) {
        clazz = bean.getClass().getName();
        this.bean = bean;
    }

    protected Entity(Parcel in) {
        try {
            clazz = in.readString();
            if (clazz.contains("ArrayList")) {
                bean = (T) new ArrayList<>();
                in.readList((List) bean, Class.forName(clazz).getClassLoader());
            } else {
                bean = in.readParcelable(Class.forName(clazz).getClassLoader());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(clazz);
        if (bean instanceof Parcelable) {
            dest.writeParcelable((Parcelable) bean, flags);
        } else if (bean instanceof List) {
            dest.writeList((List) bean);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Entity> CREATOR = new Creator<Entity>() {
        @Override
        public Entity createFromParcel(Parcel in) {
            return new Entity(in);
        }

        @Override
        public Entity[] newArray(int size) {
            return new Entity[size];
        }
    };

    public T getBean() {
        return bean;
    }
}
