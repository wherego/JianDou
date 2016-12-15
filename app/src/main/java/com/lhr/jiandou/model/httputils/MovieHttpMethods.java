package com.lhr.jiandou.model.httputils;

import com.lhr.jiandou.doubanservice.DouBanService;
import com.lhr.jiandou.model.bean.MovieHttpResult;
import com.lhr.jiandou.model.bean.SubjectsBean;
import com.lhr.jiandou.model.error.ApiException;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by ChinaLHR on 2016/12/15.
 * Email:13435500980@163.com
 */

public class MovieHttpMethods {
    public static final String BASE_URL = "https://api.douban.com/v2/movie/";
    private static final int DEFAULT_TIMEOUT = 5;
    private Retrofit retrofit;
    private DouBanService mDouBanService;

    private MovieHttpMethods() {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        httpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        retrofit = new Retrofit.Builder()
                .client(httpClientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        mDouBanService = retrofit.create(DouBanService.class);
    }

    private static class Holder {
        private static final MovieHttpMethods INSTANCE = new MovieHttpMethods();
    }

    public static final MovieHttpMethods getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 根据tag,start与count获取豆瓣电影
     */
    public void getMovieByTag(Subscriber<List<SubjectsBean>> subscriber, String tag, int start, int count) {
        mDouBanService.getMovieByTag(tag,start,count)
                .map(new HttpResultFunc<List<SubjectsBean>>())
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);

    }

    /**
     * 相同格式的Http请求数据统一进行预处理，将HttpResult的Data部分剥离出来给subseriber
     * T为真正需要的类型，也就是Data部分
     */
    private class HttpResultFunc<T> implements Func1<MovieHttpResult<T>, T> {
        @Override
        public T call(MovieHttpResult<T> httpResult) {
            if (httpResult.getCount() == 0) {
                throw new ApiException(100);
            }
            return httpResult.getSubjects();
        }
    }

}
