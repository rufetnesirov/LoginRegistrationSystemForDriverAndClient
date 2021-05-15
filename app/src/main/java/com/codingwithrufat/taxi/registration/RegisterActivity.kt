package com.codingwithrufat.taxi.registration

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.cardview.widget.CardView
import com.codingwithrufat.taxi.R
import com.codingwithrufat.taxi.common.Common
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    private lateinit var backArrow: ImageView
    private lateinit var spinner: Spinner
    private lateinit var cardViewCar: CardView
    private lateinit var cardViewSpinner: CardView
    private lateinit var createAccount: RelativeLayout
    private lateinit var editTextName: EditText
    private lateinit var editTextNumber: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextCarName: EditText
    private lateinit var iconEye: ImageView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private val TAG = "RegisterActivity"
    private val context = this@RegisterActivity
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        getWidgets()
        clickBackButton()
        spinnerItemClicked()
        commonClassMethods()

        progressDialog = ProgressDialog(context)
        createAccount.setOnClickListener {

            progressDialog.setMessage("Please Wait... Verificatoin code will send you")
            progressDialog.setCancelable(false)
            progressDialog.show()
            firebasePhoneRegistration()

        }


    }

    // -- when spinner item is changed to client then EditText(car name) become gone otherwise everything back previous situation
    private fun spinnerItemClicked(){

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                var paramWithoutCar = LayoutParams(
                        0,
                        LayoutParams.MATCH_PARENT,
                        2.0f
                )

                var paramWithCar = LayoutParams(
                        0,
                        LayoutParams.MATCH_PARENT,
                        1.0f
                )
                paramWithCar.setMargins(15, 0, 0, 0)

                when(spinner.selectedItem){

                    "Client" -> {
                        cardViewCar.visibility = View.GONE
                        cardViewSpinner.layoutParams = paramWithoutCar
                    }

                    "Driver" -> {
                        cardViewCar.visibility = View.VISIBLE
                        cardViewSpinner.layoutParams = paramWithCar
                    }

                }

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

    }

    // -- initialize widgets
    private fun getWidgets(){

        backArrow = findViewById(R.id.ic_backFromSignUp)
        spinner = findViewById(R.id.spinner)
        cardViewCar = findViewById(R.id.cardView4)
        cardViewSpinner = findViewById(R.id.cardView5)
        createAccount = findViewById(R.id.relativeCreate)
        editTextName = findViewById(R.id.editTextFullname)
        editTextCarName = findViewById(R.id.editTextCarName)
        editTextNumber = findViewById(R.id.editTextPhoneNumber)
        editTextPassword = findViewById(R.id.editTextPassword)
        iconEye = findViewById(R.id.ic_eyeForSignUpPage)

    }

    // -- setup for back button click
    private fun clickBackButton(){

        backArrow.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            finish()
        }

    }

    // -- common class is helper class
    private fun commonClassMethods(){

        // password is visible or not statement
        Common().clickedIcEye(iconEye, editTextPassword, this)

    }

    private fun firebasePhoneRegistration(){

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onCodeSent(verification_code: String, p1: PhoneAuthProvider.ForceResendingToken) {

                var intent = Intent(context, VerificationCodeActivity::class.java).putExtra("verification_code", verification_code)
                startActivity(intent)

            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                signInWithCredential(credential)

                progressDialog.dismiss()

            }

            override fun onVerificationFailed(exception: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed: ", exception);
            }

        }

        firebaseAuth = FirebaseAuth.getInstance()
        val phoneAuthOptions = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(editTextNumber.text.toString())
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)

    }

    private fun signInWithCredential(credential: PhoneAuthCredential){

        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful){

                firebaseUser = it.result!!.user!!

            }else{

                Log.w(TAG, "signInWithCredential:failure", it.exception)

                Toast.makeText(context, it.exception.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }

}