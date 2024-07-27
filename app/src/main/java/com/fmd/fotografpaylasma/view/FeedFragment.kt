package com.fmd.fotografpaylasma.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.fmd.fotografpaylasma.R
import com.fmd.fotografpaylasma.adapter.PostAdapter
import com.fmd.fotografpaylasma.databinding.FragmentFeedBinding
import com.fmd.fotografpaylasma.model.Post
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore


class FeedFragment : Fragment(), PopupMenu.OnMenuItemClickListener {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth:FirebaseAuth

    private lateinit var popup: PopupMenu
    private lateinit var db:FirebaseFirestore

    val postList:ArrayList<Post> = arrayListOf()
    private var adapter: PostAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth=Firebase.auth
        db=Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener { showPopupMenu(it) }

        popup = PopupMenu(requireContext(), binding.floatingActionButton)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.my_popup_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)

        fireStoreVerileriAl()

        adapter= PostAdapter(postList)
        binding.feedRecyclerView.layoutManager=LinearLayoutManager(requireContext())
        binding.feedRecyclerView.adapter=adapter
    }

    private fun fireStoreVerileriAl(){
        db.collection("Posts").orderBy("date",
            Query.Direction.DESCENDING).addSnapshotListener{ value, error->
            if(error!=null){
                Toast.makeText(requireContext(),error.localizedMessage,Toast.LENGTH_LONG).show()
            }
            else{
                if(value!=null){
                    if(!value.isEmpty){
                        //boş değilse
                        postList.clear()
                        val documents=value.documents
                        for(document in documents){
                            val comment=document.get("comment") as String //casting
                            val email=document.get("email") as String //casting
                            val DownloadUrl=document.get("DownloadUrl") as String //casting
                            // any normalde ama biz string istiyoruz

                            val post= Post(email,comment,DownloadUrl)
                            postList.add(post)
                        }
                        adapter?.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    private fun showPopupMenu(view: View) {
        popup.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.yuklemeItem -> {
                Navigation.findNavController(requireView()).navigate(R.id.action_feedFragment_to_yuklemeFragment)
                return true
            }
            R.id.cikisItem -> {

                // Çıkış yapma işlemi
                auth.signOut()

                val action = FeedFragmentDirections.actionFeedFragmentToKullaniciFragment3()
                Navigation.findNavController(requireView()).navigate(action)
                return true
            }
        }
        return false
    }
}

