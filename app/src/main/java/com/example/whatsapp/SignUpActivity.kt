package com.example.whatsapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.whatsapp.databinding.ActivitySignUpBinding
import com.example.whatsapp.model.User
import com.google.android.gms.tasks.Continuation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class SignUpActivity : AppCompatActivity() {


    val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val auth by lazy {
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance()
    }

    private lateinit var downloadUrl:String
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.userImgView.setOnClickListener {
            checkPermissionForImage()
//            Firebase Extension

            binding.nextBtn.setOnClickListener {
                val name = binding.nameEt.text.toString()
                if (!::downloadUrl.isInitialized) {
                    toast("Photo cannot be empty")
                } else if (name.isEmpty()) {
                    toast("Name cannot be empty")
                } else {
                    val user = User(name, downloadUrl, downloadUrl/*Needs to thumbnail url*/, auth.uid!!)
                    database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }.addOnFailureListener {
                        binding.nextBtn.isEnabled = true
                    }
                }
            }
        }
    }

    private fun toast(s: String) {
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionWrite,
                    1002
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent=Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        startActivityForResult(intent,1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK&&requestCode==1000){
            data?.data?.let {
                binding.userImgView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(it: Uri) {
        binding.nextBtn.isEnabled=false
        val ref:StorageReference= storage.getReference("uploads/" + auth.uid.toString())
        val uploadTask: UploadTask=ref.putFile(it)
        uploadTask.continueWithTask(Continuation { task ->
            if(task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            binding.nextBtn.isEnabled = true
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                Log.i("URL","Download Url: $downloadUrl")
            } else {
                // Handle failures
                // ...
            }
        }.addOnFailureListener {  }
    }
}