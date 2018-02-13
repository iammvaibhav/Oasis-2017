package v2015.oasis.pilani.bits.com.home.selfiecontest

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import v2015.oasis.pilani.bits.com.home.Compressor
import v2015.oasis.pilani.bits.com.home.GlobalData
import v2015.oasis.pilani.bits.com.home.GlobalData.TAG
import v2015.oasis.pilani.bits.com.home.GlobalData.tinyDb
import v2015.oasis.pilani.bits.com.home.MainActivity
import v2015.oasis.pilani.bits.com.home.R
import v2015.oasis.pilani.bits.com.home.databinding.FragmentSelfieMainBinding
import v2015.oasis.pilani.bits.com.home.events.IfOpenThenCloseListener
import v2015.oasis.pilani.bits.com.home.utils.StyleToasts
import java.io.File
import java.lang.Exception

interface selfiItemClickListener{
    fun onItemClick(selfie: Selfie, requestCode: Int)
}

class SelfieMainPageFragment : Fragment(){

    lateinit var notifyManager: NotificationManager
    lateinit var builder: NotificationCompat.Builder
    lateinit var progressBar: ProgressBar
    lateinit var listener: selfiItemClickListener

    val PICK_FROM_GALLERY = 4345
    val OPEN_CAMERA = 9765


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.e(TAG, "on attach")
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentSelfieMainBinding.inflate(inflater, container, false)
        val userID = tinyDb.getString("facebook_id")
        var openedSelfie: Selfie? = null
        val bottomSheetBehaviour = BottomSheetBehavior.from(binding.bottomSheetParent)
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN

