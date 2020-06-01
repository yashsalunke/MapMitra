package com.mapmitra.mapmitra.login

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.mapmitra.mapmitra.activities.MainActivity
import com.mapmitra.mapmitra.R
import com.mapmitra.mapmitra.utils.ConnectionManager
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        title = getString(R.string.loginTitle)


        if (ConnectionManager().checkConnectivity(this)){ }
        else{
            val dialog = AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("No Internet Connection Found.\n Please Connect to the Internet First.")
                .setPositiveButton("Ok"){_,_->                }
                .setNegativeButton("Cancel"){_,_->            }
            dialog.create()
            dialog.show()
        }



        val auth = FirebaseAuth.getInstance()


        if (auth.currentUser != null) {
            if (auth.currentUser!!.isEmailVerified) {
                goMainActivity()

            } else {
                Toast.makeText(
                    baseContext , "Please verify your email address." ,
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        txtSignUp.setOnClickListener {
            goRegisterActivity()
        }

        btnLogin.setOnClickListener {

            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this , "Email/Password cannot be Empty" , Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnLogin.isEnabled = false
            progressBar.visibility = VISIBLE
            //Firebase Authentication

            auth.signInWithEmailAndPassword(email , password).addOnCompleteListener { task ->
                btnLogin.isEnabled = true
                progressBar.visibility = GONE
                if (task.isSuccessful) {
                    if (auth.currentUser!!.isEmailVerified) {
                        Toast.makeText(this , "Success!" , Toast.LENGTH_SHORT).show()
                        goMainActivity()
                    } else {
                        Toast.makeText(
                            baseContext , "Please verify your email address." ,
                            Toast.LENGTH_SHORT
                        ).show()
                        auth.currentUser?.sendEmailVerification()
                    }

                } else {
                    Log.e("TAG" , "SignInFailed" , task.exception)
                    Toast.makeText(this , "SignInFailed" , Toast.LENGTH_SHORT).show()
                }
            }


        }

        btn_forgot_password.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Forgot Password")
            val view: View = layoutInflater.inflate(R.layout.dialog_forgot_password , null)
            val username = view.findViewById<EditText>(R.id.et_username)
            builder.setView(view)
            builder.setPositiveButton("Reset" , DialogInterface.OnClickListener { _ , _ ->
                forgotPassword(username)
            })
            builder.setNegativeButton("close" , DialogInterface.OnClickListener { _ , _ -> })
            builder.show()
        }


    }

    private fun forgotPassword(username: EditText) {
        if (username.text.toString().isEmpty()) {
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(username.text.toString()).matches()) {
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(username.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this , "Email sent." , Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun goMainActivity() {

        Log.i("TAG" , "goMainActivity")
        val intentMain = Intent(this , MainActivity::class.java)
        startActivity(intentMain)
        finish()
    }

    private fun goRegisterActivity() {
        Log.i("TAG" , "goRegisterActivity")
        val intentReg = Intent(this , RegisterActivity::class.java)
        startActivity(intentReg)


    }
}