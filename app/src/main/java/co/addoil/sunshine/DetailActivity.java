package co.addoil.sunshine;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            String date = getIntent().getStringExtra(DetailFragment.DATE_KEY);
            Bundle args = new Bundle();
            args.putString(DetailFragment.DATE_KEY, date);

            DetailFragment f = new DetailFragment();
            f.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, f)
                    .commit();
        }
    }

}
