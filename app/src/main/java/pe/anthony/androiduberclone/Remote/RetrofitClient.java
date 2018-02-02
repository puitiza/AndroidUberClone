package pe.anthony.androiduberclone.Remote;

import java.nio.channels.ScatteringByteChannel;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 *
 * Creando la Instancia Retrofit
 *
 * Para emitir peticiones de red a una API RESTFUL con Retrofit, necesitamos crear una instancia usando la clase Retrofit Builder y configurarla con una URL base
 * , Retrofit necesita una URL base para construir su instancia, así que le pasaremos una URL cuando llamemos a RetrofitClient.getClient(String baseUrl).
 * Esta URL será entonces usada para construir la instancia. También estamos especificando el convertidor JSON que necesitamos (Gson). "GsonConverterFactory.create()"
 * Created by ANTHONY on 20/01/2018.
 */

public class RetrofitClient {
    private static Retrofit retrofit = null;
    public static Retrofit getClient(String baseURL){
        if(retrofit == null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}

