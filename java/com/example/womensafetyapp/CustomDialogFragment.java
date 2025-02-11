package com.example.womensafetyapp;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CustomDialogFragment extends DialogFragment {
    EditText editName,editNumber;
    Button BtnSubmit,BtnCancel;
    String task;
    Contact contact;
    User user;
    TextView title;
    ImageView phoneBook;
    int globalContact,flow = 0;
    public CustomDialogFragment(String task, Contact contact, User user) {
        this.task=task;
        this.contact=contact;
        this.user=user;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.custom_dialog, container, false);
        title=(TextView)rootView.findViewById(R.id.title);
        editName=(EditText)rootView.findViewById(R.id.editName);
        editNumber=(EditText)rootView.findViewById(R.id.editNumber);
        BtnSubmit=(Button) rootView.findViewById(R.id.save);
        BtnCancel=(Button) rootView.findViewById(R.id.cancel);
        phoneBook = (ImageView) rootView.findViewById(R.id.iv_phonebook1);
        title.setText(task+" Contact Details");
        BtnSubmit.setText(task);
        if(task.equals("Update")){
         editName.setText(contact.getName());
         editNumber.setText(contact.getMobile());
        }
        phoneBook.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                //  openContact1(101);
                globalContact = 101;

                flow = 1;
                // for alove L version we need to get permission at the door to get throught that location ...
                if (getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {  // checkSelfPermission is a method avail in 23 api ..without if condition of (SDK_INT < 23 ) you cant implement it..

                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS}, 2);

                } else {

                    openContact1();
                }
            }
        });
        BtnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(editName.getText().toString().equals("")){
                            Toast.makeText(getActivity(),"Please Enter Name", Toast.LENGTH_SHORT).show();
                            editName.setError("Please Enter Name");
                        } else if(editNumber.getText().toString().equals("")){
                            Toast.makeText(getActivity(),"Please Enter Number",Toast.LENGTH_SHORT).show();
                            editNumber.setError("Please Enter Number");
                        } else if(editNumber.getText().toString().length()>11||editNumber.getText().toString().length()<10){
                            Toast.makeText(getActivity(),"Mobile should contain 10 letters",Toast.LENGTH_SHORT).show();
                            editNumber.setError("Mobile should contain 10 letters");
                        }
                        else{
                            ((ContactsActivity) getActivity()).ContactService(editName.getText().toString(),editNumber.getText().toString(),task,contact.getId());
                        }
                    }
                });
                BtnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((ContactsActivity) getActivity()).closeFragment();
                    }
                });
        // Inflate the layout to use as dialog or embedded fragment
        return rootView;
    }
    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    public void openContact1() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(intent, globalContact);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == (101) && resultCode == getActivity().RESULT_OK && null != data) {
            Uri contactData = data.getData();
            //String[] projection = { Phone.NUMBER, Phone.DISPLAY_NAME };

            Cursor c = getActivity().managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                String id =
                        c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                String hasPhone =
                        c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getActivity().getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                            null, null);
                    phones.moveToFirst();
                    if(phones.getCount()>0){
                        String phn_no1 = phones.getString(phones.getColumnIndex("data1"));
                        String name = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DISPLAY_NAME));
                        editName.setText(name);
                        editNumber.setText(phn_no1);
                    }else{
                        Toast.makeText(getActivity(), "Unable to Add Contact", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        }
    }
}