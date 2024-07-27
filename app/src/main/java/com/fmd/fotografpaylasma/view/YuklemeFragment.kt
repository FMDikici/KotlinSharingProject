package com.fmd.fotografpaylasma.view

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.fmd.fotografpaylasma.databinding.FragmentYuklemeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.google.firebase.Timestamp

import java.util.UUID


class YuklemeFragment : Fragment() {

    private var _binding: FragmentYuklemeBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var secilenGorsel: Uri?=null
    var secilenBitmap: Bitmap?=null
    //kişinin galerisine erişme ve fotoyu alma

    private lateinit var auth: FirebaseAuth
    private lateinit var storage:FirebaseStorage

    private lateinit var db: FirebaseFirestore



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth= Firebase.auth
        storage=Firebase.storage
        db=Firebase.firestore

        registerLaunchers()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentYuklemeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }


    override fun onViewCreated(view:View,savedInstanceState:Bundle?) {
        super.onViewCreated(view,savedInstanceState)
        binding.yukleButton.setOnClickListener{yukleTiklandi(it)}
        binding.imageView.setOnClickListener{gorselSec(it)}

    }



    //random string yapar ve yüklenen resimlere verir
    //aynı olmaması gerek isimlerinin yüklerken sorun yaratır
    val uuid= UUID.randomUUID()
    val gorselAdi="${uuid}.jpg"

    fun yukleTiklandi(view: View){

        //random string yapar ve yüklenen resimlere verir
        //aynı olmaması gerek isimlerinin yüklerken sorun yaratır
        val uuid= UUID.randomUUID()
        val gorselAdi="${uuid}.jpg"

        val yorum=binding.commentText.text.toString()

        val reference=storage.reference
        val gorselReferansi=reference.child("images").child(gorselAdi)
        if(secilenGorsel!=null){
            gorselReferansi.putFile(secilenGorsel!!).addOnSuccessListener { uploadTask->
                //urlyi alma işlemi
                gorselReferansi.downloadUrl.addOnSuccessListener { uri->
                    if(auth.currentUser!=null){
                        val downloadUrl=uri.toString()

                        //veri tabanına kayıt
                        val postMap= hashMapOf<String,Any>()
                        postMap.put("DownloadUrl",downloadUrl)
                        postMap.put("email",auth.currentUser!!.email.toString())
                        postMap.put("comment",yorum)
                        postMap.put("date", Timestamp.now())

                        db.collection("Posts").add(postMap).addOnSuccessListener { documentReferance->
                            //veri dataBase'e yüklenmiş oluyor
                            val action=YuklemeFragmentDirections.actionYuklemeFragmentToFeedFragment()
                            Navigation.findNavController(view).navigate(action)
                        }.addOnFailureListener { exception->
                            Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
                        }

                    }


                }

            }.addOnFailureListener{exception->
                Toast.makeText(requireContext(),exception.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun gorselSec(view: View){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // read media images
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // izin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        READ_MEDIA_IMAGES
                    )
                ) {
                    // izin mantığını kullanıcıya gösterme
                    Snackbar.make(
                        view,
                        "Galeriye Gitmek İçin İzin Vermeniz Gerek!",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin Ver", View.OnClickListener {
                        // izin istemek lazım
                        permissionLauncher.launch(READ_MEDIA_IMAGES)
                    }).show()
                } else {
                    permissionLauncher.launch(READ_MEDIA_IMAGES)
                }
            } else {
                // izin var
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // izin yok
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(),
                        READ_EXTERNAL_STORAGE
                    )
                ) {
                    // izin mantığını kullanıcıya gösterme
                    Snackbar.make(
                        view,
                        "Galeriye Gitmek İçin İzin Vermeniz Gerek!",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("İzin Ver", View.OnClickListener {
                        // izin istemek lazım
                        permissionLauncher.launch(READ_EXTERNAL_STORAGE)
                    }).show()
                } else {
                    permissionLauncher.launch(READ_EXTERNAL_STORAGE)
                }
            } else {
                // izin var
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
        }
    }


    private fun registerLaunchers(){
        activityResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
            if(result.resultCode==RESULT_OK){
                val intentFromResult=result.data
                if(intentFromResult!=null){
                    secilenGorsel=intentFromResult.data
                    try{
                        if(Build.VERSION.SDK_INT>=28){
                            val source=ImageDecoder.createSource(requireActivity().contentResolver,secilenGorsel!!)
                            secilenBitmap=ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                        else{
                            secilenBitmap=MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,secilenGorsel)
                            binding.imageView.setImageBitmap(secilenBitmap)
                        }
                    }
                    catch (e:Exception){

                    }
                }
            }
        }

        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if(result){
                //izin verildi
                val intentToGallery=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }
            else{
                //kullanici izni reddetti
                Toast.makeText(requireContext(),"İzni Reddettiniz,izne ihtiyacımız var",Toast.LENGTH_LONG).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}