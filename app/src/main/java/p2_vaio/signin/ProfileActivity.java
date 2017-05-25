package p2_vaio.signin;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.firebase.ui.auth.AuthUI;


import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static p2_vaio.signin.MainActivity.RC_SIGN_IN;

public class ProfileActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView tv;
    private Button logout;
    private GoogleApiClient mGoogleApiClient;


    private static final String TAG = "ProfileActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN = 1 ;
    private static final int RC_PHOTO_PICKER =  2;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";



    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;



    private FirebaseDatabase mfirebasedatabase;
    private DatabaseReference mmessagedatabasereference;
    private ChildEventListener mchildeventlistener ;
    private FirebaseAuth firebaseAuth ;
    private FirebaseAuth.AuthStateListener mAuthStateListener ;
    private FirebaseStorage mFirebaseStorage ;
    private StorageReference mChatPhotosStorageReference ;
    private FirebaseRemoteConfig mFirebaseRemoteConfig ;

    private String mUsername;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("tribune");

        Intent bundle = getIntent();
        final int flag  = bundle.getIntExtra("flag", -1 );
       // Toast.makeText(ProfileActivity.this ,String.valueOf(flag),Toast.LENGTH_LONG).show();
        mUsername = ANONYMOUS;


        mfirebasedatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

       /* if(firebaseAuth.getCurrentUser() ==null)
        {
            //start profile activity
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        FirebaseUser user  = firebaseAuth.getCurrentUser();*/
        mFirebaseStorage=FirebaseStorage.getInstance();
        mFirebaseRemoteConfig=FirebaseRemoteConfig.getInstance();

        String child="messages";
        mmessagedatabasereference = mfirebasedatabase.getReference().child(child);// location is messages
        mChatPhotosStorageReference=mFirebaseStorage.getReference().child("chat_photos"); // location is chat_photos


        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);




        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }

        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click

                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);
                mmessagedatabasereference.push().setValue(friendlyMessage);
                // Clear input box
                mMessageEditText.setText("");
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null)
                {
                    //signed in
/*                    Toast.makeText(MainActivity.this, "You're now signed in. Welcome to FriendlyChat.", Toast.LENGTH_SHORT)*/
                      if(flag==1)
                      {
                          onSignedInInitialize(user.getEmail());
                      }
                    else {
        onSignedInInitialize(user.getDisplayName());
    }
    //onSignedInInitialize(user.getEmail());
}
                else
                        {
                        onSignedOutCleanup();
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        }
        };
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
               // Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
                finish();//finish the activity
            }
        }  else if(requestCode==RC_PHOTO_PICKER && resultCode==RESULT_OK){
            Uri selectedImageUri = data.getData();
            //get reference to store files at location chat_photos
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());
            //upload file to firebase storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>()
                    {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                        {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            // Set the download URL to the message box, so that the user can send it to the database
                            FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUrl.toString());
                            mmessagedatabasereference.push().setValue(friendlyMessage);
                        }
                    });
        }
    }



    @Override
    protected void onPause() {
        super.onPause();

        if (mAuthStateListener != null) {
            firebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        datachDatabaseReadListener();
        mMessageAdapter.clear();
    }

    @Override
    protected void onResume() {
        super.onResume();

        firebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void onSignedInInitialize(String username)
    {
        mUsername = username ;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup(){
        mUsername = ANONYMOUS ; // inset the username
        mMessageAdapter.clear();// clear messages from the adapter
        datachDatabaseReadListener();

    }

    private void attachDatabaseReadListener() {

        if (mchildeventlistener == null) { // if null only then we will create a new listener
            mchildeventlistener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mmessagedatabasereference.addChildEventListener(mchildeventlistener);
        }
    }

    private void datachDatabaseReadListener(){
        if(mchildeventlistener!= null) {
            mmessagedatabasereference.removeEventListener(mchildeventlistener);
            mchildeventlistener = null ;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.profile) {

            startActivity(new Intent(getApplicationContext(), MainProfile.class));
            //do something
            return true;
        }
        if (id == R.id.chat) {

            //do something
            return true;
        }

        if (id == R.id.sign_out_menu) {
            LoginManager.getInstance().logOut();
            firebaseAuth.signOut();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available
                        // via FirebaseRemoteConfig get<type> calls, e.g., getLong, getString.
                        mFirebaseRemoteConfig.activateFetched();

                        // Update the EditText length limit with
                        // the newly retrieved values from Remote Config.
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // An error occurred when fetching the config.
                        Log.w(TAG, "Error fetching config", e);

                        // Update the EditText length limit with
                        // the newly retrieved values from Remote Config.
                        applyRetrievedLengthLimit();
                    }
                });
    }


    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, FRIENDLY_MSG_LENGTH_KEY + " = " + friendly_msg_length);
    }


    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
