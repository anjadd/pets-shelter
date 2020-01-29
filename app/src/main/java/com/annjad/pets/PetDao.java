package com.annjad.pets;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import io.reactivex.Single;

@Dao
public interface PetDao {

    @Query("SELECT * FROM pet")
    Single<List<Pet>> getAllPets();

    @Query("SELECT * FROM pet WHERE _id = :petId")
    Single<Pet> getPetById(int petId);

    @Insert
    void insertNewPet(Pet pet);

    @Insert
    void insertAllPets(List<Pet> pets);

    @Update
    void updatePet(Pet pet);

    @Delete
    void deletePet(Pet pet);

    @Query("DELETE FROM pet")
    void deleteAllPets();
}
