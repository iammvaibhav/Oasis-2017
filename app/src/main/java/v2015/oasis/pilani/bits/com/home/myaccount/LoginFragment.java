package v2015.oasis.pilani.bits.com.home.myaccount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import v2015.oasis.pilani.bits.com.home.GlobalData;
import v2015.oasis.pilani.bits.com.home.R;


public class LoginFragment extends Fragment {

    EditText username;
    String url="https://bits-oasis.org/2017/api/";
    EditText password;
    Button login;
    ProgressBar progressBar;
    String user;
    String pass;
    public LoginFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        (GlobalData.INSTANCE.getMainActivity()).headerText.setText(" PROFILE ");
        (GlobalData.INSTANCE.getMainActivity()).filter.setVisibility(View.INVISIBLE);

        return inflater.inflate(R.layout.fragment_login, container, false);



    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        username=(EditText) view.findViewById(R.id.username);
        password=(EditText) view.findViewById(R.id.password);
        login=(Button)view.findViewById(R.id.login);
        progressBar=(ProgressBar) view.findViewById(R.id.progressbar);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user=username.getText().toString();
                pass=password.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                getToken(user,pass);
            }
        });
    }

    void getToken(String user, final String password){
        JSONObject authDetails=new JSONObject();
        try{
            authDetails.put("username",user);
            authDetails.put("password",password);
        }catch (JSONException e){
            Log.e("Auth","Json Error");
        }
        AndroidNetworking.post(url+"api_token").addJSONObjectBody(authDetails).build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String token=response.getString("token");
                            Log.e("Token :",token);
                            AndroidNetworking.get(url+"get_profile/")
                                    .addHeaders("Authorization","JWT "+token)
                                    .build().getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    Log.e("Response",response.toString());
                                    progressBar.setVisibility(View.INVISIBLE);

                                    try{
                                        JSONObject participant=response.getJSONObject("participant");
                                        JSONArray profShows=response.getJSONArray("prof_shows");
                                        JSONArray participations=response.getJSONArray("participations");
                                        String events=stringEvents(participations);
                                        String profs=stringProfs(profShows);

                                        Log.e("Strings : ", events+profs);
                                        GlobalData.INSTANCE.getTinyDb().putString("name",participant.getString("name"));
                                        GlobalData.INSTANCE.getTinyDb().putString("college",participant.getString("college_name"));
                                        GlobalData.INSTANCE.getTinyDb().putString("barcode",participant.getString("barcode"));
                                        GlobalData.INSTANCE.getTinyDb().putBoolean("Logged",true);
                                        try {
                                            GlobalData.INSTANCE.getTinyDb().putString("events",events.substring(0,events.length()-2));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        try {
                                            GlobalData.INSTANCE.getTinyDb().putString("profs",profs.substring(0,profs.length()-2));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        GlobalData.INSTANCE.getTinyDb().putString("picUrl",participant.getString("pic_url"));
                                        GlobalData.INSTANCE.getTinyDb().putString("ems",participant.getString("ems_code"));

                                        FragmentTransaction transaction=getFragmentManager().beginTransaction();
                                        ProfileFragment profileFragment= new ProfileFragment();
                                        profileFragment.setArguments(new Bundle());
                                        transaction.replace(R.id.fragmentContainer ,profileFragment);
                                        transaction.commit();
                                    }catch (Exception e){
                                        Toast.makeText(getContext(),"Error in login.Try again.",Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onError(ANError anError) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Log.e("Error", anError.getErrorBody());
                                }
                            });
                        }catch (JSONException e){
                            Log.e("Error","Token JSON");
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        progressBar.setVisibility(View.INVISIBLE);
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


    String stringEvents(JSONArray array){
        String profs="";
        for (int i=0;i<array.length();i++){
            try{
                JSONObject prof=array.getJSONObject(i);
                profs=profs + prof.getString("name") +", ";
            }catch (JSONException e){

            }
        }

        return profs;
    }
}
