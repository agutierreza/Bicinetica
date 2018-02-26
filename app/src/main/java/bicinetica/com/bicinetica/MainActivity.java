package bicinetica.com.bicinetica;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TabsAdapter tabs;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        this.mViewPager = findViewById(R.id.view_pager);
        this.tabs = new TabsAdapter(getSupportFragmentManager(), this);
        this.mViewPager.setAdapter(this.tabs);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(this.mViewPager);

        askForPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        askForPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    private void askForPermissions(String permission)
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission))
            {
                Toast.makeText(getApplicationContext(), "The application needs explanation", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "The aplication doesn't need explanation", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(MainActivity.this, new String[] { permission }, 0);
        }
    }
}
