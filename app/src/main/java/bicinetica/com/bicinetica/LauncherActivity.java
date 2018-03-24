package bicinetica.com.bicinetica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class LauncherActivity extends AppCompatActivity {

    private List<String> permissionsNeeded = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (requestPermissions()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LauncherActivity.this, UserActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 1000);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestPermissions()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean requestPermissions() {
        for (String permission : permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] { permission }, 0);
                return false;
            }
        }

        return true;
    }
}
