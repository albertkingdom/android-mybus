package com.albertkingdom.mybusmap.ui;

import android.util.Log;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.albertkingdom.mybusmap.model.Favorite;
import com.albertkingdom.mybusmap.model.FavoriteList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.List;


public class ArrivalTimeFragmentViewModel extends ViewModel {

    static String TAG = "ArrivalTimeFragmentViewModel";
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MutableLiveData<List<Favorite>> listOfFavorite;
    //private LiveData<List<Favorite>> listOfFavorite;


    public LiveData<List<Favorite>> getListOfFavorite() {
        if (listOfFavorite == null) {
            listOfFavorite = new MutableLiveData<List<Favorite>>();
        }
        return listOfFavorite;
    }

    void getFavoriteRouteFromRemote() {
        FirebaseUser currentUser = auth.getCurrentUser();

        String email = currentUser.getEmail();

        final DocumentReference docRef = db.collection("favoriteRoute").document(email);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.w(TAG, "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    FavoriteList favoriteList = snapshot.toObject(FavoriteList.class);
                    if (favoriteList != null && favoriteList.getList() != null) {
                        listOfFavorite.setValue(favoriteList.getList());
                    }
                } else {
                    Log.d(TAG, "Current data: null");
                }
            }


        });
    }


}
