package com.pradeep.videoplayercollection;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pradeep.videoplayercollection.dbase.LoginAccess;

import java.io.File;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";
    private Toolbar toolbar;
    private SessionManager mSession;
    private ProgressBar mTaskInProgress;
    private EditText mUserName, mPassword;
    private EditText mFullName, mPhoneNumber, mEmailID, mPasswordRegister, mConfirmPasswordRegister;
    private TextView mLoginStatus, mRegisterView, mLoginView;
    private RelativeLayout mLoginPage, mRegisterPage;
    private Context mContext;
    private Button mUploadImage;
    private int SELECT_PICTURE = 200;
    private ImageView mProfileImage;
    private String mProfileImageUrl;
    private LoginAccess mLoginAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_login);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContext = this;
        mProfileImageUrl = "data/data/com.pradeep.videoplayercollection/profile.jpg";
        mFullName = (EditText) findViewById(R.id.register_name);
        mPhoneNumber = (EditText) findViewById(R.id.register_Number);
        mUploadImage = (Button) findViewById(R.id.upload_profile);
        mProfileImage = (ImageView) findViewById(R.id.profile_img);
        mEmailID = (EditText) findViewById(R.id.register_email);
        mPasswordRegister = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordRegister = (EditText) findViewById(R.id.register_confirm_password);

        mTaskInProgress = (ProgressBar) findViewById(R.id.op_progress_bar);
        mLoginPage = (RelativeLayout) findViewById(R.id.login_page);
        mRegisterPage = (RelativeLayout) findViewById(R.id.register_page);
        mUserName = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);
        mRegisterView = (TextView) findViewById(R.id.register);
        mLoginView = (TextView) findViewById(R.id.login_page_data);
        Button btnLogin = (Button) findViewById(R.id.loginButton);
        mLoginStatus = (TextView) findViewById(R.id.status);
        mLoginAccess = LoginAccess.getInstance(mContext);
        mUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        mRegisterView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegisterPage.setVisibility(View.VISIBLE);
                mLoginPage.setVisibility(View.GONE);
            }
        });
        mLoginView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRegisterPage.setVisibility(View.GONE);
                mLoginPage.setVisibility(View.VISIBLE);
            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginStatus.setVisibility(View.GONE);
                showProgressBar();
                dismissKeyboard();
                String user = mUserName.getText().toString();
                String password = mPassword.getText().toString();
                if (validate(user, password)) {
                    performLogin(user, password);
                } else {
                    Toast.makeText(LoginActivity.this, "Please Enter User Id and Password", Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        });
        Button RegisterUser = (Button)findViewById(R.id.registerButton);

        RegisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLoginStatus.setVisibility(View.GONE);
                showProgressBar();
                dismissKeyboard();
                if (validateRegister()) {
                    performRegister();
                } else {
                    Toast.makeText(LoginActivity.this, "Please Enter valid details", Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        });

        mSession = new SessionManager(mContext);
        if(mSession.isRegister() == false) {
            mRegisterPage.setVisibility(View.VISIBLE);
            mLoginPage.setVisibility(View.GONE);
        }
        if (mSession.isLoggedIn()) {
            Log.v(TAG, "Already logged In");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                Log.e(TAG,"uri:"+selectedImageUri.getPath());
                if (null != selectedImageUri) {
                    mProfileImage.setImageURI(selectedImageUri);
                    BitmapDrawable draw = (BitmapDrawable) mProfileImage.getDrawable();
                    Bitmap bitmap = draw.getBitmap();
                    try {
                        FileOutputStream outStream = null;
                        File outFile = new File(mProfileImageUrl);
                        outFile.delete();
                        outFile.createNewFile();
                        outStream = new FileOutputStream(outFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void performRegister() {
        String name = mFullName.getText().toString();
        String phone = mPhoneNumber.getText().toString();
        String email = mEmailID.getText().toString();
        String password = mPasswordRegister.getText().toString();
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeString(name);
        parcel.writeString(phone);
        parcel.writeString(email);
        parcel.writeString(password);
        int result = mLoginAccess.fetchUserData(parcel,1);
        if(result < 0) {
            Toast.makeText(LoginActivity.this, "Failed to register.", Toast.LENGTH_SHORT).show();
        } else {
            mSession.setRegister(true,mProfileImageUrl,name);
            mRegisterPage.setVisibility(View.GONE);
            mLoginPage.setVisibility(View.VISIBLE);
            Toast.makeText(LoginActivity.this, "Registration completed please login.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateRegister() {
        boolean valid = true;
        String name = mFullName.getText().toString();
        String phone = mPhoneNumber.getText().toString();
        String email = mEmailID.getText().toString();
        String password = mPasswordRegister.getText().toString();
        String reEnterPassword = mConfirmPasswordRegister.getText().toString();
        if (name.isEmpty() || name.length() < 3) {
            mFullName.setError("Enter at least 3 characters");
            valid = false;
        } else {
            mFullName.setError(null);
        }

        if (phone.isEmpty() || phone.length() < 10) {
            mPhoneNumber.setError("Enter valid mobile number");
            valid = false;
        } else {
            mPhoneNumber.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailID.setError("Enter a valid email address");
            valid = false;
        } else {
            mEmailID.setError(null);
        }
        if (password.isEmpty() || !validatePassword(password)) {
            mPasswordRegister.setError("Password must between 8 and 15 characters and At least 1 letter, 1 number, 1 special character and SHOULD NOT start with a special character ");
            valid = false;
        } else {
            mPasswordRegister.setError(null);
        }

        if (reEnterPassword.isEmpty() || !(reEnterPassword.equals(password))) {
            mConfirmPasswordRegister.setError("Password Do not match");
            valid = false;
        } else {
            mConfirmPasswordRegister.setError(null);
        }
        return valid;
    }

    private void performLogin(String user, String passwd) {
        Log.v(TAG, "performLogin");
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeString(user);
        parcel.writeString(passwd);
        int result = mLoginAccess.fetchUserData(parcel,2);
        if(result < 0) {
            Toast.makeText(LoginActivity.this, "Failed to register.", Toast.LENGTH_SHORT).show();
        } else {
            parcel.setDataPosition(0);
            int id = parcel.readInt();
            mSession.setLogin(true, id);
            Toast.makeText(LoginActivity.this, "Login is successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showProgressBar() {
        Log.v(TAG, "showProgressBar");
        if (mTaskInProgress.getVisibility() != View.VISIBLE) {
            Log.v(TAG, "showProgressBar....");
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mTaskInProgress.setVisibility(View.VISIBLE);
        }
    }

    private void hideProgressBar() {
        Log.v(TAG, "hideProgressBar");
        if (mTaskInProgress.getVisibility() == View.VISIBLE) {
            Log.v(TAG, "hideProgressBar....");
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            mTaskInProgress.setVisibility(View.GONE);
        }
    }

    private boolean validate(String user, String passwd) {
        boolean valid = true;

        if (user.isEmpty() || user.length() < 0) {
            mUserName.setError("Enter at least 3 characters");
            valid = false;
        } else {
            mUserName.setError(null);
        }

        if (passwd.isEmpty() || !validatePassword(passwd)) {
            mPassword.setError("Password must between 8 and 15 alphanumeric characters and Atleast 1 letter, 1 number, 1 special character and SHOULD NOT start with a special character ");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        return valid;
    }

    private boolean validatePassword(final String password) {
        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$&_])[A-Za-z\\d][A-Za-z\\d@#$&_]{7,19}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        return matcher.matches();
    }

    private void dismissKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy, state : ");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.v(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
