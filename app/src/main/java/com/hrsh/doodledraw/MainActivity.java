

package com.hrsh.doodledraw;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    List<DrawingModel> images = new ArrayList<>();
    RecyclerView recyclerView;
    FloatingActionButton fab;
    MaterialToolbar materialToolbar;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        StorageReference listRef = FirebaseStorage.getInstance().getReference().child("images");

//        listRef.listAll()
//                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
//                    @Override
//                    public void onSuccess(ListResult listResult) {
//                        for (StorageReference prefix : listResult.getPrefixes()) {
//                            // All the prefixes under listRef.
//                            // You may call listAll() recursively on them.
//                        }
//
//                        for (StorageReference item : listResult.getItems()) {
//                            // All the items under listRef.
////                            images.add(item.get);
//                        }
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        // Uh-oh, an error occurred!
//                    }
//                });
//        FirebaseFirestore.getInstance().collection("Drawings").get()
//                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        DocumentSnapshot document = task.getResult();
//                        if(document.exists()) {
//
//                            List<DrawingModel> group = Objects.requireNonNull(document.toObject(DrawingDocument.class)).drawings;
//                        List<String> group = (List<String>) document.get("Drawings");
//                            System.out.println("LOGGGGG: " + group.get(0) + " " + group.get(1));
//                        }
//                    }
//                });
        materialToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(materialToolbar);
        fab = findViewById(R.id.fab);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        CustomAdapter adapter = new CustomAdapter(getApplicationContext(), images);
        recyclerView.setAdapter(adapter);

        FirebaseFirestore.getInstance().collection("Drawings")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> drawDoc = document.getData(); //new HashMap<>();
                                images.add(new DrawingModel(drawDoc.get("uid").toString() , drawDoc.get("email").toString() , drawDoc.get("url").toString() ));
                                System.out.println("LOGGG: "+drawDoc.get("email")+" "+document.get("url"));
                                Log.d("LOGGG: ", document.getId() + " => " + document.getData());
                                adapter.notifyDataSetChanged();
                            }

                        } else {
                            Log.w("LOGGG: ", "Error getting documents.", task.getException());
                        }
                    }
                });

//        for (int i = 0; i < images.size(); i++) {
//            DrawingModel itemModel = new DrawingModel();
//            itemModel.setUrl(images.get(i).getUrl()); // TODO
//            itemModel.setEmail(images.get(i).getEmail()); // TODO
//            itemModel.setUid(images.get(i).getUid());
//
//            //add in array list
//            images.add(itemModel);
//        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent drawingActivity = new Intent(MainActivity.this, DrawingActivity.class);
                startActivity(drawingActivity);
            }
        });
    }
}

