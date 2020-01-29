package com.annjad.pets;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PetsAdapter extends ArrayAdapter<Pet> {

    public PetsAdapter(@NonNull Context context, int resource, @NonNull List<Pet> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext())
                    .inflate(R.layout.pet_list_view_item, parent, false);
        }
        Pet currentPet = getItem(position);
        if (currentPet != null) {
            TextView petName = listItemView.findViewById(R.id.pet_name);
            petName.setText(currentPet.getName());
            TextView petBreed = listItemView.findViewById(R.id.pet_breed);
            if (TextUtils.isEmpty(currentPet.getBreed())) {
                petBreed.setText(R.string.unknown_breed);
            } else {
                petBreed.setText(currentPet.getBreed());
            }
        }

/*        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext().getApplicationContext(), EditorActivity.class);
                intent.putExtra("isEditMode", true);
                if (currentPet != null) {
                    intent.putExtra("editPetId", currentPet.getPetId());
                }
                getContext().startActivity(intent);
            }
        });*/
        return listItemView;
    }
}
