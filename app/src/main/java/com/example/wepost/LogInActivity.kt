package com.example.wepost

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.wepost.daos.UserDao
import com.example.wepost.databinding.ActivityLogInBinding
import com.example.wepost.models.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LogInActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 33
    private lateinit var binding: ActivityLogInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        binding.logInBtn.setOnClickListener {
            if (binding.emailInp.text?.isBlank() == true) {
                binding.etEmail.error = "Please enter an Email"
                return@setOnClickListener
            }

            else if (binding.passwordInp.text?.isBlank() == true) {
                binding.etPassword.error = "Please enter a Password"
                return@setOnClickListener
            }

            else {
                binding.progressBar.visibility = View.VISIBLE
                binding.scrollView2.visibility = View.GONE
                CoroutineScope(Dispatchers.IO).launch {
                    val authResult: AuthResult? = signIn()
                    if (authResult == null) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            binding.scrollView2.visibility = View.VISIBLE
                            Toast.makeText(this@LogInActivity, "Please enter a valid email and password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        // use authResult for example to get FirebaseUser using authResult.getUser()
                        val mainActivityIntent = Intent(this@LogInActivity, MainActivity::class.java)
                        startActivity(mainActivityIntent)
                        finish()
                    }
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        binding.goToSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.d(ContentValues.TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        binding.progressBar.visibility = View.VISIBLE
        binding.scrollView2.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main) {
                updateUIWithGoogle(firebaseUser)
            }
        }
    }

    private fun updateUIWithGoogle(firebaseUser: FirebaseUser?) {
        if (firebaseUser != null) {

            val user = UserModel(firebaseUser.uid, firebaseUser.displayName.toString())
            user.profilePhoto = firebaseUser.photoUrl.toString()
            val usersDao = UserDao()
            usersDao.addUser(user)

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        } else {
            binding.progressBar.visibility = View.GONE
            binding.scrollView2.visibility = View.VISIBLE
            Toast.makeText(this, "Account creation Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private suspend fun signIn(): AuthResult? {
        return try {
            val email = binding.emailInp.text.toString()
            val password = binding.passwordInp.text.toString()

            val authResult: AuthResult = auth.signInWithEmailAndPassword(email,password).await()
            authResult

        } catch (e: Exception) {
            null
        }
    }
}