package com.pradeep.videoplayercollection;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static android.service.notification.Condition.SCHEME;

public class AppDisplayActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppDataDisplayAdaptor mAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private Toolbar mToolbar;
    private  Dialog myDialog;
    private Activity mActivity;
    private Context mContext;
    private List<Object> mAllInstalledAppList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_display);
        mActivity = this;
        mContext = this;
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerViewLayoutManager = new GridLayoutManager(AppDisplayActivity.this, 1);
        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        mAllInstalledAppList = GetAllInstalledApkInfo();
        mAdapter = new AppDataDisplayAdaptor(AppDisplayActivity.this, mAllInstalledAppList, new CustomItemClickListener() {
            @Override
            public void onItemClick(View v, final int position, final List<Object> list) {
                String packageName = (String)list.get(position);
                Intent intent = AppDisplayActivity.this.getPackageManager().getLaunchIntentForPackage(packageName);
                if(intent != null){
                    AppDisplayActivity.this.startActivity(intent);
                }
                else {
                    Toast.makeText(AppDisplayActivity.this,packageName + " Error, Please Try Again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public boolean onItemLongClick(View v, final int position, final List<Object> appInstallList) {
                Context wrapper = new ContextThemeWrapper(AppDisplayActivity.this, R.style.programClickStyle);
                PopupMenu popup = new PopupMenu(wrapper, v);
                popup.inflate(R.menu.app_menu_option);
                popup.setGravity(Gravity.CENTER);
                setForceShowIcon(popup);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.share:
                                shareData((String) mAllInstalledAppList.get(position));
                                break;
                            case R.id.detail:
                                startApplicationDetailsActivity((String) mAllInstalledAppList.get(position));
                                break;
                            case R.id.uninstall: {
                                showAlert(1,position,0);
                                break;
                            }
                        }
                        return false;
                    }
                });
                popup.show();
                return true;
            }
        });
        recyclerView.setAdapter(mAdapter);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(mToolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setVisibility(View.VISIBLE);
    }

    public static void setForceShowIcon(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private void shareData(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            File srcFile = new File(ai.publicSourceDir);
            Intent share = new Intent();
            share.setAction(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            share.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.setType("application/vnd.android.package-archive");
            share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(srcFile));
            startActivity(Intent.createChooser(share, "PersianCoders"));
        } catch (Exception e) {
            Log.e("ShareApp", e.getMessage());
        }
    }

    private void startApplicationDetailsActivity(String packageName) {
        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        startActivity(intent);
    }

    private void showAlert(final int what, final int etc1,final int etc2) {
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                myDialog = new Dialog(mContext);
                myDialog.setContentView(R.layout.app_display_popup);
                final TextView text_dialog = (TextView) myDialog.findViewById(R.id.text_dialog);
                final TextView dialog_tital = (TextView) myDialog.findViewById(R.id.app_tital);
                final Button btn_yes = (Button) myDialog.findViewById(R.id.btn_yes);
                final Button btn_no = (Button) myDialog.findViewById(R.id.btn_no);
                dialog_tital.setText("ALERT");
                btn_yes.setText(R.string.ok);
                btn_yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myDialog.dismiss();
                    }
                });
                switch (what) {
                    case 1: {
                        btn_yes.setText("conform");
                        text_dialog.setText("Are you sure want to uninstall app?");
                        btn_yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_DELETE);
                                    intent.setData(Uri.parse("package:" + mAllInstalledAppList.get(etc1)));
                                    startActivity(intent);
                                    Toast.makeText(mActivity, "Successfully uninstalled app", Toast.LENGTH_SHORT).show();
                                    mAllInstalledAppList.remove(etc1);
                                    mAdapter.notifyDataSetChanged();
                                }catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(mActivity, "failed to uninstalled app", Toast.LENGTH_SHORT).show();
                                }
                                myDialog.dismiss();
                            }
                        });

                        btn_no.setText("Cancel");
                        btn_no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                myDialog.dismiss();
                            }
                        });
                    }
                    default:
                        Log.w("AppDisplay", "No Such case found");
                }
                myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                myDialog.show();
            }
        });
    }


    public List<Object> GetAllInstalledApkInfo() {
        List<Object> appPackageName = new ArrayList<>();
        Map<String,String> appList = new TreeMap<>();
        final PackageManager pm = AppDisplayActivity.this.getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        appList.putAll(getPackagesOfDialerApps(AppDisplayActivity.this));
        for (ApplicationInfo listInfo : packages) {
            if(pm.getLaunchIntentForPackage(listInfo.packageName) != null) {
                appList.put(GetAppName(listInfo.packageName),listInfo.packageName);
            }
        }
        for (String packageInfo : appList.values()) {
            appPackageName.add(packageInfo);
        }
        return appPackageName;
    }

    public void onBackPressed() {
        Intent i = new Intent(AppDisplayActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
        finish();
    }
    public Map<String,String> getPackagesOfDialerApps(Context context){
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        Map<String,String> appList = new HashMap<>();
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, 0);
        for(ResolveInfo resolveInfo : resolveInfos){
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            appList.put(GetAppName(activityInfo.packageName),activityInfo.packageName);
        }
        return appList;
    }

    public String GetAppName(String ApkPackageName) {
        String Name = "";
        ApplicationInfo applicationInfo;
        PackageManager packageManager = AppDisplayActivity.this.getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(ApkPackageName, 0);
            if (applicationInfo != null) {
                Name = (String) packageManager.getApplicationLabel(applicationInfo);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return Name;
    }
}