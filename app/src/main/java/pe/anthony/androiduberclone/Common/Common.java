package pe.anthony.androiduberclone.Common;

import pe.anthony.androiduberclone.Remote.IGoogleAPI;
import pe.anthony.androiduberclone.Remote.RetrofitClient;
import retrofit2.Retrofit;

/**
 * reando la Utilidades API
 * Esta clase tendrá la URL base como una variable estática y también proporcionará la interfaz IGoogleAPI
 * con un método estático getGoogleAPI() al resto de nuestra aplicación
 * Si fuera post ,Asegúrate de que terminas la URL base con una /
 * Created by ANTHONY on 20/01/2018.
 */

public class Common {
    public static final String baseURL = "https://maps.googleapis.com";
    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }
}
