/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.contacts.R;
import com.android.contacts.interactions.ContactDeletionInteraction;
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.util.ImplicitIntentsUtil;

/**
 * Senior-friendly menu activity with large buttons for basic contact operations.
 * This activity provides quick access to the most common contact actions.
 */
public class SeniorMenuActivity extends AppCompatActivity {

    private Button btnAddContact;
    private Button btnCallContact;
    private Button btnViewContacts;
    private Button btnEditContact;
    private Button btnDeleteContact;
    private Button btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.senior_menu_activity);

        initializeViews();
        setUpClickListeners();
    }

    private void initializeViews() {
        btnAddContact = findViewById(R.id.btn_add_contact);
        btnCallContact = findViewById(R.id.btn_call_contact);
        btnViewContacts = findViewById(R.id.btn_view_contacts);
        btnEditContact = findViewById(R.id.btn_edit_contact);
        btnDeleteContact = findViewById(R.id.btn_delete_contact);
        btnSettings = findViewById(R.id.btn_settings);
    }

    private void setUpClickListeners() {
        btnAddContact.setOnClickListener(v -> onAddContactClicked());
        btnCallContact.setOnClickListener(v -> onCallContactClicked());
        btnViewContacts.setOnClickListener(v -> onViewContactsClicked());
        btnEditContact.setOnClickListener(v -> onEditContactClicked());
        btnDeleteContact.setOnClickListener(v -> onDeleteContactClicked());
        btnSettings.setOnClickListener(v -> onSettingsClicked());
    }

    private void onAddContactClicked() {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        startActivity(intent);
    }

    private void onCallContactClicked() {
        // Open contacts list to select a contact to call
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    private void onViewContactsClicked() {
        // Open the main contacts list
        Intent intent = new Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI);
        startActivity(intent);
    }

    private void onEditContactClicked() {
        // Open contacts list to select a contact to edit
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    private void onDeleteContactClicked() {
        // Open contacts list to select a contact to delete
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 3);
    }

    private void onSettingsClicked() {
        startActivity(new Intent(this, ContactsPreferenceActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        Uri contactUri = data.getData();

        switch (requestCode) {
            case 1: // Call contact
                callContact(contactUri);
                break;
            case 2: // Edit contact
                editContact(contactUri);
                break;
            case 3: // Delete contact
                deleteContact(contactUri);
                break;
        }
    }

    private void callContact(Uri contactUri) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(contactUri);
        try {
            startActivity(callIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot make call", Toast.LENGTH_SHORT).show();
        }
    }

    private void editContact(Uri contactUri) {
        Intent editIntent = new Intent(Intent.ACTION_EDIT);
        editIntent.setData(contactUri);
        startActivity(editIntent);
    }

    private void deleteContact(Uri contactUri) {
        ContactDeletionInteraction.start(this, contactUri, false);
    }
}
