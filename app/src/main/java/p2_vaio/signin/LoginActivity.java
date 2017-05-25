package p2_vaio.signin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import static android.widget.Toast.LENGTH_SHORT;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btn;
    private EditText email, password;
    private TextView tv;
    private  int flag=0;
    private ProgressDialog progressdailog;
    private FirebaseAuth firebaseauth;


    private CallbackManager mCallbackManager;


    private SignInButton google;
    private FirebaseAuth.AuthStateListener mAuthStateListener ;
    public static final int RC_SIGN_IN =0;
    private static final String TAG="MAIN_ACTIVITY";
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);
        setTitle("SIGN IN");
        firebaseauth = FirebaseAuth.getInstance();
        progressdailog = new ProgressDialog(this);
        if (firebaseauth.getCurrentUser() != null) {
            //start profile activity
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        }

        btn = (Button) findViewById(R.id.btnregister1);
        email = (EditText) findViewById(R.id.editTextemail1);
        password = (EditText) findViewById(R.id.editTextpassword1);
        tv = (TextView) findViewById(R.id.tvsignin1);

        btn.setOnClickListener(this);
        tv.setOnClickListener(this);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("260280797002-2jjqm51a3qb0qd52kppjnq28sth8vlvd.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
// options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@android.support.annotation.NonNull ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        google = (SignInButton)findViewById(R.id.google_signin2);
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });





        mAuthStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@android.support.annotation.NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user =firebaseAuth.getCurrentUser();
                if(user!=null)
                {
                    progressdailog.dismiss();
                    //String name = user.getDisplayName().toString();
                    //Toast.makeText(LoginActivity.this,name,Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                }

            }
        };

        mCallbackManager = CallbackManager.Factory.create();

        final LoginButton loginButton= (LoginButton) findViewById(R.id.fblogin_button2);
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
                    Toast.makeText(LoginActivity.this, name, Toast.LENGTH_LONG).show();
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
                Toast.makeText(LoginActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();

                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                Toast.makeText(LoginActivity.this, "Error", Toast.LENGTH_SHORT).show();

                // ...
            }
        });

    }

    private void userlogin() {

        String email1 = email.getText().toString().trim();
        String password1 = password.getText().toString().trim();

        if (TextUtils.isEmpty(email1)) {
            Toast.makeText(this, "Enter your Email", Toast.LENGTH_SHORT).show();
            return;
            //email is empty
        }
        if (TextUtils.isEmpty(password1)) {
            Toast.makeText(this, "Enter your Password", Toast.LENGTH_SHORT).show();
            return;
        }
        progressdailog.setMessage("Logging In..");
        progressdailog.show();

        firebaseauth.signInWithEmailAndPassword(email1,password1).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressdailog.dismiss();
                if(task.isSuccessful())
                {
                    Toast.makeText(LoginActivity.this, "Registered", Toast.LENGTH_SHORT).show();
                    flag=1;
                    Intent i=new Intent(LoginActivity.this,ProfileActivity.class);
                    i.putExtra("flag",flag);
                    startActivity(i);
                }
                else
                {
                    Toast.makeText(LoginActivity.this, "Failed. Try again!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    @Override
    public void onClick(View view) {
        if (view == btn) {
            userlogin();
        } else if (view == tv) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Invalid Credential..!!", Toast.LENGTH_LONG).show();
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
            public void onComplete(@android.support.annotation.NonNull Task<AuthResult> task) {

                //Toast.makeText(LoginActivity.this, "ho gaya!", Toast.LENGTH_LONG).show();
                Log.d("AUTH","Sign in with credential:on complete: "+task.isSuccessful());

                if(!task.isSuccessful()){
                    progressdailog.dismiss();
                    Toast.makeText(LoginActivity.this, "Google Authentication Failed!", Toast.LENGTH_SHORT).show();
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
                    public void onComplete(@android.support.annotation.NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            progressdailog.dismiss();
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Facebook Authentication failed.",
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

        startActivity(new Intent(this, MainActivity.class));
    }

}
