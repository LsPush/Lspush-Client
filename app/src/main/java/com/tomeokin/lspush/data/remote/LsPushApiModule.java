/*
 * Copyright 2017 TomeOkin
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

import android.app.Application;

import com.decay.gson.GsonStrategy;
import com.decay.logger.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tomeokin.lspush.BuildConfig;
import com.tomeokin.lspush.app.NetworkUtils;
import com.tomeokin.lspush.app.logger.AppLogger;
import com.tomeokin.lspush.data.internal.CurrentUser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
public class LsPushApiModule {
    private Log log = AppLogger.networkLog("LsPush");
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String CACHE_DIR = "http-cache-lspush";

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(final Application application, final CurrentUser currentUser) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(provideHttpLoggingInterceptor());
            //builder.addNetworkInterceptor(new StethoInterceptor());
        }

        builder.connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(35, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor(currentUser))
            //.addInterceptor(provideOfflineCacheInterceptor())
            //.addNetworkInterceptor(provideCacheInterceptor())
            .cache(provideCache(application));

        return builder.build();
    }

    private HttpLoggingInterceptor provideHttpLoggingInterceptor() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                log.log(message);
            }
        });
        loggingInterceptor.setLevel(
            BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
        return loggingInterceptor;
    }

    private Interceptor authInterceptor(final CurrentUser currentUser) {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String token = request.header(LsPushService.AUTH_TOKEN_KEY);
                if (token != null) {
                    request = request.newBuilder()
                        .header(LsPushService.AUTH_TOKEN_KEY, currentUser.getAuthToken())
                        .build();
                }
                return chain.proceed(request);
            }
        };
    }

    private Cache provideCache(final Application app) {
        Cache cache = null;
        try {
            cache = new Cache(new File(app.getCacheDir(), CACHE_DIR), 10 * 1024 * 1024); // 10 MB
        } catch (Exception e) {
            log.log(e, "Could not create Cache!");
        }
        return cache;
    }

    public Interceptor provideCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());

                // re-write response header to force use of cache
                CacheControl cacheControl = new CacheControl.Builder().maxAge(0, TimeUnit.SECONDS)
                    .onlyIfCached()
                    .maxStale(0, TimeUnit.SECONDS) // 清除离线时的配置
                    .build();

                return response.newBuilder().header(CACHE_CONTROL, cacheControl.toString()).build();
            }
        };
    }

    public Interceptor provideOfflineCacheInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                if (NetworkUtils.isOffline()) {
                    log.w("no network available");
                    CacheControl cacheControl = new CacheControl.Builder().maxStale(7, TimeUnit.DAYS).build();

                    request = request.newBuilder().cacheControl(cacheControl).build();
                }

                return chain.proceed(request);
            }
        };
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        return new GsonBuilder().setExclusionStrategies(new GsonStrategy())
            // see http://stackoverflow.com/questions/7910734/gsonbuilder-setdateformat-for-2011-10-26t202959-0700
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // or yyyy-MM-dd'T'HH:mm:ss.SSSZ, yyyy-MM-dd HH:mm:ss and so on
            .create();
    }

    @Provides
    @Singleton
    public LsPushService provideLsPushService(Gson gson, OkHttpClient okHttpClient) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(LsPushService.API_URL)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .validateEagerly(BuildConfig.DEBUG) // 预校验
            .build();

        return retrofit.create(LsPushService.class);
    }
}