        listener = object : selfiItemClickListener {
            override fun onItemClick(selfie: Selfie, requestCode: Int) {
                if (requestCode == 0) {
                    openedSelfie = selfie
                    binding.remove.visibility = View.INVISIBLE
                    binding.caption.text = selfie.caption
                    binding.description.text = selfie.description
                    binding.like.isChecked = selfie?.likes?.contains(userID) ?: false
                    binding.noOfLikes.text = selfie.noOfLikes.toString()
                    binding.like.setOnTouchListener { v, event -> false }
                    Picasso.with(GlobalData.mainActivity).load(selfie.fullImageUrl).placeholder(R.drawable.placeholder).fit().centerInside().into(binding.image)
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }else{
                    binding.remove.visibility = View.VISIBLE
                    binding.caption.text = selfie.caption
                    binding.description.text = selfie.description
                    binding.like.isChecked = selfie.noOfLikes > 0
                    binding.noOfLikes.text = selfie.noOfLikes.toString()
                    binding.remove.setOnClickListener {
                        Toast.makeText(GlobalData.mainActivity, "Preparing to delete", Toast.LENGTH_LONG).show()
                        FirebaseDatabase.getInstance().getReference("SelfieContest").child("Selfies").child(selfie.selfieID).removeValue().addOnCompleteListener {
                            FirebaseDatabase.getInstance().getReference("SelfieContest").child("People").child(userID).child("selfies").child(selfie.selfieID).removeValue().addOnCompleteListener {
                                //deleted selfie\
                                Toast.makeText(GlobalData.mainActivity, "Successfully deleted", Toast.LENGTH_SHORT).show()
                                bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                            }
                        }
                    }
                    binding.like.setOnTouchListener { v, event -> true }
                    Picasso.with(GlobalData.mainActivity).load(selfie.fullImageUrl).placeholder(R.drawable.placeholder).fit().centerInside().into(binding.image)
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }

        binding.close.setOnClickListener{
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
        }

        (GlobalData.mainActivity as MainActivity).closeListenerSelfie = object : IfOpenThenCloseListener {
            override fun ifOpenThenClose(): Boolean {
                if (bottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehaviour.state = BottomSheetBehavior.STATE_HIDDEN
                    return true
                }
                return false
            }
        }

        binding.like.setOnCheckStateChangeListener { view, checked ->
            val selfieID = openedSelfie?.selfieID

            if (checked){
                if (openedSelfie?.likes == null){
                    val c = ArrayList<String>()
                    c.add(userID)
                    openedSelfie?.likes = c
                    FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).setValue(openedSelfie).addOnCompleteListener {
                        FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").setValue((dataSnapshot.value as Long) + 1L)
                                binding.noOfLikes.text = binding.noOfLikes.text.toString().toInt().plus(1).toString()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    }
                }else{
                    openedSelfie?.likes?.add(userID)
                    FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).setValue(openedSelfie).addOnCompleteListener {
                        FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").setValue((dataSnapshot.value as Long) + 1L)
                                binding.noOfLikes.text = binding.noOfLikes.text.toString().toInt().plus(1).toString()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {

                            }
                        })
                    }
                }

            }else{
               val index = openedSelfie?.likes?.indexOf(userID) ?: -1
                if (index != -1){
                    openedSelfie?.likes?.removeAt(index)

                    FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).setValue(openedSelfie).addOnCompleteListener {
                        FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                FirebaseDatabase.getInstance().reference.child("SelfieContest").child("Selfies").child(selfieID).child("noOfLikes").setValue((dataSnapshot.value as Long) - 1L)
                                binding.noOfLikes.text = binding.noOfLikes.text.toString().toInt().minus(1).toString()
                            }

                            override fun onCancelled(databaseError: DatabaseError) {}
                        })

                    }

                }
            }

        }

        binding.viewpager.adapter = ViewPagerAdapter(childFragmentManager, tinyDb.getString("facebook_id"))
        binding.tabs.setupWithViewPager(binding.viewpager)
        progressBar = binding.progressbar

        notifyManager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        builder = NotificationCompat.Builder(activity)
        builder.setContentTitle("Image Upload")
                .setContentText("Upload in progress")
                .setSmallIcon(R.drawable.ic_upload)

        Picasso.with(activity).load(R.drawable.nav_drawer_background).fit().centerCrop().into(binding.background)


        binding.camera.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), OPEN_CAMERA)
            }else{
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePicture.resolveActivity(activity.packageManager) != null)
                    EasyImage.openCamera(this@SelfieMainPageFragment, 0)
                else
                    Toast.makeText(activity, "No camera app installed!", Toast.LENGTH_LONG).show()
            }
            binding.fab.close(true)
        }

        binding.browse.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PICK_FROM_GALLERY)
            }else{
                val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                if (pickPhoto.resolveActivity(activity.packageManager) != null)
                    EasyImage.openGallery(this@SelfieMainPageFragment, 0)
                else
                    Toast.makeText(activity, "No gallery/photos app installed!", Toast.LENGTH_LONG).show()
            }
            binding.fab.close(true)
        }

        binding.logout.setOnClickListener {
            binding.fab.close(true)
            disconnectFromFacebook()
            progressBar.visibility = View.VISIBLE
        }

        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        Log.e(TAG, "on detach")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.e(TAG, "activity created")
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "resume created")
        Log.e("is null", "${(activity == null)}" )
    }

    fun disconnectFromFacebook() {

        if (AccessToken.getCurrentAccessToken() == null) {
            return  // already logged out
        }

        GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null,
                HttpMethod.DELETE, GraphRequest.Callback {
            LoginManager.getInstance().logOut()
            GlobalData.mainActivity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, SelfieContestFragment())?.commit()
        }).executeAsync()
    }

    fun uploadImage(imageFile: File){

        val st = StyleToasts.loadingToast("Uploading...", activity)
        progressBar.visibility = View.VISIBLE

        st.setDuration(Toast.LENGTH_LONG)
        st.show()
        val thumbnail = Compressor(activity)
                .setMaxHeight(50)
                .setMaxHeight(50)
                .setQuality(20)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).absolutePath)
                .compressToFile(imageFile)
        val actualImage = Compressor(activity).compressToFile(imageFile)

        //val c = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(actualImage.absolutePath), 100, 100)

        val imageID = System.currentTimeMillis().toString()
        val userID = tinyDb.getString("facebook_id")

        FirebaseStorage.getInstance().getReference("thumbnail")
                .child(userID)
                .child(imageID)
                .putFile(Uri.fromFile(thumbnail))
                .addOnProgressListener { _ ->
                    builder.setContentText("Preparing to Upload").setProgress(0, 0, true)
                    notifyManager.notify(21, builder.build())
                }
                .addOnSuccessListener { taskSnapshot ->
                    val thumbnailUrl = taskSnapshot.downloadUrl.toString()
                    FirebaseStorage.getInstance().getReference("images")
                            .child(userID)
                            .child(imageID)
                            .putFile(Uri.fromFile(actualImage))
                            .addOnProgressListener { _ ->
                                builder.setContentText("Uploading").setProgress(0, 0, true)
                                notifyManager.notify(21, builder.build())
                                //show progress
                            }
                            .addOnSuccessListener { taskSnapshot ->
                                progressBar.visibility = View.INVISIBLE
                                builder.setContentText("Successfully Uploaded")
                                        .setProgress(0, 0, false)
                                notifyManager.notify(21, builder.build())
                                val fullSizeURL = taskSnapshot.downloadUrl.toString()
                                //Successfully Uploaded
                                val fragment = EnterDataFragment()
                                val arguments = Bundle()
                                arguments.putString("imageID", imageID)
                                arguments.putString("userID", userID)
                                arguments.putString("thumbnailUrl", thumbnailUrl)
                                arguments.putString("fullSizeImageUrl", fullSizeURL)
                                fragment.arguments = arguments

                                GlobalData.mainActivity?.supportFragmentManager?.beginTransaction()?.replace(R.id.fragmentContainer, fragment)?.commit()
                            }
                            .addOnFailureListener { exception ->
                                val st = StyleToasts.errorToast("Error! Please try again", activity)
                                st.setDuration(Toast.LENGTH_SHORT)
                                st.show()
                                builder.setContentText("Error!")
                                        .setProgress(0, 0, false)
                                notifyManager.notify(21, builder.build())
                                exception.printStackTrace()
                            }
                }
                .addOnFailureListener { exception ->
                    val st = StyleToasts.errorToast("Error! Please try again", activity)
                    st.setDuration(Toast.LENGTH_SHORT)
                    st.show()
                    builder.setContentText("Error!")
                            .setProgress(0, 0, false)
                    notifyManager.notify(21, builder.build())
                    exception.printStackTrace()
                }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PICK_FROM_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    if (pickPhoto.resolveActivity(activity.packageManager) != null)
                        EasyImage.openGallery(this@SelfieMainPageFragment, 0)
                    else
                        Toast.makeText(activity, "No gallery/photos app installed!", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(activity, "Please allow permissions to select image from gallery", Toast.LENGTH_LONG).show()
                }
            }
            OPEN_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    if (takePicture.resolveActivity(activity.packageManager) != null)
                        EasyImage.openCamera(this@SelfieMainPageFragment, 0)
                    else
                        Toast.makeText(activity, "No camera app installed!", Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(activity, "Please allow permissions to open camera", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        EasyImage.handleActivityResult(requestCode, resultCode, data, activity, object : DefaultCallback() {
            override fun onImagePicked(imageFile: File, source: EasyImage.ImageSource, type: Int) {
                uploadImage(imageFile)
                Log.e("is null on actiult", "${activity == null}")
            }

            override fun onImagePickerError(e: Exception, source: EasyImage.ImageSource, type: Int) {
                Toast.makeText(activity, "Error! Please try again", Toast.LENGTH_LONG).show()
            }

            override fun onCanceled(source: EasyImage.ImageSource, type: Int) {
                if (source == EasyImage.ImageSource.CAMERA) {
                    val photoFile = EasyImage.lastlyTakenButCanceledPhoto(activity)
                    photoFile?.delete()
                }
            }
        })

        super.onActivityResult(requestCode, resultCode, data)
    }
}