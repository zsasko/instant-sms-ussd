package com.m039.mqst.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.m039.mqst.items.InstantUssd;
import com.m039.mqst.items.InstantSms;
import com.m039.mqst.items.InstantItem;

import com.m039.mqst.R;
import com.m039.mqst.ItemFactory;
import android.widget.Button;
import android.widget.CheckBox;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.widget.TextView;
import android.provider.Contacts.People;
import android.net.Uri;
import android.database.Cursor;
import android.util.Log;

/**
 * Describe class CreationTab here.
 *
 *
 * Created: Fri Sep  2 13:03:08 2011
 *
 * @author <a href="mailto:flam44@gmail.com">Mozgin Dmitry</a>
 * @version 1.0
 */
public class EditActivity extends Activity {
    static private final int TYPE_SMS       = 0;
    static private final int TYPE_USSD      = 1;

    static private final String[] mTypes    = {"sms", "ussd"};
    static private View mSmsLayout;
    static private View mTypeLayout;
    static private ViewGroup.LayoutParams mLayoutParams;

    static {
        mLayoutParams = new LinearLayout.LayoutParams(-1, -1, 1);
    }

    private int mItemPosition = 0;

    // on spinner listener

    private AdapterView.OnItemSelectedListener mTypeSelectListener = new AdapterView.OnItemSelectedListener()  {
            public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {
                selectLayoutType(position);
            }

            public void onNothingSelected (AdapterView<?> parent) {
            }
        };

    private void        selectLayoutType(InstantItem item) {
        selectLayoutType(item.getType());
    }

    private void        selectLayoutType(String type) {
        if (type.equals("sms")) {
            selectLayoutType(TYPE_SMS);
        }

        if (type.equals("ussd")) {
            selectLayoutType(TYPE_USSD);
        }
    }

    private void        selectLayoutType(int type) {
        ViewGroup vg = (ViewGroup) findViewById(R.id.add_activity_layout);
        View v = null;

        if (type == TYPE_SMS) {
            v = mSmsLayout;
        }

        if (type == TYPE_USSD) {
            v = mTypeLayout;
        }

        if (v != null) {
            vg.removeViewAt(0);
            vg.addView(v, 0, mLayoutParams);
        }

        // set spinner selection item

        Spinner s = (Spinner) findViewById(R.id.add_activity_spinner);
        s.setSelection(type);
    }

    // on buttons listener (name is taken from the xml file)

    public void         onButtonClick(View v) {
        int id = v.getId();

        switch (id) {
        case R.id.add_activity_add_button:
            InstantItem item = createInstantItem();

            if (item != null) {
                ItemFactory.getFactory().setItem(mItemPosition, item);
            }

            setResult(RESULT_OK);
            break;
        case R.id.add_activity_cancel_button:
            setResult(RESULT_CANCELED);
            break;
        default:
            break;
        }

        finish();
    }

    // look at add_activity_sms.xml for image button widget

    private static final int PICK_CONTACT_REQUEST      = 1;
    
    public void         onContactButtonClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void      onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case PICK_CONTACT_REQUEST:
            if (resultCode == RESULT_OK) {
                Cursor cur = managedQuery(data.getData(), null, null, null, null);

                try {
                    if (cur.moveToFirst()) {
                        String contactId = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                        int hasPhone = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                        if (hasPhone == 1) {

                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                                       null,
                                                                       ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
                                                                       null,
                                                                       null);

                            if (phones.moveToFirst()) {
                                String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                if (number != null) {
                                    TextView tv = (TextView) findViewById(R.id.add_activity_etext_address);
                                    tv.setText(number);
                                } 
                            } 

                            phones.close();
                        } else {
                            Toast.makeText(this, R.string.contact_hasnt_phone, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (IllegalArgumentException  e) {
                    Log.e("m039", e.getMessage());
                }
            }
            break;
        default:
            break;
        }
    }
    
    @Override
    public void         onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_activity);

        initLayouts();

        Spinner s = (Spinner) findViewById(R.id.add_activity_spinner);

