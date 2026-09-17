/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.contacts.interactions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.contacts.SimImportService;
import com.android.contacts.database.SimContactDao;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.SimCard;
import com.android.contacts.model.SimContact;
import com.android.contacts.model.account.AccountInfo;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.preference.ContactsPreferences;
import com.google.common.util.concurrent.Futures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Starts the existing SIM import service when a SIM becomes available.
 */
public class AutoSimImportReceiver extends BroadcastReceiver {
    private static final String TAG = "AutoSimImportReceiver";

    public static void startImport(Context context) {
        startImport(context, null);
    }

    public static void startImport(Context context, PendingResult pendingResult) {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    importAvailableContacts(context.getApplicationContext());
                } finally {
                    if (pendingResult != null) {
                        pendingResult.finish();
                    }
                    executor.shutdown();
                }
            }
        });
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_SIM_STATE_CHANGED.equals(intent.getAction())) {
            return;
        }
        startImport(context, goAsync());
    }

    private static void importAvailableContacts(Context context) {
        final SimContactDao dao = SimContactDao.create(context);
        if (!dao.canReadSimContacts()) {
            return;
        }

        final AccountWithDataSet targetAccount = getTargetAccount(context);
        if (targetAccount == null) {
            Log.w(TAG, "No writable account is available for automatic SIM import");
            return;
        }

        for (SimCard sim : dao.getSimCards()) {
            if (sim.isImported() || sim.isDismissed()) {
                continue;
            }
            final ArrayList<SimContact> contacts = dao.loadContactsForSim(sim);
            if (!contacts.isEmpty()) {
                SimImportService.startImport(
                        context, sim.getSubscriptionId(), contacts, targetAccount);
            }
        }
    }

    private static AccountWithDataSet getTargetAccount(Context context) {
        final List<AccountInfo> writableAccounts = Futures.getUnchecked(
                AccountTypeManager.getInstance(context).filterAccountsAsync(
                        AccountTypeManager.writableFilter()));
        if (writableAccounts.isEmpty()) {
            return null;
        }

        final AccountWithDataSet defaultAccount =
                new ContactsPreferences(context).getDefaultAccount();
        if (defaultAccount != null && AccountInfo.contains(writableAccounts, defaultAccount)) {
            return defaultAccount;
        }
        return writableAccounts.get(0).getAccount();
    }
}
