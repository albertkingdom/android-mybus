package com.albertkingdom.mybusmap.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.albertkingdom.mybusmap.model.Favorite;
import com.albertkingdom.mybusmap.model.FavoriteList;
import com.albertkingdom.mybusmap.model.db.FavoriteRealm;
import com.albertkingdom.mybusmap.util.RealmManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.realm.RealmResults;
import timber.log.Timber;


public class ArrivalTimeFragmentViewModel extends ViewModel {

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<Boolean> isLogin = new MutableLiveData<>(false);
    private MutableLiveData<List<Favorite>> listOfFavorite = new MutableLiveData<>();

    public ArrivalTimeFragmentViewModel() {
        super();
        checkIfSignIn();
    }

    public LiveData<List<Favorite>> getListOfFavorite() {
        return listOfFavorite;
    }

    public LiveData<Boolean> getIsLogin() {
        if (isLogin == null) {
            isLogin = new MutableLiveData<>(false);
        }
        return isLogin;
    }

    void checkIfSignIn() {
        isLogin.setValue(auth.getCurrentUser() != null);
    }

    void getFavoriteRouteFromRemote() {
        FirebaseUser currentUser = auth.getCurrentUser();

        String email = currentUser.getEmail();

        final DocumentReference docRef = db.collection("favoriteRoute").document(email);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Timber.w(error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    FavoriteList favoriteList = snapshot.toObject(FavoriteList.class);
                    if (favoriteList != null && favoriteList.getList() != null) {
                        listOfFavorite.setValue(favoriteList.getList());
                    }
                } else {
                    Timber.d("Current data: null");
                }
            }


        });
    }

    void saveToRemote(String routeName) {
        FirebaseUser currentUser = auth.getCurrentUser();
        String userEmail = currentUser.getEmail();
        final DocumentReference ref = db.collection("favoriteRoute").document(userEmail);
        Favorite favorite = new Favorite(routeName, "");
        ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (snapshot != null && snapshot.exists()) {
                    Timber.d("Current data: %s", snapshot.getData());
                    ref.update("list", FieldValue.arrayUnion(favorite))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                      @Override
                                                      public void onSuccess(Void unused) {
                                                          Timber.d("DocumentSnapshot successfully written!");
                                                      }
                                                  }
                            )
                            .addOnFailureListener(new OnFailureListener() {
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {
                                                          Timber.w(e);
                                                      }
                                                  }
                            );


                } else {
                    Timber.d("Current data: null");
                    List<Favorite> favorites = new ArrayList<>();
                    favorites.add(favorite);
                    FavoriteList favoriteList = new FavoriteList(favorites);
                    ref.set(favoriteList)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                      @Override
                                                      public void onSuccess(Void unused) {
                                                          Timber.d("DocumentSnapshot successfully written!");
                                                      }
                                                  }
                            )
                            .addOnFailureListener(new OnFailureListener() {
                                                      @Override
                                                      public void onFailure(@NonNull Exception e) {
                                                          Timber.w(e);
                                                      }
                                                  }
                            );
                }
            }
        });

    }

    void removeFromRemote(String routeName) {
        FirebaseUser currentUser = auth.getCurrentUser();
        String userEmail = currentUser.getEmail();
        final DocumentReference ref = db.collection("favoriteRoute").document(userEmail);
        Favorite favorite = new Favorite(routeName, "");
        ref.update("list", FieldValue.arrayRemove(favorite))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                          @Override
                                          public void onSuccess(Void unused) {
                                              Timber.d("DocumentSnapshot successfully remove!");
                                          }
                                      }
                )
                .addOnFailureListener(new OnFailureListener() {
                                          @Override
                                          public void onFailure(@NonNull Exception e) {
                                              Timber.w(e);
                                          }
                                      }
                );
    }

    void getFromDB() {
        RealmResults<FavoriteRealm> listOfStation = RealmManager.shared.queryAllFromDB();

        List<Favorite> list = listOfStation.stream().map(favoriteRealm -> new Favorite(favoriteRealm.getName(), null)).collect(Collectors.toList());
        listOfFavorite.setValue(list);
    }

    void saveToDB(String routeName) {
        RealmManager.shared.saveToDB(routeName);
    }

    void removeFromDB(String routeName) {
        FavoriteRealm favoriteRealmToDelete = new FavoriteRealm(routeName, null);
        RealmManager.shared.removeFromDB(favoriteRealmToDelete);
    }

}
