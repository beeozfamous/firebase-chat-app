package com.project.helloworst.firebasechatapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static java.lang.Integer.parseInt;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;
    private ViewPager mViewPage;
    private SectionPagesAdapter mSectionPagesAdapter;
    private TabLayout mTabLayout;
    private TextView tool_bar_title;
    private DatabaseReference mUserRef;

    private SOService mService;
    int appSize;
    String URLDownload;
    ProgressDialog bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mService = ApiUtils.getSOService();
        mService.getVersion().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<VersionResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d("Phat", "onErrorVersion "+ e.getMessage());
                    }

                    @Override
                    public void onNext(VersionResponse versionResponse) {
                        Log.d("Phat", "onNextVersion");

                        appSize = versionResponse.getSize();
                        URLDownload = versionResponse.getUrl();
                        String currentVersion = getString(R.string.version);
                        String[] CurrentVersion = currentVersion.split("\\.");
                        String[] NewVersion = versionResponse.getNewVersion().split("\\.");
                        int CurrentMajor = parseInt(CurrentVersion[0]);
                        int CurrentMinor = parseInt(CurrentVersion[1]);
                        int CurrentPatch = parseInt(CurrentVersion[2]);
                        int CHMajor = parseInt(NewVersion[0]);
                        int CHMinor = parseInt(NewVersion[1]);
                        int CHPatch = parseInt(NewVersion[2]);
                        if (CHMajor > CurrentMajor) {

                            CharSequence option[] = new CharSequence[]{"Accept", "Decline"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            builder.setTitle("New version update: "+versionResponse.getNewVersion());
                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0) {
                                        if (checkPermissionsGranted())
                                        {
                                            new DownloadNewVersion().execute();
                                        }else{
                                            requestPermission();
                                        }
                                        dialog.dismiss();
                                    }
                                    if (which == 1) {
                                        dialog.dismiss();
                                    }

                                }
                            });
                            builder.show();

                        } else if (CHMinor > CurrentMinor && CHMajor >= CurrentMajor) {
                            CharSequence option[] = new CharSequence[]{"Accept", "Decline"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            builder.setTitle("New version update: "+versionResponse.getNewVersion());
                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0) {
                                        if (checkPermissionsGranted())
                                        {
                                            new DownloadNewVersion().execute();
                                        }else{
                                            requestPermission();
                                        }
                                        dialog.dismiss();
                                    }
                                    if (which == 1) {
                                        dialog.dismiss();
                                    }

                                }
                            });
                            builder.show();

                        } else if (CHPatch > CurrentPatch && CHMinor >= CurrentMinor && CHMajor >= CurrentMajor) {
                            CharSequence option[] = new CharSequence[]{"Accept", "Decline"};

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                            builder.setTitle("New version update: "+versionResponse.getNewVersion());
                            builder.setItems(option, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (which == 0) {
                                        if (checkPermissionsGranted())
                                        {
                                            new DownloadNewVersion().execute();
                                        }else{
                                            requestPermission();

                                        }
                                        dialog.dismiss();
                                    }
                                    if (which == 1) {
                                        dialog.dismiss();
                                    }

                                }
                            });
                            builder.show();

                        }
                    }
                });



        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }

        mToolbar=findViewById(R.id.main_page_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");

        tool_bar_title=findViewById(R.id.tool_bar_title);
        tool_bar_title.setText("OWl");

        mViewPage=findViewById(R.id.main_tabPager);
        mSectionPagesAdapter= new SectionPagesAdapter(getSupportFragmentManager());
        mViewPage.setAdapter(mSectionPagesAdapter);
        mTabLayout= findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPage);

    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if(currentUser== null){
           upDateUI();
        }
        else{
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }catch (Exception e){
            Log.d("MAIN", "onDestroy: this thing null");
        }
    }

    private void upDateUI() {

        Intent mStartActivity = new Intent(MainActivity.this, StartActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(MainActivity.this, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)MainActivity.this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() , mPendingIntent);
        System.exit(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case(R.id.main_logout_button):
                {
                FirebaseAuth.getInstance().signOut();
                upDateUI();
                break;
            }
            case(R.id.main_setting_button):
            {
                Intent startIntent= new Intent(MainActivity.this,SettingsActivity.class );
                startActivity(startIntent);
                break;
            }
            case(R.id.main_all_users):
            {
                Intent userIntent= new Intent(MainActivity.this,UsersActivity.class);
                startActivity(userIntent);
                break;
            }
        }
        return true;

    }
    class DownloadNewVersion extends AsyncTask<String,Integer,Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            bar = new ProgressDialog(MainActivity.this);
            bar.setCancelable(false);

            bar.setMessage("Downloading...");

            bar.setIndeterminate(true);
            bar.setCanceledOnTouchOutside(false);
            bar.show();

        }

        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            bar.setIndeterminate(false);
            bar.setMax(100);
            bar.setProgress(progress[0]);
            String msg = "";
            if(progress[0]>99){

                msg="Finishing... ";

            }else {

                msg="Downloading... "+progress[0]+"%";
            }
            bar.setMessage(msg);

        }
        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            bar.dismiss();

            if(result){

                Toast.makeText(getApplicationContext(),"Download Completed",
                        Toast.LENGTH_SHORT).show();

            }else{

                Toast.makeText(getApplicationContext(),"Error: Try Again",
                        Toast.LENGTH_SHORT).show();


            }

        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            Boolean flag = false;

            try {

                URL url = new URL(URLDownload);

                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();


                String PATH = Environment.getExternalStorageDirectory()+"/Download/";
                File file = new File(PATH);
                file.mkdirs();

                File outputFile = new File(file,"OWl_DEBUT.apk");

                if(outputFile.exists()){
                    outputFile.delete();
                }

                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = c.getInputStream();

                int total_size = appSize;//size of apk

                byte[] buffer = new byte[1024];
                int len1 = 0;
                int per = 0;
                int downloaded=0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                    downloaded +=len1;
                    per = (int) (downloaded * 100 / total_size);
                    publishProgress(per);
                }
                fos.close();
                is.close();


                File toInstall = new File(PATH + "OWl_DEBUT.apk");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Uri apkUri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", toInstall);
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    intent.setData(apkUri);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    MainActivity.this.startActivity(intent);
                } else {
                    Uri apkUri = Uri.fromFile(toInstall);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MainActivity.this.startActivity(intent);
                }

                flag = true;
            } catch (Exception e) {
                Log.e("Phat", "Update Error: " + e.getMessage());
                Log.e("Phat", "Update Error: " + e.getLocalizedMessage());
                Log.e("Phat", "Update Error: " + e.getCause());


                flag = false;
            }
            return flag;

        }

    }
    @SuppressLint("CheckResult")
    public void requestPermission(){
        RxPermissions rxPermissions = new RxPermissions(MainActivity.this);
        rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        new DownloadNewVersion().execute();
                    } else {
                        Toast.makeText(MainActivity.this,"Action need permisson!",Toast.LENGTH_LONG).show();
                    }
                });
    }
    private boolean checkPermissionsGranted() {
        return ( ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
}
