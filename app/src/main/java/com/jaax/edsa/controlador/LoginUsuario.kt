package com.jaax.edsa.controlador

import android.annotation.SuppressLint
import android.app.*
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.jaax.edsa.modelo.DBHelper
import com.jaax.edsa.modelo.Usuario
import com.jaax.edsa.R
import kotlinx.android.synthetic.main.login_usuario.*
import java.lang.NullPointerException

class LoginUsuario: AppCompatActivity() {
    private lateinit var txtForPsswrd: TextView
    private lateinit var btnAcceder: Button
    private lateinit var edTxtUsuario: EditText
    private lateinit var edTxtPsswrd: EditText
    private lateinit var db: DBHelper
    private lateinit var toast: Toast
    private lateinit var usuarioActual: Usuario

    private fun init() {
        val datosUsuario = this.intent.extras
        usuarioActual = Usuario(
            datosUsuario?.getString("usrNombre")!!,
            datosUsuario.getString("usrPassword")!!,
            datosUsuario.getString("usrKeyword")!!,
            ArrayList()
        )
        //val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        //notificationManager?.cancel(GestorNotificaciones.NOTIFICATION_ID)
    }

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_usuario)

        edTxtUsuario = findViewById(R.id.main_loginUsername)
        edTxtPsswrd = findViewById(R.id.main_loginPassword)
        btnAcceder = findViewById(R.id.main_btnAcceder)
        txtForPsswrd = findViewById(R.id.main_forgotPassword)
        db = DBHelper(this.applicationContext, DBHelper.nombreDB, null, DBHelper.version)
        toast = Toast.makeText(this.applicationContext, "txt", Toast.LENGTH_SHORT)
        toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0)
        init()
        edTxtUsuario.setText(usuarioActual.nombre)

        MobileAds.initialize(this@LoginUsuario)
        val adRequest = AdRequest.Builder().build()
        adview1.loadAd( adRequest )
    }

    override fun onResume() {
        super.onResume()
        btnAcceder.setOnClickListener {
            val usr = edTxtUsuario.text.toString()
            val pss = edTxtPsswrd.text.toString()
            usuarioActual.nombre = usr
            usuarioActual.password = pss

            val acceso = verificarLogin( usuarioActual.nombre, usuarioActual.password )
            if( acceso ){
                toast.setText("Bienvenid@")
                toast.show()
                edTxtPsswrd.setText("")
                val intent = Intent(this@LoginUsuario, VerEmails::class.java)
                intent.putExtra("Login_usrNombre", usuarioActual.nombre)
                intent.putExtra("Login_usrPassword", usuarioActual.password)
                intent.putExtra("Login_usrKeyword", usuarioActual.keyword)
                startActivity(intent)
                this.finish()
            } else {
                toast.setText("Revisa tus datos")
                toast.show()
            }
        }

        /*txtNewUser.setOnClickListener {
            val intent = Intent(this.applicationContext, AddUsuario::class.java)
            startActivity(intent)
            this.finish()
        }*/

        txtForPsswrd.setOnClickListener {
            val intent = Intent(this.applicationContext, UpdateUsuario::class.java)
            intent.putExtra("Login_usrNombre", usuarioActual.nombre)
            intent.putExtra("Login_usrPassword", usuarioActual.password)
            intent.putExtra("Login_usrKeyword", usuarioActual.keyword)
            startActivity(intent)
            this.finish()
        }
    }

    private fun verificarLogin( usuario: String, psswrd: String ): Boolean{
        try {
            val cursor = db.getAllUsuarios()

            if( usuario=="" || psswrd=="" ){
                toast.setText("Uno o más campos están vacíos")
                toast.show()
                return false
            } else {
                if( cursor.count>0 ){ //debería existir sólo 1 usuario si ya se registró
                    while(cursor.moveToNext()){
                        //sólo tendrá un valor porque sólo se puede registrar un usuario por app
                        val usrYaRegistrado = cursor.getString(0)
                        val pssYaRegistrada = cursor.getString(1)

                        return (usrYaRegistrado==usuario && pssYaRegistrada==psswrd)
                    }
                } else return false
            }
        } catch (excp: NullPointerException){}
        return false
    }
}