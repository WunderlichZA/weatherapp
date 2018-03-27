package rest;

import model.WeatherModel;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Hersteller on 3/27/2018.
 */

public interface ApiService {

    /*
    Retrofit get annotation with our URL
    And our method that will return us the List of ContactList
    */

    @GET("weather?")
    Call<WeatherModel> getWeatherUpdate(@Query("lat") int lat, @Query("lon") int lon,
                                            @Query("appid") String apikey);
}
