package com.fmd.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.fmd.fotografpaylasma.databinding.FragmentKullaniciBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class KullaniciFragment : Fragment() {

    private var _binding: FragmentKullaniciBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth= Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentKullaniciBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.kayitButton.setOnClickListener{kayitOl(it)}
        binding.girisButton.setOnClickListener{girisYap(it)}

        val guncelKullanici=auth.currentUser
        if(guncelKullanici!=null){
            //kullanici daha önce giriş yapmış
            val action=KullaniciFragmentDirections.actionKullaniciFragment3ToFeedFragment()
            Navigation.findNavController(view).navigate(action)
        }
    }

    fun kayitOl(view:View){
        println("Kayit Ol tıklandı")

        val email=binding.emailText.text.toString()
        val password=binding.passwordText.text.toString()

        if(email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task->
                if(task.isSuccessful){
                    //oluşturulduysa
                    val action=KullaniciFragmentDirections.actionKullaniciFragment3ToFeedFragment()
                    Navigation.findNavController(view).navigate(action)
                }
            }.addOnFailureListener { exception->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                //hata mesajini verir
            }
        }

    }

    fun girisYap(view:View){
        println("Giris Yap tıklandı")

        val email=binding.emailText.text.toString()
        val password=binding.passwordText.text.toString()
        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val action=KullaniciFragmentDirections.actionKullaniciFragment3ToFeedFragment()
                Navigation.findNavController(view).navigate(action)
            }.addOnFailureListener {exception->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}