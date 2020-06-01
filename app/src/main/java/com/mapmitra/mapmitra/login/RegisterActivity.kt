package com.mapmitra.mapmitra.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mapmitra.mapmitra.R
import com.mapmitra.mapmitra.models.User
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestoreDB: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        title = "SIgn Up"
        auth = FirebaseAuth.getInstance()
        firestoreDB = FirebaseFirestore.getInstance()

        btnSignUp.setOnClickListener {
            signUpUser()
        }
        txtLogIn.setOnClickListener {
            startActivity(Intent(this , LoginActivity::class.java))

        }


    }

    private fun signUpUser() {

        if (!Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString())
                .matches() || etEmail.text.toString().isEmpty()
        ) {
            etEmail.error = "Please enter valid email"
            etEmail.requestFocus()
            return
        }

        if (etName.text.toString().isEmpty()) {
            etName.error = "Please Enter Your Name"
            etName.requestFocus()
        }



        if (etPassword.text.toString().length < 6) {
            if (etPassword.text.toString().isEmpty()) {
                etPassword.error = "Please enter Password"
                etPassword.requestFocus()
            } else {
                etPassword.error = "Please enter password with minimum 6 characters"
                etPassword.requestFocus()
                return
            }
        }

        if (etMobile.text.length != 10) {
            etMobile.error = "Please Enter Valid Mobile Number"
            etMobile.requestFocus()
            return
        }

        btnSignUp.isEnabled = false
        progressBar.visibility = View.VISIBLE
        val name = etName.text.toString()
        val eMail = etEmail.text.toString()
        val mobile = etMobile.text.toString().toLong()


        auth.createUserWithEmailAndPassword(etEmail.text.toString() , etPassword.text.toString())
            .addOnCompleteListener(this) { userCreationTask ->
                btnSignUp.isEnabled = true
                progressBar.visibility = View.GONE
                if (userCreationTask.isSuccessful) {
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val builder = AlertDialog.Builder(this@RegisterActivity)
                                builder.setTitle("Varify Email")
                                    .setMessage("A verification link is sent to you via Email, Please Verify your Email. ")
                                    .setPositiveButton("OK") { _ , _ ->
                                        val user = User(name , eMail , mobile)

                                        firestoreDB.collection("users")
                                            .document(auth.currentUser!!.uid)
                                            .set(user)
                                            .addOnCompleteListener { userDataUploadTask ->
                                                if (userDataUploadTask.isSuccessful) {

                                                    etEmail.text.clear()
                                                    etMobile.text.clear()
                                                    etPassword.text.clear()

                                                    auth.signOut()

                                                    startActivity(
                                                        Intent(
                                                            this ,
                                                            LoginActivity::class.java
                                                        )
                                                    )
                                                }
                                            }
                                    }
                                    .show()


                            }
                        }

                } else {
                    Toast.makeText(
                        this ,
                        "Sign Up failed. Try again after some time." ,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }


}



