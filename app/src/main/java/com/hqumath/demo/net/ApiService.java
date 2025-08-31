package com.hqumath.demo.net;

import com.hqumath.demo.bean.ReposEntity;
import com.hqumath.demo.bean.UserInfoEntity;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * ****************************************************************
 * 文件名称: MainService
 * 作    者: Created by gyd
 * 创建时间: 2019/1/22 17:11
 * 文件描述:
 * 注意事项:
 * 版权声明:
 * ****************************************************************
 */
public interface ApiService {

    //@Headers("Content-Type: application/json")
    //@POST("api/auth/loginApp")
    //Observable<BaseResult<LoginBean>> login(@Body Map<String, String> body);

    //获取设备列表
    //@GET("api/device/list")
    //Observable<BaseResult<DeviceListBean>> getDeviceList(@QueryMap Map<String, String> query);

    //获取用户信息
    @GET("users/{userName}")
    Observable<UserInfoEntity> getUserInfo(@Path("userName") String userName);

    //获取用户仓库
    @GET("users/{userName}/repos")
    Observable<List<ReposEntity>> getMyRepos(@Path("userName") String userName, @QueryMap Map<String, String> query);

}
