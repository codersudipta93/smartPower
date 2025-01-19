package com.sunmi.scanner;

import com.sunmi.scanner.entity.Entity;
/**
 * 处理结果回调
 */
interface ICallBack {
    /**
     * 处理成功结果回调
     */
    void onSuccess(in Entity bean);
    /**
     * 处理失败结果回调
     */
    void onFiled(in int errorCode);
}
