package com.vboly.entity;

/**
 * 回调信息实体类
 */
public class PostResult {
    /**
     * 回调状态
     */
    private int result = 0;
    /**
     * 回调信息
     */
    private String msg = "";

    public PostResult(){

    }

    public PostResult(int result, String msg){
        this.result = result;
        this.msg = msg;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "PostResult{" +
                "result=" + result +
                ", msg='" + msg + '\'' +
                '}';
    }
}
