/*
    This file is part of the Browser WebApp.

    Browser WebApp is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Browser WebApp is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with the Browser webview app.

    If not, see <http://www.gnu.org/licenses/>.
 */

package de.baumann.browser.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.mobapphome.mahencryptorlib.MAHEncryptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.channels.FileChannel;

import de.baumann.browser.R;
import de.baumann.browser.about.About_activity;


public class Activity_settings extends AppCompatActivity {

    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        helper_main.onStart(Activity_settings.this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.menu_settings);

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        PreferenceManager.setDefaultValues(this, R.xml.user_settings, false);
        PreferenceManager.setDefaultValues(this, R.xml.user_settings_search, false);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.edit().putString("openURL", sharedPref.getString("startURL", "https://github.com/scoute-dich/browser/")).apply();
        sharedPref.edit().putString("started", "").apply();

        // Display the fragment as the activity_screen_main content
        getFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences sharedPref;
        private MAHEncryptor mahEncryptor;
        private String toDecode;
        private FrameLayout frameLayout;

        private void addOpenSettingsListener() {

            final Activity activity = getActivity();
            Preference reset = findPreference("settings");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    getActivity().startActivity(intent);

                    return true;
                }
            });
        }

        private void addRestore_searchChooseListener() {

            Preference reset = findPreference("searchChoose");

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    helper_main.switchToActivity(getActivity(), Activity_settings_search.class);
                    return true;
                }
            });
        }

        private void addLicenseListener() {

            Preference reset = findPreference("license");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    helper_main.switchToActivity(getActivity(), About_activity.class);
                    return true;
                }
            });
        }

        private void addBackup_dbListener() {

            Preference reset = findPreference("backup_db");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    final CharSequence[] options = {
                            getString(R.string.action_backup),
                            getString(R.string.action_restore)};
                    new AlertDialog.Builder(getActivity())
                            .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            })
                            .setItems(options, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int item) {
                                    if (options[item].equals(getString(R.string.action_backup))) {
                                        File directory = new File(Environment.getExternalStorageDirectory() + "/Android/data/browser.backup/");
                                        if (!directory.exists()) {
                                            //noinspection ResultOfMethodCallIgnored
                                            directory.mkdirs();
                                        }

                                        final CharSequence[] options = {
                                                getString(R.string.app_title_bookmarks),
                                                getString(R.string.app_title_readLater),
                                                getString(R.string.app_title_passStorage),
                                                getString(R.string.app_title_history),
                                                getString(R.string.action_whiteList),
                                                getString(R.string.menu_all)};
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.action_backup))
                                                .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                })
                                                .setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int item) {
                                                        if (options[item].equals(getString(R.string.app_title_bookmarks))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_backup))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            backup_Bookmarks();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.app_title_readLater))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_backup))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            backup_ReadLater();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.app_title_passStorage))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_backup))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            backup_passStorage();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.app_title_history))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_backup))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            backup_history();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.action_whiteList))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_backup))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            backup_whiteList();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.menu_all))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_backup))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            backup_Bookmarks();
                                                                            backup_ReadLater();
                                                                            backup_passStorage();
                                                                            backup_history();
                                                                            backup_whiteList();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                    }
                                                }).show();
                                    }
                                    if (options[item].equals(getString(R.string.action_restore))) {

                                        final CharSequence[] options = {
                                                getString(R.string.app_title_bookmarks),
                                                getString(R.string.app_title_readLater),
                                                getString(R.string.app_title_passStorage),
                                                getString(R.string.app_title_history),
                                                getString(R.string.action_whiteList),
                                                getString(R.string.menu_all)};
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle(getString(R.string.action_backup))
                                                .setPositiveButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        dialog.cancel();
                                                    }
                                                })
                                                .setItems(options, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int item) {
                                                        if (options[item].equals(getString(R.string.app_title_bookmarks))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_restore))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            restore_Bookmarks();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.app_title_readLater))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_restore))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            restore_ReadLater();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.app_title_passStorage))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_restore))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            restore_passStorage();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.app_title_history))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_restore))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            restore_history();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.action_whiteList))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_restore))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            restore_whiteList();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                        if (options[item].equals(getString(R.string.menu_all))) {
                                                            new AlertDialog.Builder(getActivity())
                                                                    .setTitle(getString(R.string.toast_confirmation_title))
                                                                    .setMessage(getString(R.string.toast_confirmation_restore))
                                                                    .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            restore_Bookmarks();
                                                                            restore_ReadLater();
                                                                            restore_passStorage();
                                                                            restore_history();
                                                                            restore_whiteList();
                                                                        }
                                                                    })
                                                                    .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int whichButton) {
                                                                            dialog.cancel();
                                                                        }
                                                                    }).show();
                                                        }
                                                    }
                                                }).show();
                                    }
                                }
                            }).show();
                    return true;
                }
            });
        }

        private void addProtectListener() {

            Preference reset = findPreference("protect_PW");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View dialogView = View.inflate(getActivity(), R.layout.dialog_pin, null);

                    final EditText pass_userPW = (EditText) dialogView.findViewById(R.id.pass_userPin);

                    try {
                        toDecode = mahEncryptor.decode(sharedPref.getString("protect_PW", ""));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    pass_userPW.setText(toDecode);

                    builder.setView(dialogView);
                    builder.setTitle(R.string.action_protect);
                    builder.setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {

                            try {
                                String toEncode = mahEncryptor.encode(pass_userPW.getText().toString().trim());
                                sharedPref.edit().putString("protect_PW", toEncode).apply();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    });

                    final AlertDialog dialog2 = builder.create();
                    // Display the custom alert dialog on interface
                    dialog2.show();
                    helper_editText.showKeyboard(getActivity(), pass_userPW, 0, toDecode, getActivity().getString(R.string.pw_hint));

                    return true;
                }
            });
        }

        private void addWhiteListListener() {

            Preference reset = findPreference("whiteList");
            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {

                    final android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setTitle(R.string.app_conf)
                            .setMessage(helper_main.textSpannable(getString(R.string.toast_whiteList_confirm)))
                            .setPositiveButton(R.string.toast_yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    sharedPref.edit().putString("whiteList", "").apply();
                                    Snackbar.make(frameLayout, getString(R.string.toast_whiteList), Snackbar.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(R.string.toast_cancel, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });
                    dialog.show();

                    return true;
                }
            });
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            PreferenceManager.setDefaultValues(getActivity(), R.xml.user_settings, false);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.user_settings_search, false);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            frameLayout = (FrameLayout) getActivity().findViewById(R.id.content_frame);

            try {
                mahEncryptor = MAHEncryptor.newInstance(sharedPref.getString("saved_key", ""));
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(frameLayout, getString(R.string.toast_error), Snackbar.LENGTH_LONG).show();
            }

            addPreferencesFromResource(R.xml.user_settings);
            addLicenseListener();
            addOpenSettingsListener();
            addBackup_dbListener();
            addRestore_searchChooseListener();
            addProtectListener();
            addWhiteListListener();
        }

        private void backup_Bookmarks () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "bookmarks_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "bookmarks_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_backup), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_backup_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void backup_ReadLater () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath3 = "//data//" + "de.baumann.browser"
                            + "//databases//" + "readLater_DB_v01.db";
                    String backupDBPath3 = "//Android//" + "//data//" + "//browser.backup//" + "readLater_DB_v01.db";
                    File currentDB3 = new File(data, currentDBPath3);
                    File backupDB3 = new File(sd, backupDBPath3);

                    FileChannel src3 = new FileInputStream(currentDB3).getChannel();
                    FileChannel dst3 = new FileOutputStream(backupDB3).getChannel();
                    dst3.transferFrom(src3, 0, src3.size());
                    src3.close();
                    dst3.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_backup), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_backup_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void backup_passStorage () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "pass_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "pass_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_backup), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_backup_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void backup_history () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "history_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "history_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_backup), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_backup_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void backup_whiteList () {
            try {
                File directory = new File(Environment.getExternalStorageDirectory() + "/Android/data/browser.backup/");
                String whiteList = sharedPref.getString("whiteList", "");
                File whiteListBackup = new File(directory, "whiteList.txt");
                FileWriter writer = new FileWriter(whiteListBackup);
                writer.append(whiteList);
                writer.flush();
                writer.close();

                Snackbar.make(frameLayout, getString(R.string.toast_backup), Snackbar.LENGTH_LONG).show();

            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_backup_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void restore_Bookmarks () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "bookmarks_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "bookmarks_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_restore), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_restore_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void restore_ReadLater () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "readLater_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "readLater_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_restore), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_restore_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void restore_passStorage () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "pass_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "pass_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_restore), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_restore_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void restore_history () {
            try {
                File sd = Environment.getExternalStorageDirectory();
                File data = Environment.getDataDirectory();

                if (sd.canWrite()) {
                    String currentDBPath = "//data//" + "de.baumann.browser"
                            + "//databases//" + "history_DB_v01.db";
                    String backupDBPath = "//Android//" + "//data//" + "//browser.backup//" + "history_DB_v01.db";
                    File currentDB = new File(data, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();

                    Snackbar.make(frameLayout, getString(R.string.toast_restore), Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_restore_not), Snackbar.LENGTH_LONG).show();
            }
        }

        private void restore_whiteList () {
            try {
                File directory = new File(Environment.getExternalStorageDirectory() + "/Android/data/browser.backup/");
                File whiteListBackup = new File(directory, "whiteList.txt");
                StringBuilder text = new StringBuilder();

                BufferedReader br = new BufferedReader(new FileReader(whiteListBackup));
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
                sharedPref.edit().putString("whiteList", text.toString()).apply();
                Snackbar.make(frameLayout, getString(R.string.toast_restore), Snackbar.LENGTH_LONG).show();
            } catch (Exception e) {
                Snackbar.make(frameLayout, getString(R.string.toast_restore_not), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        updateSettings();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            updateSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateSettings () {
        sharedPref.edit().putString("openURL", "settings").apply();
        if (sharedPref.getBoolean ("java", false)){
            sharedPref.edit().putString("java_string", getString(R.string.app_yes)).apply();
        } else {
            sharedPref.edit().putString("java_string", getString(R.string.app_no)).apply();
        }

        if (sharedPref.getString ("cookie", "2").equals("1")){
            sharedPref.edit().putString("cookie_string", getString(R.string.app_yes)).apply();
        } else if (sharedPref.getString ("cookie", "2").equals("2")) {
            sharedPref.edit().putString("cookie_string", getString(R.string.app_yes)).apply();
        } else if (sharedPref.getString ("cookie", "2").equals("3")) {
            sharedPref.edit().putString("cookie_string", getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("loc", false)){
            sharedPref.edit().putString("loc_string", getString(R.string.app_yes)).apply();
        } else {
            sharedPref.edit().putString("loc_string", getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("blockads_bo", false)){
            sharedPref.edit().putString("blockads_string", getString(R.string.app_yes)).apply();
        } else {
            sharedPref.edit().putString("blockads_string", getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("request_bo", false)){
            sharedPref.edit().putString("request_string", getString(R.string.app_yes)).apply();
        } else {
            sharedPref.edit().putString("request_string", getString(R.string.app_no)).apply();
        }

        if (sharedPref.getBoolean ("pictures", false)){
            sharedPref.edit().putString("pictures_string", getString(R.string.app_yes)).apply();
        } else {
            sharedPref.edit().putString("pictures_string", getString(R.string.app_no)).apply();
        }

        finishAffinity();
    }
}