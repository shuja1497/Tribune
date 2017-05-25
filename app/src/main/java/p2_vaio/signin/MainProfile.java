package p2_vaio.signin;

import android.content.Intent;
import android.support.annotation.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static p2_vaio.signin.MainActivity.RC_SIGN_IN;

public class MainProfile extends AppCompatActivity  {

    private static final String TAG = "";
    private FirebaseAuth firebaseauth;
    private TextView name;
    private FirebaseAuth.AuthStateListener mAuthStateListener;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profile);

        name = (TextView)findViewById(R.id.profile_name) ;

        firebaseauth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseauth.getCurrentUser();



        if (user == null) {
            //start profile activity
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }


        name.setText(user.getDisplayName());


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu1, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.profile) {

            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            //do something
            return true;
        }
        if (id == R.id.chat) {

            //do something
            return true;
        }

        if (id == R.id.sign_out_menu) {


            firebaseauth.signOut();
            LoginManager.getInstance().logOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

}