package com.dsatm.ayurvi


import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener



class ProfileFragment : Fragment() {
    private var firebaseAuth: FirebaseAuth? = null
    var firebaseUser: FirebaseUser? = null
    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null
    var avatartv: ImageView? = null
    var nam: TextView? = null
    var email: TextView? = null
    var postrecycle: RecyclerView? = null
    var fab: FloatingActionButton? = null
    var pd: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // creating a view to inflate the layout
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)
        firebaseAuth = FirebaseAuth.getInstance()


        // getting current user data
        firebaseUser = firebaseAuth!!.currentUser
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase!!.getReference("Users")


        // Initialising the text view and imageview
        avatartv = view.findViewById<ImageView>(R.id.avatartv)
        nam = view.findViewById<TextView>(R.id.nametv)
        email = view.findViewById<TextView>(R.id.emailtv)
        fab = view.findViewById<FloatingActionButton>(R.id.fab)
        pd = ProgressDialog(activity)
        pd!!.setCanceledOnTouchOutside(false)
        val query = databaseReference!!.orderByChild("email").equalTo(
            firebaseUser!!.email
        )

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (dataSnapshot1 in dataSnapshot.children) {
                    // Log the data to ensure it's being retrieved
                    Log.d("FirebaseData", "Name: ${dataSnapshot1.child("name").value}")
                    Log.d("FirebaseData", "Email: ${dataSnapshot1.child("email").value}")
                    Log.d("FirebaseData", "Image: ${dataSnapshot1.child("image").value}")

                    val name = dataSnapshot1.child("name").value?.toString() ?: "Your Name"
                    val emaill = dataSnapshot1.child("email").value?.toString() ?: "Your Email"
                    val image = dataSnapshot1.child("image").value?.toString() ?: ""

                    // Setting data to our text views
                    nam?.text = name
                    email?.text = emaill

                    // Loading the image using Glide
                    image.takeIf { it.isNotEmpty() }?.let { imageUrl ->
                        avatartv?.let { imageView ->
                            context?.let { safeContext ->
                                Glide.with(safeContext)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_users)  // Optional placeholder
                                    .error(R.drawable.ic_account)      // Optional error image
                                    .into(imageView)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FirebaseError", "Error: ${databaseError.message}")
            }
        })



        // On click we will open EditProfileActivity
        fab?.setOnClickListener {
            startActivity(
                Intent(
                    activity,
                    EditProfilePage::class.java
                )
            )
        }
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
}
