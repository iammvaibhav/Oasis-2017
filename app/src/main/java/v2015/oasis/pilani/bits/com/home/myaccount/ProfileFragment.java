package v2015.oasis.pilani.bits.com.home.myaccount;

import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import v2015.oasis.pilani.bits.com.home.GlobalData;
import v2015.oasis.pilani.bits.com.home.MainActivity;
import v2015.oasis.pilani.bits.com.home.R;


public class ProfileFragment extends Fragment {

    //TextView header;
    TextView name;
    CircleImageView profileImage;
    TextView college;
    TextView random1;
    TextView random2;
    TextView events;
    TextView profShow;
    ImageView qrcode;
    Button logout;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity)getActivity()).headerText.setText(" PROFILE ");
        ((MainActivity)getActivity()).filter.setVisibility(View.INVISIBLE);

        return inflater.inflate(R.layout.fragment_profile, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        name=(TextView)view.findViewById(R.id.name);
        profileImage=(CircleImageView)view.findViewById(R.id.profile_image);
        college=(TextView)view.findViewById(R.id.college);
        profShow=(TextView)view.findViewById(R.id.prof_show);
        events=(TextView)view.findViewById(R.id.events);
        random1=(TextView)view.findViewById(R.id.barcode);
        random2=(TextView)view.findViewById(R.id.emscode);
        qrcode=(ImageView)view.findViewById(R.id.qrcode);
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("here", "logout");
                MyAccountFragment myAccountFragment = new MyAccountFragment();
                Bundle bundle = new Bundle();
                if (getArguments() == null) {
                    myAccountFragment.setArguments(bundle);
                }
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                GlobalData.INSTANCE.getTinyDb().putBoolean("Logged", false);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, myAccountFragment).commit();
            }
        });
        Typeface typeface= ((MainActivity)getActivity()).getTypefaceBold();
        setAllTypeface(typeface);
        setFields();

    }

    void setAllTypeface(Typeface typeface){
        college.setTypeface(typeface);
        profShow.setTypeface(typeface);
        name.setTypeface(typeface);
        events.setTypeface(typeface);
        random1.setTypeface(typeface);
        random2.setTypeface(typeface);
    }

    void setFields(){
        String info= GlobalData.INSTANCE.getTinyDb().getString("barcode");
        String url=GlobalData.INSTANCE.getTinyDb().getString("picUrl");
        Bitmap bitmap;
        college.setText(GlobalData.INSTANCE.getTinyDb().getString("college"));
        name.setText(GlobalData.INSTANCE.getTinyDb().getString("name"));
        random1.setText("Barcode: "+info);
        random2.setText("EMS Code:" + GlobalData.INSTANCE.getTinyDb().getString("ems"));
        profShow.setText("Prof-Shows : "+GlobalData.INSTANCE.getTinyDb().getString("profs"));
        events.setText("Events: "+GlobalData.INSTANCE.getTinyDb().getString("events"));
        Log.e("URL", url);

        final OkHttpClient client = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                .build();

        final Picasso picasso = new Picasso.Builder(getActivity())
                .downloader(new OkHttp3Downloader(client))
                .build();

        picasso.load(url).into(profileImage);
        MultiFormatWriter multiFormatWriter =new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=multiFormatWriter.encode(info, BarcodeFormat.QR_CODE,dpToPx(180),dpToPx(180));
            BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
            bitmap=barcodeEncoder.createBitmap(bitMatrix);
            qrcode.setImageBitmap(bitmap);
        }catch (WriterException e){
            Log.d("Image Error", e.getStackTrace().toString());
        }catch (Exception e){

        }

    }
}
