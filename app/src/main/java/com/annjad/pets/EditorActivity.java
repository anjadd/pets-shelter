package com.annjad.pets;

import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;
import androidx.core.app.NavUtils;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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

/**
 * Allows user to create a new pet or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity {

    Pet mPet;
    boolean mPetHasChanged = false;
    PetDatabase petsDb;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * EditText field to enter the pet's name
     */
    @BindView(R.id.edit_pet_name)
    public EditText mNameEditText;

    /**
     * EditText field to enter the pet's breed
     */
    @BindView(R.id.edit_pet_breed)
    public EditText mBreedEditText;

    /**
     * EditText field to enter the pet's weight
     */
    @BindView(R.id.edit_pet_weight)
    public EditText mWeightEditText;

    /**
     * EditText field to enter the pet's gender
     */
    @BindView(R.id.spinner_gender)
    public Spinner mGenderSpinner;

    /**
     * Gender of the pet. The possible values are:
     * 0 for unknown gender, 1 for male, 2 for female.
     */
    private int mGender = 0;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        petsDb = Room.databaseBuilder(this, PetDatabase.class, "pets_db").build();

        setupSpinner();
        Intent myIntent = getIntent();
        isEditMode = myIntent.getBooleanExtra("isEditMode", false);
        if (isEditMode) {
            setTitle(getString(R.string.edit_pet));
            getPetById(myIntent.getIntExtra("editPetId", 1));
        } else {
            setTitle(getString(R.string.add_a_pet));
            mPet = new Pet();
            invalidateOptionsMenu();
        }
        mNameEditText.setOnTouchListener(mTouchListener);
        mBreedEditText.setOnTouchListener(mTouchListener);
        mGenderSpinner.setOnTouchListener(mTouchListener);
        mWeightEditText.setOnTouchListener(mTouchListener);
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            view.performClick();
            return false;
        }
    };

    private void getPetById(int editPetId) {
        Single<Pet> petSingle = petsDb.petDao().getPetById(editPetId);
        petSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Pet>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onSuccess(Pet pet) {
                        mPet = pet;
                        fillPetInfo();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(EditorActivity.this, "Error returning pet by id", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void fillPetInfo() {
        mNameEditText.setText(mPet.getName());
        mBreedEditText.setText(mPet.getBreed());
        mGenderSpinner.setSelection(mPet.getGender());
        mWeightEditText.setText(String.valueOf(mPet.getWeight()));
    }

    private void updatePetInDb() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                petsDb.petDao().updatePet(mPet);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(EditorActivity.this, "Pet updated successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(EditorActivity.this, "Error updating the pet in the db", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void insertPetInDb() {
        Completable.fromAction(() -> petsDb.petDao().insertNewPet(mPet))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(EditorActivity.this, "Pet inserted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(EditorActivity.this, "Error inserting the pet in the db", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deletePetFromDb() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                petsDb.petDao().deletePet(mPet);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(EditorActivity.this, "Pet deleted successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(EditorActivity.this, "Error deleting the pet from db", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenderSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.gender_male))) {
                        mGender = 1; // Male
                    } else if (selection.equals(getString(R.string.gender_female))) {
                        mGender = 2; // Female
                    } else {
                        mGender = 0; // Unknown
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGender = 0; // Unknown
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!isEditMode) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save a pet to the database and close the activity
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                if (isEditMode) {
                    showDeleteDialog();
                }
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                } else {
                    showUnsavedChangesDialog();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void savePet() {
        String nameString = mNameEditText.getText().toString().trim();
        String breedString = mBreedEditText.getText().toString().trim();
        String weightString = mWeightEditText.getText().toString().trim();
        if (TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(breedString) &&
                mGender == 0 &&
                TextUtils.isEmpty(weightString)) {
            return;
        }
        mPet.setName(nameString);
        mPet.setBreed(breedString);
        mPet.setGender(mGender);
        mPet.setWeight(!TextUtils.isEmpty(weightString) ? Integer.parseInt(weightString) : 0);
        if (isEditMode)
            updatePetInDb();
        else
            insertPetInDb();
    }

    private void showUnsavedChangesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_discard_changes))
                .setPositiveButton(R.string.dialog_discard, (dialogInterface, i) -> finish())
                .setNegativeButton(R.string.dialog_keep_editing, (dialogInterface, i) -> {
                    if (dialogInterface != null) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_pet).setMessage(R.string.dialog_delete_pet)
                .setPositiveButton(R.string.delete, (dialogInterface, i) -> {
                    deletePetFromDb();
                    finish();
                }).setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
            if (dialogInterface != null) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }
        showUnsavedChangesDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }
}
