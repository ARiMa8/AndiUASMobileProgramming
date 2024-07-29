package pnj.uts.ti.andiuasmobileprogramming;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.location.Geocoder;
import android.location.Address;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private SearchView searchView;
    private Geocoder geocoder;
    private Button buttonRemoveAllWishlist;
    private ArrayList<LatLng> wishlist = new ArrayList<>();
    private Map<LatLng, Marker> markersMap = new HashMap<>();
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        searchView = findViewById(R.id.search_view);
        geocoder = new Geocoder(this);

        buttonRemoveAllWishlist = findViewById(R.id.button_remove_all_wishlist);
        buttonRemoveAllWishlist.setOnClickListener(v -> {
            // Remove all markers and clear wishlist
            markersMap.clear();
            wishlist.clear();
            mMap.clear();
            Toast.makeText(MainActivity.this, "All wishlist locations removed", Toast.LENGTH_SHORT).show();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void searchLocation(String location) {
        List<Address> addressList;
        try {
            addressList = geocoder.getFromLocationName(location, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.clear(); // Clear existing markers
                mMap.addMarker(new MarkerOptions().position(latLng).title(location)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Geocoder failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        mMap.setMyLocationEnabled(true);

        mMap.setOnMapClickListener(latLng -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Add wishlist place");

            final EditText input = new EditText(MainActivity.this);
            input.setHint("Enter place name");
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String placeName = input.getText().toString().trim();
                if (!placeName.isEmpty()) {
                    // Add marker with title
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(placeName) // This sets the title, which is shown as a label
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    marker.setTag(placeName); // Use tag to store place name
                    wishlist.add(latLng);
                    markersMap.put(latLng, marker);
                    Toast.makeText(MainActivity.this, "Place added to wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Place name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        });

        mMap.setOnMarkerClickListener(marker -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Remove this place");
            builder.setMessage("Do you want to remove this place from the wishlist?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                LatLng position = marker.getPosition();
                marker.remove();
                markersMap.remove(position);
                wishlist.remove(position);
                Toast.makeText(MainActivity.this, "Place removed from wishlist", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

            builder.show();
            return true;
        });

        // Default info window will show the marker's title
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}