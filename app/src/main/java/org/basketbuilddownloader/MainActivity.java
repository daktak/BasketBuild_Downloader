package org.basketbuilddownloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {
    private static final String LOGTAG = LogUtil
            .makeLogTag(MainActivity.class);
    String[] perms = {Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static int RC_INTERNET = 1;
    private static final int REQUEST_PREFS = 99;
    private static int RC_EXT_WRITE =2;
    private static int RC_INT_WRITE =3;
    private ArrayList<String> urls = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, "needed",
                    RC_INTERNET, perms);
        }
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);


        //String siteUrl = "https://s.basketbuild.com/devs/aokp/oneplus2/";
        /*
        Uri builtUri = Uri.parse(FORECAST_BASE_URL)
    .buildUpon()
                .appendPath()
                .build()*/
        //
        run(this);
    }

    public void run(Context context) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String base = mySharedPreferences.getString("prefBase",getString(R.string.base_val)).trim();
        String prefix = mySharedPreferences.getString("prefPrefix",getString(R.string.prefix_val)).trim();
        String project = mySharedPreferences.getString("prefProject",getString(R.string.project_val)).trim();
        String device = mySharedPreferences.getString("prefDevice",getString(R.string.device_val)).trim();
        Uri builtUri = Uri.parse(base+"/"+prefix)
                .buildUpon()
                .appendPath(project)
                .appendPath(device)
                .build();
        new ParseURL().execute(new String[]{builtUri.toString()});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent prefs = new Intent(getBaseContext(), SetPreferenceActivity.class);
            startActivityForResult(prefs, REQUEST_PREFS);
            run(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void download(String url, String desc, String title, String filename) {
        if (EasyPermissions.hasPermissions(this, perms)) {
            // Have permissions, do the thing!
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(this, "needed",
                    RC_EXT_WRITE, perms);
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(desc);
        request.setTitle(title);

        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String directory = mySharedPreferences.getString("prefDirectory",Environment.DIRECTORY_DOWNLOADS).trim();
        request.setDestinationInExternalPublicDir(directory, filename);


        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Some permissions have been granted
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Some permissions have been denied

    }
    private class ParseURL extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            Log.w(LOGTAG, strings[0]);
            ArrayList<String> urls = new ArrayList<String>();
            try {

                Document doc = Jsoup.connect(strings[0]).get();
                //SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                //String selector = mySharedPreferences.getString("prefSelector",getString(R.string.selector_val)).trim();
                String selector = getString(R.string.selector_val);
                Elements links = doc.select(selector);
                for (Element link : links) {
                    urls.add(link.attr("href"));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return urls.toString();
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String newS = s.substring(1,s.length()-1);
            List<String> array = Arrays.asList(newS.split(","));

            setList(array);

        }
    }

    private class ParseURLDownload extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {
            Log.w(LOGTAG, strings[0]);
            ArrayList<String> urls = new ArrayList<String>();
            try {

                Document doc = Jsoup.connect(strings[0]).get();
                //SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                //String selector = mySharedPreferences.getString("prefSelector",getString(R.string.selector_val)).trim();
                String selector = getString(R.string.selectorDL_val);
                Elements links = doc.select(selector);
                for (Element link : links) {
                    urls.add(link.attr("href"));
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }

            return urls.toString();
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String newS = s.substring(1,s.length()-1);
            List<String> array = Arrays.asList(newS.split(","));
            String url ="";
            for (String i : array) {
                Log.w(LOGTAG,i);
                String prefix = "";
                if (!(i.startsWith("http"))) {
                    prefix = getString(R.string.base_val)+"/";
                }
                url = prefix+i;
            }
            if (!(url.isEmpty())){
                int slash = url.lastIndexOf("/")+1;
                String filename = url.substring(slash+1);
                download(url, "desc", filename, filename);
            }

        }
    }

    public void setList(List<String> values)  {
        ArrayList<String> names = new ArrayList<String>();


        for (String i : values) {
            i = i.trim();
            int slash = i.lastIndexOf("/")+1;
            try {
                names.add(i.substring(slash));
            } catch (Exception e){
                names.add(i);
            }
            String prefix = "";
            if (!(i.startsWith("http"))) {
                SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                prefix = mySharedPreferences.getString("prefBase",getString(R.string.base_val)).trim()+"/";
            }
            urls.add(prefix+i);
        }
        //newest on top
        Collections.reverse(urls);
        Collections.reverse(names);
        // Find the ListView resource.
        ListView mainListView = (ListView) findViewById( R.id.listView );
        ListAdapter listAdapter =  new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);

        // Set the ArrayAdapter as the ListView's adapter.
        mainListView.setAdapter( listAdapter );

        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, final View view,
          int position, long id) {

          String url = urls.get(position);
          /*
          SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
          boolean external = mySharedPreferences.getBoolean("prefExternal",false);
          if (external) {

              Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse(url));
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

              startActivity(intent);
          } else{ */
              new ParseURLDownload().execute(new String[]{url.toString()});
          //}

      }

    });
    }
}
