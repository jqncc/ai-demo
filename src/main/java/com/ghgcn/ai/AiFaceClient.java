package com.ghgcn.ai;

public interface AiFaceClient {

    /**
     * 注册用户
     */
    void addUser();

    /**
     * 注册组
     */
    void addGroup();

    /**
     * 人脸检测
     */
    void detect();

    /**
     * 人脸查找
     */
    void findFaces();

    /**
     * 获取用户
     */
    void getUser();

    /**
     * 获取用户所有人脸照片
     */
    void getFaces();

    /**
     * 人脸比对
     */
    void match();

    /**
     * 身份验证
     */
    void persionVerify();
}
