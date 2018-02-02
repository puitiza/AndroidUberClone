package pe.anthony.androiduberclone.Remote;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Retrofit es un cliente HTTP de tipo seguro para Android y Java.
 * Esta poderosa librería hace sencillo consumir datos JSON o XML, que después es analizado en Objetos Java (Plain Old Java Objects, POJOs).
 * Todas las peticiones GET, POST, PUT, PATCH, and DELETE pueden ser ejecutadas.
 * Retrofit no tiene un convertidor JSON integrado para convertir de objetos JSON a Java. En su lugar, viene con soporte para las siguientes librerías de convertidor JSON para manejar eso:
 *    Gson: com.squareup.retrofit:converter-gson
 *    Jackson: com.squareup.retrofit:converter-jackson
 *     Moshi: com.squareup.retrofit:converter-moshi
 *   Para buffers de Protocolo, Retrofit soporta:
 *    Protobuf: com.squareup.retrofit2:converter-protobuf
 *    Wire: com.squareup.retrofit2:converter-wire
 *  Y para XML Retrofit soporta:
 *    Simple Framework: com.squareup.retrofit2:converter-simpleframework
 *
 * Creando la Interfaz API
 * Esta interfaz contiene métodos que vamos a usar para ejecutar peticiones HTTP tales como POST, PUT, y DELETE. Comencemos con la petición GET.
 * Created by ANTHONY on 20/01/2018.
 */

public interface IGoogleAPI {
    @GET
    Call<String> getPath(@Url String url);

    /*Esto es un ejemplo de un metodo post y como funciona retrofit

         @POST("/posts")
         @FormUrlEncoded
         Call<Post> savePost(@Field("title") String title,
                            @Field("body") String body,
                            @Field("userId") long userId);

         Encima del método está la anotación @POST, que indica que queremos ejecutar una petición POST cuando este método es llamado.
         El valor de argumento para la anotación @POST es el punto fina---que es /posts. Así que la URL completa sería
         http://jsonplaceholder.typicode.com/posts.
         ¿pero qué sobre el @FormUrlEncoded? Esto indicará que la petición tendrá su tipo MIME
         (un campo de encabezado que identifica el formato del cuerpo de una petición o respuesta HTTP)
         establecido a application/x-www-form-urlencoded y también que sus nombres de campo y valores serán codificados en UTF-8 antes de ser codificados en URI.
         La anotación @Field("key") con nombre de parámetro debería empatar el nombre que la API espera.
         Retrofit convierte implícitamente los valores a cadenas de texto usando String.valueOf(Object)
    */

}
