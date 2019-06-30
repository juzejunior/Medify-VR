package br.com.spacerocket.medifyvr

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    var auth = FirebaseAuth.getInstance()

    companion object{
       val RC_SIGN_IN  = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        verifyUserLogged()
    }

    fun verifyUserLogged() {
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startLoginFlow()
        }
    }

    fun startLoginFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .build(),
            RC_SIGN_IN)
    }

    /*override fun onResume() {
        super.onResume()
        //verifyUserLogged()
    }*/

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(this, "Entrando...", Toast.LENGTH_SHORT).show()
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, "Ops! Credenciais não reconhecidas :(, tente novamente.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
