package v2015.oasis.pilani.bits.com.home.myaccount;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import v2015.oasis.pilani.bits.com.home.GlobalData;
import v2015.oasis.pilani.bits.com.home.MainActivity;
import v2015.oasis.pilani.bits.com.home.R;
import v2015.oasis.pilani.bits.com.home.utils.StyleToasts;

public class MyAccountFragment extends Fragment {

    String url="https://bits-oasis.org/2017/api/";
    String key="7f4add320a0c4bd5906d6c28b7b47b09";
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    Button login;
    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private SignInButton signInButton;

    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ((MainActivity)GlobalData.INSTANCE.getMainActivity()).headerText.setText(" PROFILE ");
        ((MainActivity)GlobalData.INSTANCE.getMainActivity()).filter.setVisibility(View.INVISIBLE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();


        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            try {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .enableAutoManage(getActivity() /* FragmentActivity */, new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                            }
                        } /* OnConnectionFailedListener */)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mAuth = FirebaseAuth.getInstance();

        if (getArguments() != null){
            Log.e("isConnected", String.valueOf(mGoogleApiClient.isConnected()));
            Log.e("isConnecting", String.valueOf(mGoogleApiClient.isConnecting()));
            mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(@Nullable Bundle bundle) {
                    Log.e("yess", String.valueOf(mGoogleApiClient.isConnected()));
                    signOut();
                }

                @Override
                public void onConnectionSuspended(int i) {

                }
            });
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootView = inflater.inflate(R.layout.fragment_gmail, container, false);
        login=(Button) rootView.findViewById(R.id.login);

        progressBar = rootView.findViewById(R.id.progressbar);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("Clicked","Here");
                FragmentTransaction fragmentTransaction= GlobalData.INSTANCE.getMainActivity().getSupportFragmentManager().beginTransaction();
                LoginFragment fragment= new LoginFragment();
                fragmentTransaction.replace(R.id.fragmentContainer ,fragment);
                fragmentTransaction.commit();
            }
        });

        signInButton=(SignInButton) rootView.findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        Log.d("GARG", "***** on Stop ***** ");
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            Log.d("GARG", "***** on Stop mGoogleApiClient disconnect ***** ");

            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                StyleableToast st = StyleToasts.INSTANCE.errorToast("Error! Please try again", getActivity());
                st.setDuration(Toast.LENGTH_LONG);
                st.show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        progressBar.setVisibility(View.VISIBLE);
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            check(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(getContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        progressBar.setVisibility(View.INVISIBLE);
                    }

                    private void check(FirebaseUser user) {
                        String Email=user.getEmail();

                        if(!Email.contains("@pilani.bits-pilani.ac.in"))
                        {
                            Toast.makeText(getContext(),"Please login through Bits mail only" , Toast.LENGTH_LONG).show();
                            signOut();
                        }
                        else{
                            Uri uri=user.getPhotoUrl();
                            String imageUrl=uri.toString();
                            putDetails(Email,imageUrl);

                        }
                    }
                });
    }

    private void signIn() {
        try {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error! Please try again", Toast.LENGTH_LONG);
        }
    }

    private void signOut() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        //updateUI(null);
                    }
                });

    }
    void putDetails(String email, final String imageUrl){
        JSONObject body=new JSONObject();
        Log.e("Here", "In putDetails");
        try {
            body.put("email",email);
            Log.e("email", email);
            body.put("unique_key",key);
        }catch (JSONException e){

        }
        AndroidNetworking.post(url+"get_profile_bitsian/").addJSONObjectBody(body).build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("Response",response.toString());
                        try {
                            JSONArray array = response.getJSONArray("prof_shows");
                            String profs = stringProfs(array);
                            response=response.getJSONObject("bitsian");
                            String username=response.getString("name");
                            String barcode=response.getString("barcode");
                            GlobalData.INSTANCE.getTinyDb().putString("barcode",barcode);
                            GlobalData.INSTANCE.getTinyDb().putString("events","");
                            GlobalData.INSTANCE.getTinyDb().putString("name",username);
                            GlobalData.INSTANCE.getTinyDb().putString("college","Birla Institute of Technology and Science, Pilani");
                            GlobalData.INSTANCE.getTinyDb().putBoolean("Logged",true);
                            GlobalData.INSTANCE.getTinyDb().putString("picUrl",imageUrl);
                            Log.e("Image Url",imageUrl);
                            try {
                                GlobalData.INSTANCE.getTinyDb().putString("profs",profs.substring(0, profs.length() - 2));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            GlobalData.INSTANCE.getTinyDb().putString("ems",response.getString("ems_code"));
                            progressBar.setVisibility(View.INVISIBLE);
                            FragmentTransaction fragmentTransaction= GlobalData.INSTANCE.getMainActivity().getSupportFragmentManager().beginTransaction();
                            ProfileFragment fragment=new ProfileFragment();
                            fragmentTransaction.replace(R.id.fragmentContainer ,fragment);
                            fragmentTransaction.commit();
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.e("error", anError.getErrorDetail());
                        Log.e("error1", String.valueOf(anError.getErrorCode()));
                        Log.e("error", anError.getResponse().toString());
                        StyleableToast st = StyleToasts.INSTANCE.errorToast("Error! Please try again", getActivity());
                        st.setDuration(Toast.LENGTH_LONG);
                        st.show();
                    }


                });
    }

    String stringProfs(JSONArray array){
        String profs="";
        for (int i=0;i<array.length();i++){
            try{
                JSONObject prof=array.getJSONObject(i);
                profs=profs + prof.getString("prof_show_name") + " - " + prof.getString("count")+", ";
            }catch (JSONException e){

            }
        }

        return profs;
    }

//    String getBarCode(String email){
//        String year=email.substring(3,5);
//        String barcode;
//        if(year.equals("17")){
//            barcode=email.charAt(0)+year+email.substring(5,9);
//        }else {
//            barcode=email.charAt(0)+year+"0"+email.substring(5,8);
//        }
//        return barcode;
//    }
}