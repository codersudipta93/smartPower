/*
 * This file is auto-generated.  DO NOT MODIFY.
 */
package com.example.parkingagent.utils.scanner;
/**
 * 处理结果回调
 */

import com.example.parkingagent.utils.scanner.entity.Entity;

public interface ICallBack extends android.os.IInterface
{
  /** Default implementation for ICallBack. */
  public static class Default implements ICallBack
  {
    /**
         * 处理成功结果回调
         */
    @Override public void onSuccess(Entity bean) throws android.os.RemoteException
    {
    }
    /**
         * 处理失败结果回调
         */
    @Override public void onFiled(int errorCode) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements ICallBack
  {
    private static final String DESCRIPTOR = "com.sunmi.scanner.ICallBack";
    /** Construct the stub at attach it to the interface. */
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an com.sunmi.scanner.ICallBack interface,
     * generating a proxy if needed.
     */
    public static ICallBack asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof ICallBack))) {
        return ((ICallBack)iin);
      }
      return new Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      String descriptor = DESCRIPTOR;
      switch (code)
      {
        case INTERFACE_TRANSACTION:
        {
          reply.writeString(descriptor);
          return true;
        }
        case TRANSACTION_onSuccess:
        {
          data.enforceInterface(descriptor);
          Entity _arg0;
          if ((0!=data.readInt())) {
            _arg0 = Entity.CREATOR.createFromParcel(data);
          }
          else {
            _arg0 = null;
          }
          this.onSuccess(_arg0);
          reply.writeNoException();
          return true;
        }
        case TRANSACTION_onFiled:
        {
          data.enforceInterface(descriptor);
          int _arg0;
          _arg0 = data.readInt();
          this.onFiled(_arg0);
          reply.writeNoException();
          return true;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
    }
    private static class Proxy implements ICallBack
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
           * 处理成功结果回调
           */
      @Override public void onSuccess(Entity bean) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          if ((bean!=null)) {
            _data.writeInt(1);
            bean.writeToParcel(_data, 0);
          }
          else {
            _data.writeInt(0);
          }
          boolean _status = mRemote.transact(Stub.TRANSACTION_onSuccess, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onSuccess(bean);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
           * 处理失败结果回调
           */
      @Override public void onFiled(int errorCode) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain();
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(errorCode);
          boolean _status = mRemote.transact(Stub.TRANSACTION_onFiled, _data, _reply, 0);
          if (!_status && getDefaultImpl() != null) {
            getDefaultImpl().onFiled(errorCode);
            return;
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      public static ICallBack sDefaultImpl;
    }
    static final int TRANSACTION_onSuccess = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_onFiled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    public static boolean setDefaultImpl(ICallBack impl) {
      // Only one user of this interface can use this function
      // at a time. This is a heuristic to detect if two different
      // users in the same process use this function.
      if (Proxy.sDefaultImpl != null) {
        throw new IllegalStateException("setDefaultImpl() called twice");
      }
      if (impl != null) {
        Proxy.sDefaultImpl = impl;
        return true;
      }
      return false;
    }
    public static ICallBack getDefaultImpl() {
      return Proxy.sDefaultImpl;
    }
  }
  /**
       * 处理成功结果回调
       */
  public void onSuccess(com.example.parkingagent.utils.scanner.entity.Entity bean) throws android.os.RemoteException;
  /**
       * 处理失败结果回调
       */
  public void onFiled(int errorCode) throws android.os.RemoteException;
}
