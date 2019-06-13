package com.android.imusic.net;

import com.google.gson.internal.$Gson$Types;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by TinyHung@outlook.com
 * 2019/5/8
 * 配合OkHttpUtils数据交互的回调抽象类，代替interface接口
 * @param <T> 最终返回的实体对象数据类型,如果不指定，则返回String类型实体对象
 */

public abstract class OnResultCallBack <T> {

    //用户指定的数据类型
    private final Type mType;

    public OnResultCallBack(){
        mType = getClassTypeParameter(getClass());
    }

    /**
     * 获取当前类的多态
     * @param subclass class属性
     * @return 指定的泛型、多态
     */
    private Type getClassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        //JVM未识别到的,泛型、多态为空
        if (superclass instanceof Class) {
            return String.class;
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    /**
     * 返回实体Type
     * @return 泛型对象Type
     */
    public Type getType(){
        return mType;
    }

    /**
     * 响应成功
     * @param data 实体对象,没有传泛型默认是String.class类型
     */
    public abstract void onResponse(T data);

    /**
     * 响应、解析失败
     * @param code 错误码
     * @param errorMsg 描述信息
     */
    public abstract void onError(int code,String errorMsg);
}