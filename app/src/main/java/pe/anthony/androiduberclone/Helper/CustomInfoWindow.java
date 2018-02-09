package pe.anthony.androiduberclone.Helper;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import pe.anthony.androiduberclone.R;

/**
 * Es parte del paque helper-- ayudante
 * Created by ANTHONY on 9/02/2018.
 */

public class CustomInfoWindow implements GoogleMap.InfoWindowAdapter{

    View myView;


    public CustomInfoWindow(Context context) {
        myView = LayoutInflater.from(context).inflate(R.layout.custom_rider_info_window,null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        TextView txtPickupTitle = myView.findViewById(R.id.txtPickupInfo);
        txtPickupTitle.setText(marker.getTitle());

        TextView txtPickupSnippet = myView.findViewById(R.id.txtPickupSnippet);
        txtPickupSnippet.setText(marker.getTitle());
        return myView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
