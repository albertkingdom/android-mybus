package com.albertkingdom.mybusmap.util;

import com.albertkingdom.mybusmap.model.db.FavoriteRealm;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import timber.log.Timber;

public class RealmManager {
    String realmName = "My Project";
    RealmConfiguration config = new RealmConfiguration.Builder().name(realmName).build();
    Realm backgroundThreadRealm = Realm.getInstance(config);

    public static RealmManager shared = new RealmManager();
    public static String TAG = "RealmManager";
    private RealmManager() {}
    public void saveToDB(String routeName) {
        FavoriteRealm favorite = new FavoriteRealm(routeName, null);
        backgroundThreadRealm.executeTransactionAsync(new Realm.Transaction() {
                                                          @Override
                                                          public void execute(Realm realm) {
                                                              realm.insert(favorite);
                                                          }
                                                      }, new Realm.Transaction.OnSuccess() {
                                                          @Override
                                                          public void onSuccess() {
                                                              Timber.i("save success");
                                                          }
                                                      }, new Realm.Transaction.OnError() {
                                                          @Override
                                                          public void onError(Throwable error) {
                                                              Timber.w(error);
                                                          }
                                                      }

        );
    }

    public RealmResults<FavoriteRealm> queryAllFromDB() {
        return backgroundThreadRealm.where(FavoriteRealm.class).findAll();
    }

    public void removeFromDB(FavoriteRealm favoriteRealm) {
        String routeNameToBeDel = favoriteRealm.getName();
        backgroundThreadRealm.executeTransactionAsync(new Realm.Transaction() {
                                                          @Override
                                                          public void execute(Realm realm) {
                                                              FavoriteRealm innerYetAnotherTask = realm.where(FavoriteRealm.class).equalTo("name", routeNameToBeDel).findFirst();
                                                              innerYetAnotherTask.deleteFromRealm();
                                                          }
                                                      }, new Realm.Transaction.OnSuccess() {
                                                          @Override
                                                          public void onSuccess() {
                                                              Timber.i("delete success");
                                                          }
                                                      }, new Realm.Transaction.OnError() {
                                                          @Override
                                                          public void onError(Throwable error) {
                                                              Timber.w(error);
                                                          }
                                                      }

        );
    }
}
