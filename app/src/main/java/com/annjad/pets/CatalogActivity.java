package com.annjad.pets;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class CatalogActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.lv_pets)
    ListView mPetsList;

    PetDatabase petsDb;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);
        ButterKnife.bind(this);
        petsDb = Room.databaseBuilder(getApplicationContext(), PetDatabase.class, "pets_db")
                .build();

        // Setup FAB to open EditorActivity
        mFab.setOnClickListener(view -> {
            Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
            intent.putExtra("isEditMode", false);
            startActivity(intent);
        });
        readPetsFromDb();
        Log.i("PETSs", "On Create");
        mPetsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                Pet editPet = (Pet) adapterView.getItemAtPosition(i);
                intent.putExtra("isEditMode", true);
                intent.putExtra("editPetId", editPet.getPetId());
                startActivity(intent);
            }
        });
    }

    private void readPetsFromDb() {
        Single<List<Pet>> allPets = petsDb.petDao().getAllPets();
        allPets.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Pet>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(List<Pet> pets) {
                        PetsAdapter adapter = new PetsAdapter(getApplicationContext(), 0, pets);
                        mPetsList.setAdapter(adapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "Error reading from database", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        readPetsFromDb();
//        Log.i("PETSs", "On Resume");
//    }

    @Override
    protected void onDestroy() {
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummyPetsDataInDb();
                readPetsFromDb();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPetsInDb();
                readPetsFromDb();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyPetsDataInDb() {
        List<Pet> dummyPets = new ArrayList<>();
        dummyPets.add(new Pet("Toto", "Terrier", 1, 7));
        dummyPets.add(new Pet("Bela", "Beagle", 2, 5));
        dummyPets.add(new Pet("Lucky", "Samoyed", 1, 10));
        dummyPets.add(new Pet("Aurora", "Retriever", 2, 8));
        dummyPets.add(new Pet("Max", "Labrador", 1, 7));
        Completable.fromAction(() -> petsDb.petDao().insertAllPets(dummyPets))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(getApplicationContext(), "Dummy data inserted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "Error with inserting dummy data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteAllPetsInDb() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                petsDb.petDao().deleteAllPets();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(getApplicationContext(), "All data deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getApplicationContext(), "Error with deleting data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("PETSs", "On Start");
        readPetsFromDb();
    }
}
