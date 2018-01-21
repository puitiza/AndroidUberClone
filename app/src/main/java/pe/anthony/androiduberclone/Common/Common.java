package pe.anthony.androiduberclone.Common;

import pe.anthony.androiduberclone.Remote.IGoogleAPI;
import pe.anthony.androiduberclone.Remote.RetrofitClient;
import retrofit2.Retrofit;

/**
 * Created by ANTHONY on 20/01/2018.
 */

public class Common {
    public static final String baseURL = "https://maps.googleapis.com";
    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
