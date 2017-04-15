/*
 * Copyright 2016 TomeOkin
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tomeokin.lspush.data.remote;

import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.app.crypto.CryptoToken;
import com.tomeokin.lspush.data.model.AccessBundle;
import com.tomeokin.lspush.data.model.Collect;
import com.tomeokin.lspush.data.model.Comment;
import com.tomeokin.lspush.data.model.Response;
import com.tomeokin.lspush.data.model.User;
import com.tomeokin.lspush.data.model.UserProfile;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface LsPushService {
    String AUTH_TOKEN_KEY = "token";
    String AUTH_TOKEN = AUTH_TOKEN_KEY + ": token_value";
    String API_URL = BuildConfig.LSPUSH_SERVER_URL;
    String CLOUD_SPEED_URL = API_URL + "/api/cloudspeed/download";

    @POST("api/captcha/checkCaptcha")
    Observable<Response<String>> checkCaptcha(@Body CryptoToken cryptoToken);

    @Multipart
    @POST("api/resource/upload")
    Observable<Response<String>> upload(@Part MultipartBody.Part resource);

    @Multipart
    @Headers(AUTH_TOKEN)
    @POST("api/user/updateUserInfo")
    Observable<Response<User>> updateUserInfo(@Part("user") RequestBody user, @Part MultipartBody.Part... resource);

    @POST("api/user/register")
    Observable<Response<AccessBundle>> register(@Body CryptoToken cryptoToken);

    @POST("api/user/login")
    Observable<Response<AccessBundle>> login(@Body CryptoToken cryptoToken);

    @Headers(AUTH_TOKEN)
    @POST("/api/collect/post")
    Observable<Response<Void>> postCollect(@Body Collect collect);

    @Headers(AUTH_TOKEN)
    @GET("api/collect/newest")
    Observable<Response<List<Collect>>> getNewestCollects(@Query("page") int page, @Query("size") int size);

    String SEARCH_COLLECT_OPTION_TITLE = "title";
    String SEARCH_COLLECT_OPTION_TAG = "tag";
    String SEARCH_COLLECT_OPTION_URL = "url";

    String SEARCH_COLLECT_GROUP_ALL = "all";
    String SEARCH_COLLECT_GROUP_USER = "user";
    String SEARCH_COLLECT_GROUP_FAVOR = "favor";

    @Headers(AUTH_TOKEN)
    @GET("/api/collect/search")
    Observable<Response<List<Collect>>> findCollect(@Query("targetUserId") long userId, @Query("option") String option,
        @Query("group") String group, @Query("keyword") String keyword, @Query("page") int page,
        @Query("size") int size);

    @Headers(AUTH_TOKEN)
    @GET("/api/collect/hot")
    Observable<Response<List<Collect>>> findHotCollects(@Query("page") int page, @Query("size") int size);

    @Headers(AUTH_TOKEN)
    @GET("/api/collect/recent")
    Observable<Response<List<Collect>>> findRecentTopHotCollects(@Query("days") int days);

    @Headers(AUTH_TOKEN)
    @GET("api/collect/own/{userId}")
    Observable<Response<List<Collect>>> getUserCollects(@Path("userId") long userId, @Query("page") int page,
        @Query("size") int size);

    @Headers(AUTH_TOKEN)
    @GET("api/collect/favor/{userId}")
    Observable<Response<List<Collect>>> getUserFavorCollects(@Path("userId") long userId, @Query("page") int page,
        @Query("size") int size);

    @Headers(AUTH_TOKEN)
    @GET("api/collect/profile/{userId}")
    Observable<Response<UserProfile>> getUserProfile(@Path("userId") long userId);

    @Headers(AUTH_TOKEN)
    @POST("/api/follow/follow/{userId}")
    Observable<Response<Void>> follow(@Path("userId") long userId);

    @Headers(AUTH_TOKEN)
    @POST("/api/follow/unfollow/{userId}")
    Observable<Response<Void>> unfollow(@Path("userId") long userId);

    @Headers(AUTH_TOKEN)
    @POST("/api/favor/add/{colId}")
    Observable<Response<Void>> addFavor(@Path("colId") long colId);

    @Headers(AUTH_TOKEN)
    @POST("/api/favor/remove/{colId}")
    Observable<Response<Void>> removeFavor(@Path("colId") long colId);

    @Headers(AUTH_TOKEN)
    @GET("/api/fetch/getUrlInfo")
    Observable<Response<Collect>> getUrlInfo(@Query("url") String url);

    @Headers(AUTH_TOKEN)
    @POST("/api/comment/add/{colId}")
    Observable<Response<Comment>> addComment(@Path("colId") long colId, @Body String comment);

    @Headers(AUTH_TOKEN)
    @GET("/api/comment/find/{colId}")
    Observable<Response<List<Comment>>> findComment(@Path("colId") long colId, @Query("page") int page,
        @Query("size") int size);

    @Headers(AUTH_TOKEN)
    @GET("/api/follow/following/{userId}")
    Observable<Response<List<User>>> getUserFollowing(@Path("userId") long userId, @Query("page") int page,
        @Query("size") int size);

    @Headers(AUTH_TOKEN)
    @GET("/api/follow/follower/{userId}")
    Observable<Response<List<User>>> getUserFollowers(@Path("userId") long userId, @Query("page") int page,
        @Query("size") int size);
}