        s.setAdapter(new ArrayAdapter<String>(this,
                                              R.layout.add_activity_spinner_item,
                                              mTypes));

        s.setOnItemSelectedListener(mTypeSelectListener);

        Intent intent = getIntent();

        if (intent != null) {
            mItemPosition = intent.getExtras().getInt("item position");
            InstantItem item = ItemFactory.getFactory().getItem(mItemPosition);

            selectLayoutType(item);
            fillLayoutWithItem(item);
        }
    }

    private void           fillLayoutWithItem(InstantItem item) {
        View layout = findViewById(R.id.add_activity_layout);

        if (item != null) {

            if (item instanceof InstantSms) {
                InstantSms sms = (InstantSms) item;

                EditText ehelp  = (EditText) layout.findViewById(R.id.add_activity_etext_help);
                EditText eaddr  = (EditText) layout.findViewById(R.id.add_activity_etext_address);
                EditText etext  = (EditText) layout.findViewById(R.id.add_activity_etext_text);
                CheckBox cwarn  = (CheckBox) layout.findViewById(R.id.add_activity_cbox_warning);

                ehelp.setText(sms.getHelp());
                eaddr.setText(sms.getAddress());
                etext.setText(sms.getText());
                cwarn.setChecked(sms.getWarning());
            }

            if (item instanceof InstantUssd) {
                InstantUssd ussd = (InstantUssd) item;

                EditText ehelp = (EditText) layout.findViewById(R.id.add_activity_etext_help);
                EditText etext = (EditText) layout.findViewById(R.id.add_activity_etext_text);

                ehelp.setText(ussd.getHelp());
                etext.setText(ussd.getText());
            }
        }
    }

    /**
     * Return null if text fields are empty
     */
    private InstantItem    createInstantItem() {
        Spinner s = (Spinner) findViewById(R.id.add_activity_spinner);
        int position = s.getSelectedItemPosition();

        View layout = findViewById(R.id.add_activity_layout);

        InstantItem item = null;

        if (position == TYPE_SMS) {
            EditText ehelp  = (EditText) layout.findViewById(R.id.add_activity_etext_help);
            EditText eaddr  = (EditText) layout.findViewById(R.id.add_activity_etext_address);
            EditText etext  = (EditText) layout.findViewById(R.id.add_activity_etext_text);
            CheckBox cwarn  = (CheckBox) layout.findViewById(R.id.add_activity_cbox_warning);
            
            String help     = ehelp.getText().toString();
            String addr     = eaddr.getText().toString();
            String text     = etext.getText().toString();
            Boolean warn    = cwarn.isChecked();
            
            if (!help.equals("") &&
                !addr.equals("") &&
                !text.equals("")) {
                item = new InstantSms(help, addr, text, warn);              
            }
        }

        if (position == TYPE_USSD) {
            EditText ehelp = (EditText) layout.findViewById(R.id.add_activity_etext_help);
            EditText etext = (EditText) layout.findViewById(R.id.add_activity_etext_text);

            String help = ehelp.getText().toString();
            String text = etext.getText().toString();

            if (!help.equals("") &&
                !text.equals("")) {
                item = new InstantUssd(help, text);
            }
        }

        return item;
    }

    private void        initLayouts() {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup vg = (ViewGroup) findViewById(R.id.add_activity_layout);
        View v;

        mSmsLayout = inflater.inflate(R.layout.add_activity_sms, null);
        mTypeLayout = inflater.inflate(R.layout.add_activity_ussd, null);

        // set default view
        vg.addView(mSmsLayout, 0, mLayoutParams);

        // adds buttons to layouts
        vg = (ViewGroup) mSmsLayout;
        v = inflater.inflate(R.layout.add_activity_buttons, null);
        vg.addView(v);

        // change text of the button

        Button b;

        b = (Button) v.findViewById(R.id.add_activity_add_button);
        b.setText(R.string.edit_button);

        vg = (ViewGroup) mTypeLayout;
        v = inflater.inflate(R.layout.add_activity_buttons, null);
        vg.addView(v);
    }

}
