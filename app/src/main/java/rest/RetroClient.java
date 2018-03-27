package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Hersteller on 3/26/2018.
 */

public class RetroClient {

    /********
     * URLS
     *******/
    public static final String ROOT_URL = "http://api.openweathermap.org/data/2.5/";

    /**
     * Get Retrofit Instance
     */
    private static Retrofit getRetrofitInstance() {
        return new Retrofit.Builder()
                .baseUrl(ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Get API Service
     *
     * @return API Service
     */
    public static ApiService getApiService(){
        return getRetrofitInstance().create(ApiService.class);
    }
}
