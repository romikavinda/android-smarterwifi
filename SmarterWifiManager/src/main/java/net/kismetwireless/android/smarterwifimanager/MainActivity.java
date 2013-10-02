package net.kismetwireless.android.smarterwifimanager;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.doomonafireball.betterpickers.timepicker.TimePickerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


// Main icon color shifts
// 00e8d5    b8b8b8    a40000

public class MainActivity extends FragmentActivity {
    Context context;

    private static int PREFS_REQ = 1;

    SmarterWifiServiceBinder serviceBinder;
    SmarterPagerAdapter pagerAdapter;
    ViewPager viewPager;
    ActionBar actionBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        serviceBinder = new SmarterWifiServiceBinder(this);

        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayHomeAsUpEnabled(false);

        viewPager = (ViewPager) findViewById(R.id.pager);

        // Defer UI creation until we've bound to the service
        serviceBinder.doCallAndBindService(new SmarterWifiServiceBinder.BinderCallback() {
            @Override
            public void run(SmarterWifiServiceBinder b) {
                List<Fragment> fragments = new Vector<Fragment>();

                fragments.add(Fragment.instantiate(context, FragmentMain.class.getName()));
                fragments.add(Fragment.instantiate(context, FragmentSsidBlacklist.class.getName()));
                fragments.add(Fragment.instantiate(context, FragmentLearned.class.getName()));
                fragments.add(Fragment.instantiate(context, FragmentBluetoothBlacklist.class.getName()));
                fragments.add(Fragment.instantiate(context, FragmentTimeRange.class.getName()));

                pagerAdapter = new SmarterPagerAdapter(getSupportFragmentManager(), fragments);

                for (int x = 0; x < pagerAdapter.getCount(); x++) {
                    SmarterFragment sf = (SmarterFragment) pagerAdapter.getItem(x);

                    ActionBar.Tab t = actionBar.newTab().setText(getString(sf.getTitle()));
                    t.setTabListener(new SmarterTabsListener(sf));
                    actionBar.addTab(t);
                }

                viewPager.setAdapter(pagerAdapter);

                viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

                if (savedInstanceState != null) {
                    actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tabposition", 0));
                }
            }

        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            outState.putInt("tabposition", actionBar.getSelectedTab().getPosition());
        } catch (NullPointerException npe) {
            Log.d("smarter", "tried to save sate but got a null in getSelectedTab(): " + npe);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (serviceBinder != null)
            serviceBinder.doUnbindService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivityForResult(new Intent(MainActivity.this, PrefsActivity.class), PREFS_REQ);
            return true;
        }

        if (item.getItemId() == R.id.action_about) {
            showAbout();
            return true;
        }

        if (item.getItemId() == R.id.action_timetest) {
            showTimeTest();
            return true;
        }

        return true;
    }

    private void showTimeTest() {
        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = getLayoutInflater();

        View v = inflater.inflate(R.layout.time_dialog, null);
        */

        TimePickerBuilder tpb = new TimePickerBuilder();
        tpb.setFragmentManager(getSupportFragmentManager());
        tpb.setStyleResId(R.style.BetterPickersDialogFragment);

        tpb.show();

        /*
        builder.setView(v);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        builder.create().show();
        */

    }

    public class SmarterTabsListener implements ActionBar.TabListener {
        public Fragment fragment;

        public SmarterTabsListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // ignore
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // show tab
            viewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // hide
        }
    }

    public class SmarterPagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> fragments = new ArrayList<Fragment>();

        public SmarterPagerAdapter(FragmentManager fm, List<Fragment> frags) {
            super(fm);

            fragments = frags;
        }

        @Override
        public Fragment getItem(int i) {
            return fragments.get(i);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public void showAbout() {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);

        WebView wv = new WebView(this);

        wv.loadUrl("file:///android_asset/html_no_copy/about.html");

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);

                return true;
            }
        });

        alert.setView(wv);

        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        alert.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PREFS_REQ) {
            serviceBinder.doUpdatePreferences();
        }
    }

}
