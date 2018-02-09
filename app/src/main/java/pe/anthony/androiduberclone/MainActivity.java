package pe.anthony.androiduberclone;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.app.AlertDialog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import dmax.dialog.SpotsDialog;
import pe.anthony.androiduberclone.Common.Common;
import pe.anthony.androiduberclone.Model.Rider;
import pe.anthony.androiduberclone.Model.User;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity {
    private Button btnSignIn,btnRegister;
    RelativeLayout rootLayout;  //este es el id del activity_main.xml
//   Esto es firebase
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    @Override
    protected void attachBaseContext(Context newBase) {
//        Esto es de una libreria para cambiar el tipo de fuente de los text view
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//      Es necesario setear la nueva fuente antes de que construya la activity
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                            .setDefaultFontPath("fonts/Arkhip_font.ttf")
                                            .setFontAttrId(R.attr.fontPath)
                                            .build());
        setContentView(R.layout.activity_main);

//      Inicializas firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_rider_tbl);

//      Inicializo los view del mainActivity
        btnSignIn = findViewById(R.id.btnSignIn);
        btnRegister = findViewById(R.id.btnRegister);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT){
            btnRegister.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_register_background_v19));
        }else{
            btnRegister.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_register_background));
        }
        rootLayout = findViewById(R.id.rootLayout);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterDialog();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPlayServices()){
                    showLoginDialog();
                }else{
                    GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
                    int resulCode = googleAPI.isGooglePlayServicesAvailable(MainActivity.this);
                    checkPlayService(resulCode);
                }

            }
        });
    }

    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.Signin);
        dialog.setMessage("Porfavor necesitamos tu sign In");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);
        dialog.setPositiveButton(R.string.Signin, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
//              Set disable button Sign In if is processing
                btnSignIn.setEnabled(false);
                if(TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edtPassword.getText().toString().length()<6){
                    Snackbar.make(rootLayout,"Password too short",Snackbar.LENGTH_SHORT).show();
                    return;
                }
//              Usando la libreria para mostrar que esta cargando
                final AlertDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();
//                Login user
               auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                       .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                           @Override
                           public void onSuccess(AuthResult authResult) {
                               waitingDialog.dismiss();
                               startActivity(new Intent(MainActivity.this,Home.class));
                               finish();
                           }
                       })
                       .addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               waitingDialog.dismiss();
                               Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                               btnSignIn.setEnabled(true);
                           }
                       });
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.register);
        dialog.setMessage("Porfavor necesitamos tu correo");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);
        dialog.setView(register_layout);
        dialog.setPositiveButton(R.string.register, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                if(TextUtils.isEmpty(edtEmail.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter email address",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(edtPassword.getText().toString().length()<6){
                    Snackbar.make(rootLayout,"Password too short",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtName.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter name",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(edtPhone.getText().toString())){
                    Snackbar.make(rootLayout,"Please enter phone number",Snackbar.LENGTH_SHORT).show();
                    return;
                }
//                Registrar new user
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
//                                Guarda al usuario para la BD
                                Rider rider = new Rider();
                                rider.setEmail(edtEmail.getText().toString());
                                rider.setName(edtName.getText().toString());
                                rider.setPassword(edtPassword.getText().toString());
                                rider.setPhone(edtPhone.getText().toString());
//                                Usa email to key
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(rider)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Snackbar.make(rootLayout,"Register success fully",Snackbar.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(rootLayout,"Failed"+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }

        private boolean checkPlayServices() {
//      Esta funcion es para compromar los play services del dispositivos
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resulCode = googleAPI.isGooglePlayServicesAvailable(this);
        if(resulCode != ConnectionResult.SUCCESS){
            if(googleAPI.isUserResolvableError(resulCode)){
                googleAPI.getErrorDialog(this, resulCode, PLAY_SERVICE_RES_REQUEST).show();
            }
            else{
                Toast.makeText(this,"This device is not supported",Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }

    private void checkPlayService(int PLAY_SERVICE_STATUS)
    {
        /*Esta funcion fue creada para manejar mejor  la respuesta para comprobar si esta actualizado los services de google play*/
        switch (PLAY_SERVICE_STATUS)
        {
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                Toast.makeText(this,"please udpate your google play service",Toast.LENGTH_SHORT).show();
                break;
            case ConnectionResult.API_UNAVAILABLE:
                //API is not available
                break;
            case ConnectionResult.NETWORK_ERROR:
                //Network error while connection
                break;
            case ConnectionResult.RESTRICTED_PROFILE:
                //Profile is restricted by google so can not be used for play services
                break;
            case ConnectionResult.SERVICE_MISSING:
                //service is missing
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                //service available but user not signed in
                break;
            case ConnectionResult.SUCCESS:
                break;
        }
    }
}
