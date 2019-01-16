package com.app.vst.christapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import database.DeviceDataManager;
import utils.Common;

public class UIBaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        try
        {
            inflater.inflate(R.menu.menu_global, menu);
            if((this instanceof UIHomeActivity) == false || (DeviceDataManager.Data != null && DeviceDataManager.Data.SelectedUser != null && DeviceDataManager.Data.SelectedUser.IsAdmin == false)) {
                menu.findItem(R.id.mnu_settings).setVisible(false);
            }
            else { menu.findItem(R.id.mnu_settings).setVisible(true); }

            if(DeviceDataManager.Data == null || DeviceDataManager.Data.SelectedUser == null || DeviceDataManager.Data.SelectedUser.ID == null || DeviceDataManager.Data.SelectedUser.ID.trim().length() == 0) {
                menu.findItem(R.id.mnu_user).setVisible(false);
            }
            else {
                MenuItem item = menu.findItem(R.id.mnu_user);
                item.setVisible(true);
                item.setTitle("( " + DeviceDataManager.Data.SelectedUser.Name + " )          ");
            }
        }
        catch(Exception ex) { }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(DeviceDataManager.Data == null || DeviceDataManager.Data.SelectedUser == null || DeviceDataManager.Data.SelectedUser.ID == null || DeviceDataManager.Data.SelectedUser.ID.trim().length() == 0) {
            this.startActivity(new Intent(this, UILoginActivity.class));
            this.finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try
        {
            final int id = item.getItemId();
            final AppCompatActivity pointer = this;
            switch (id) {
                case R.id.mnu_settings:
                    pointer.startActivity(new Intent(pointer, UIConfigActivity.class));
                    break;
                case R.id.mnu_back:
                    this.onBackPressed();
                    break;
                case R.id.mnu_signout:
                    signOutWithConfirm();
                    break;
            }
            return true;
        }
        catch(Exception ex) { }
        return true;
    }
    protected void signOutWithConfirm() {
        final AppCompatActivity pointer = this;

        AlertDialog dlg = new AlertDialog.Builder(pointer)
                .setTitle("Confirm")
                .setMessage("Do you want to sign out?")
                .setCancelable(true)
                .setPositiveButton("Sign out", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DeviceDataManager.Data.SelectedUser = null;
                        DeviceDataManager.saveLocalData(pointer);
                        minimizeApp();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dlg.show();

        Common.styleButton(pointer, dlg.getButton(Dialog.BUTTON_POSITIVE));

        Button button = dlg.getButton(Dialog.BUTTON_NEGATIVE);
        Common.styleButton(pointer, button);
        button.setAlpha(.7F);
    }
    protected void minimizeApp() {
        final AppCompatActivity pointer = this;
        try {
            Intent intent = new Intent(pointer, UILoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            moveTaskToBack(true);
        } catch (Exception ex) {
        }
    }
}
