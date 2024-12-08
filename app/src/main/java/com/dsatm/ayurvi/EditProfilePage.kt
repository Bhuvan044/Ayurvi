package com.dsatm.ayurvi

import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class EditProfilePage : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    var storageReference: StorageReference? = null
    var storagepath: String = "Users_Profile_Cover_image/"
    var uid: String? = null
    var set: ImageView? = null
    var profilepic: TextView? = null
    var editname: TextView? = null
    var editpassword: TextView? = null
    var pd: ProgressDialog? = null
    var imageUri: Uri? = null
    var profileOrCoverPhoto: String? = null

    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile_page)

        initializeViews()
        initializeFirebase()
        initializePermissionLaunchers()
        loadUserProfile()

        editpassword?.setOnClickListener {
            pd!!.setMessage("Changing Password")
            showPasswordChangeDialog()
        }

        profilepic?.setOnClickListener {
            pd!!.setMessage("Updating Profile Picture")
            profileOrCoverPhoto = "image"
            showImagePicDialog()
        }

        editname?.setOnClickListener {
            pd!!.setMessage("Updating Name")
            showNamePhoneUpdate("name")
        }
    }

    private fun initializeViews() {
        profilepic = findViewById(R.id.profilepic)
        editname = findViewById(R.id.editname)
        set = findViewById(R.id.setting_profile_image)
        pd = ProgressDialog(this).apply {
            setCanceledOnTouchOutside(false)
        }
        editpassword = findViewById(R.id.changepassword)
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        storageReference = FirebaseStorage.getInstance().getReference()
        databaseReference = firebaseDatabase!!.getReference("Users")
    }

    private fun initializePermissionLaunchers() {
        cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pickFromCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                Log.d("imageUri",imageUri.toString())
                uploadProfileCoverPhoto(imageUri)
            }
        }
    }

    private fun loadUserProfile() {
        val query = databaseReference!!.orderByChild("email").equalTo(firebaseUser!!.email)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshot1 in dataSnapshot.children) {
                    val image = dataSnapshot1.child("image").value.toString()
                    Glide.with(this@EditProfilePage).load(image).into(set!!)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    private fun showPasswordChangeDialog() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_update_password, null)
        val oldpass = view.findViewById<EditText>(R.id.oldpasslog)
        val newpass = view.findViewById<EditText>(R.id.newpasslog)
        val editpass = view.findViewById<Button>(R.id.updatepass)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)
        val dialog = builder.create()
        dialog.show()

        editpass.setOnClickListener {
            val oldp = oldpass.text.toString().trim()
            val newp = newpass.text.toString().trim()
            if (TextUtils.isEmpty(oldp)) {
                Toast.makeText(this, "Current Password can't be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(newp)) {
                Toast.makeText(this, "New Password can't be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            updatePassword(oldp, newp)
        }
    }

    private fun updatePassword(oldp: String, newp: String) {
        pd!!.show()
        val user = firebaseAuth!!.currentUser
        val authCredential = EmailAuthProvider.getCredential(user!!.email!!, oldp)
        user.reauthenticate(authCredential)
            .addOnSuccessListener {
                user.updatePassword(newp)
                    .addOnSuccessListener {
                        pd!!.dismiss()
                        Toast.makeText(this, "Password Changed", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        pd!!.dismiss()
                        Toast.makeText(this, "Failed to Change Password", Toast.LENGTH_LONG).show()
                    }
            }.addOnFailureListener {
                pd!!.dismiss()
                Toast.makeText(this, "Failed to Authenticate Old Password", Toast.LENGTH_LONG).show()
            }
    }

    private fun showNamePhoneUpdate(key: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update $key")

        // Create layout for new name input
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
            addView(EditText(this@EditProfilePage).apply {
                hint = "Enter $key"
            })
        }
        builder.setView(layout)

        builder.setPositiveButton("Update") { dialog, which ->
            val value = (layout.getChildAt(0) as EditText).text.toString().trim()
            if (!TextUtils.isEmpty(value)) {
                pd!!.show()
                val result = hashMapOf<String, Any>(key to value)
                databaseReference!!.child(firebaseUser!!.uid).updateChildren(result)
                    .addOnSuccessListener {
                        pd!!.dismiss()
                        Toast.makeText(this, "Updated", Toast.LENGTH_LONG).show()
                    }.addOnFailureListener {
                        pd!!.dismiss()
                        Toast.makeText(this, "Unable to Update", Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(this, "Unable to Update", Toast.LENGTH_LONG).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which -> pd!!.dismiss() }
        builder.create().show()
    }

    private fun showImagePicDialog() {
        val options = arrayOf("Camera", "Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Image From")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        pickFromCamera()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
                1 -> {
                    pickFromGallery()
                }
            }
        }
        builder.create().show()
    }

    private fun pickFromCamera() {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "Temp_pic")
            put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
        }
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }
        startActivityForResult(cameraIntent, IMAGE_PICK_CAMERA_CODE)
    }

    private fun pickFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(galleryIntent)
    }

    private fun uploadProfileCoverPhoto(imageUri: Uri?) {
        // Check if the image URI is null
        if (imageUri == null) {
            Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Show the progress dialog
        pd?.show()

        // Define the file path in Firebase Storage
        val filePathAndName = "$storagepath${firebaseUser!!.uid}.png"

        // Create a reference to the Firebase Storage
        val reference = storageReference?.child(filePathAndName)

        // Start the upload process
        reference?.putFile(imageUri)
            ?.addOnSuccessListener { taskSnapshot ->
                // Get the download URL
                reference.downloadUrl.addOnSuccessListener { uri ->
                    // Prepare the result for the database
                    val result = hashMapOf("image" to uri.toString())

                    // Update the user's profile in the database
                    databaseReference!!.child(firebaseUser!!.uid).updateChildren(result as Map<String, Any>)
                        .addOnSuccessListener {
                            pd!!.dismiss()
                            Toast.makeText(this, "Image Updated", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            pd!!.dismiss()
                            Toast.makeText(this, "Unable to Update Image", Toast.LENGTH_LONG).show()
                        }
                }.addOnFailureListener { downloadUriException ->
                    pd!!.dismiss()
                    Toast.makeText(this, "Error fetching download URL: ${downloadUriException.message}", Toast.LENGTH_LONG).show()
                }
            }
            ?.addOnFailureListener { uploadException ->
                pd!!.dismiss()
                // Log the error message for debugging
                Log.e("Upload Error", uploadException.message ?: "Unknown error")
                Toast.makeText(this, "Unable to Upload Image: ${uploadException.message}", Toast.LENGTH_LONG).show()
            }
    }


    companion object {
        private const val IMAGE_PICK_CAMERA_CODE = 1000
    }
}
