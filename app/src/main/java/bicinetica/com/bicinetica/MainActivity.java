package bicinetica.com.bicinetica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import bicinetica.com.bicinetica.diagnostics.Trace;

public class MainActivity extends AppCompatActivity {

    private TabsAdapter tabs;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Trace.init();

        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        this.mViewPager = findViewById(R.id.view_pager);
        this.tabs = new TabsAdapter(getSupportFragmentManager(), this);
        this.mViewPager.setAdapter(this.tabs);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(this.mViewPager);

        askForPermissions(Manifest.permission.ACCESS_FINE_LOCATION);
        askForPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                Trace.critical(e);
                MainActivity.this.finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Trace.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.sensor_settings) {
            Intent intent = new Intent(this, SensorsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void askForPermissions(String permission)
    {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
            {
                Toast.makeText(getApplicationContext(), "The application needs explanation", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), "The aplication doesn't need explanation", Toast.LENGTH_LONG).show();
            }

            ActivityCompat.requestPermissions(this, new String[] { permission }, 0);
        }
    }
}
