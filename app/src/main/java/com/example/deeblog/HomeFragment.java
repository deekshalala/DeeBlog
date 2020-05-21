package com.example.deeblog;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    RecyclerView blog_list_view;
    List<blogPost> blogList;
    List<Users> usersList;

    blogPostAdapter blogPostAdapter;

    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    DocumentSnapshot lastVisible;
    Boolean firstPageFirstLoad=true;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home,container,false);
        setHasOptionsMenu(true);

        usersList=new ArrayList<>();

        blogList=new ArrayList<>();

        blog_list_view=view.findViewById(R.id.blog_list_view);
        blogPostAdapter=new blogPostAdapter(blogList, usersList);
        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogPostAdapter);
        blog_list_view.setHasFixedSize(true);

        firebaseFirestore=FirebaseFirestore.getInstance();
        auth=FirebaseAuth.getInstance();

        if(auth.getCurrentUser()!=null) {
            blog_list_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean reachedBottom=!recyclerView.canScrollVertically(1);
                    if(reachedBottom){
                        loadMorePost();
                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(3);
            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                    if(e!=null){
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!queryDocumentSnapshots.isEmpty()){
                        if (firstPageFirstLoad) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size()-1);
                            blogList.clear();
                            usersList.clear();
                        }
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPostID = doc.getDocument().getId();

                                final blogPost blogPost = doc.getDocument().toObject(com.example.deeblog.blogPost.class).withId(blogPostID);
                                String blogUserId = doc.getDocument().getString("user");
                                firebaseFirestore.collection("Users").document(blogUserId).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                if (task.isSuccessful()) {
                                                    Users users = task.getResult().toObject(Users.class);

                                                    if (firstPageFirstLoad) {
                                                        usersList.add(users);
                                                        blogList.add(blogPost);
                                                    } else {
                                                        usersList.add(0, users);
                                                        blogList.add(0, blogPost);
                                                    }

                                                } else {
                                                    Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                                blogPostAdapter.notifyDataSetChanged();
                                            }
                                        });


                            }
                        }

                        firstPageFirstLoad = false;
                    }
                    else{
                        Toast.makeText(getActivity(), "Add New Post", Toast.LENGTH_LONG).show();
                    }

                }
            });

       }

        return view;
    }


    public void loadMorePost(){

            Query nextQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(4);

            nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                                              @Override
                                              public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                                                  if (e != null) {
                                                      Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                                      return;
                                                  }
                                                  if (!queryDocumentSnapshots.isEmpty()) {

                                                      lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                                      for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                                          if (doc.getType() == DocumentChange.Type.ADDED) {
                                                              String blogPostID = doc.getDocument().getId();
                                                              final blogPost blogPost = doc.getDocument().toObject(com.example.deeblog.blogPost.class).withId(blogPostID);
                                                              String blogUserId = doc.getDocument().getString("user");
                                                              firebaseFirestore.collection("Users").document(blogUserId).get()
                                                                      .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                          @Override
                                                                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                                                              if (task.isSuccessful()) {
                                                                                  Users users = task.getResult().toObject(Users.class);
                                                                                  usersList.add(users);
                                                                                  blogList.add(blogPost);

                                                                              } else {
                                                                                  Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                                              }
                                                                              blogPostAdapter.notifyDataSetChanged();
                                                                          }
                                                                      });
                                                          }
                                                      }
                                                  }
                                              }});
            }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        if(item.getItemId()==R.id.action_search){
            final SearchView searchView= (SearchView) item.getActionView();
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    blogPostAdapter.getFilter().filter(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {

                    blogPostAdapter.getFilter().filter(newText);
                    return true;
                }

            });
        }



        return super.onOptionsItemSelected(item);
    }
}





