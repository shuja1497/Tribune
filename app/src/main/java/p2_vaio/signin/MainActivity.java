package p2_vaio.signin;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInApi;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import static android.R.attr.password;
import static android.widget.Toast.LENGTH_SHORT;
import static p2_vaio.signin.R.id.email;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnregister;
    private EditText etemail, etpassword,name;
    private TextView tvsignin;

    private CallbackManager mCallbackManager;

    private ProgressDialog progressdailog;

    private FirebaseAuth firebaseauth;
    private SignInButton google;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    public static final int RC_SIGN_IN =0;
    private  int flag=0;
    private static final String TAG="MAIN_ACTIVITY";
    private GoogleApiClient mGoogleApiClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        progressdailog = new ProgressDialog(this,R.style.full_screen_dialog);
        progressdailog.setContentView(R.layout.pd);





        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //.requestIdToken("260280797002-2jjqm51a3qb0qd52kppjnq28sth8vlvd.apps.googleusercontent.com")
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        google = (SignInButton)findViewById(R.id.google_signin);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });


        firebaseauth = FirebaseAuth.getInstance();


        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    progressdailog.dismiss();
                    //String name = user.getDisplayName().toString();
                    //Toast.makeText(MainActivity.this,name,Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }

            }
        };

        mCallbackManager = CallbackManager.Factory.create();

        final LoginButton  loginButton= (LoginButton) findViewById(R.id.fblogin_button);
        loginButton.setReadPermissions("email","public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    progressdailog.dismiss();
                    String name = user.getDisplayName();
                    Toast.makeText(MainActivity.this, name, Toast.LENGTH_LONG).show();
                    loginButton.setVisibility(View.INVISIBLE);
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    // User is signed in
                } else {
                    // No user is signed in
                }
                // startActivity(new Intent(MainActivity.this , Main2Activity.class));

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                Toast.makeText(MainActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();

                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();

                // ...
            }
        });

// .

        //findViewById(R.id.google).setOnClickListener(this);

        progressdailog = new ProgressDialog(this);
        // google=(SignInButton)findViewById(R.id.google);
        btnregister=(Button)findViewById(R.id.btnregister);
        etemail=(EditText)findViewById(R.id.editTextemail);
        etpassword=(EditText)findViewById(R.id.editTextpassword);
        tvsignin=(TextView)findViewById(R.id.tvsignin);
        name=(EditText)findViewById(R.id.name);

        btnregister.setOnClickListener(this);
        tvsignin.setOnClickListener(this);


    }



    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Invalid Credential..!!", Toast.LENGTH_LONG).show();
    }

    private void registeruser() {
        String email = etemail.getText().toString().trim();
        String password = etpassword.getText().toString().trim();
        String n = name.getText().toString().trim();


        if (!validate()) {
            onSignupFailed();
        } else {

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Enter your Email", Toast.LENGTH_SHORT).show();
                return;
                //email is empty

            }
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Enter your Password", Toast.LENGTH_SHORT).show();

                return;
                //password is empty
            }
            //progressbar

            progressdailog.setMessage("Registering User..");
            progressdailog.show();
            firebaseauth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MainActivity.this, "Registered", Toast.LENGTH_SHORT).show();
                                flag=1;
                                Intent i=new Intent(MainActivity.this,ProfileActivity.class);
                                i.putExtra("flag",flag);
                                startActivity(i);
                                //startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                            } else {
                                progressdailog.dismiss();
                                Toast.makeText(MainActivity.this, "Failed. Try again!", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }


    public boolean validate() {
        boolean valid = true;

        String n = name.getText().toString();
        String email = etemail.getText().toString();
        String password = etpassword.getText().toString();

        if (n.isEmpty() || name.length() < 3) {
            name.setError("at least 3 characters");
            valid = false;
        } else {
            name.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etemail.setError("enter a valid email address");
            valid = false;
        } else {
            etemail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            etpassword.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            etpassword.setError(null);
        }

        return valid;
    }
    @Override
    public void onClick(View view) {

        if(view==btnregister)
        {
            flag=1;
            registeruser();
        }
        else if(view==tvsignin)
        {
            startActivity(new Intent(this, LoginActivity.class));
        }
/*        else if(view.getId()==R.id.google)
        {
            signIn();

        }*/
        //login activity

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                progressdailog.setMessage("Logging In your Google Account");
                progressdailog.show();
                firebaseAuthWithGoogle(account);

            } else {
                // Google Sign In failed, update UI appropriately
                // ...
                Toast.makeText(this,"google login failed",LENGTH_SHORT).show();
            }
        }


    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseauth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                //Toast.makeText(MainActivity.this, "ho gaya!", Toast.LENGTH_LONG).show();
                Log.d("AUTH","Sign in with credential:on complete: "+task.isSuccessful());

                if(!task.isSuccessful()){
                    progressdailog.dismiss();
                    Toast.makeText(MainActivity.this, "Google Authentication Failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        firebaseauth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            firebaseauth.removeAuthStateListener(mAuthStateListener);
        }
    }


    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        // [START_EXCLUDE silent]
        //showProgressDialog();
        // [END_EXCLUDE]
        progressdailog.setMessage("Logging In your Facebook Account");
        progressdailog.show();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseauth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            progressdailog.dismiss();
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Facebook Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        //hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


}
