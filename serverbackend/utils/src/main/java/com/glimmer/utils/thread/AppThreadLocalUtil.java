package com.glimmer.utils.thread;


import com.glimmer.model.user.pojos.User;

public class AppThreadLocalUtil {

    private final static ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<>();

    //存入线程中
    public static void setUser(User user){
        USER_THREAD_LOCAL.set(user);
    }

    //从线程中获取
    public static User getUser(){
        return USER_THREAD_LOCAL.get();
    }

    //清理
    public static void clear(){
        USER_THREAD_LOCAL.remove();
    }

}
