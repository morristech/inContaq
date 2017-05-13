package nyc.c4q.jonathancolon.inContaq.data.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;

import io.realm.Realm;
import nyc.c4q.jonathancolon.inContaq.contactlist.model.Contact;
import nyc.c4q.jonathancolon.inContaq.realm.RealmHelper;
import nyc.c4q.jonathancolon.inContaq.sms.model.Sms;

public class GetAllSmsWorkerTask extends AsyncTask<Long, Void, ArrayList<Sms>> {

    private static final String TAG = GetAllSmsWorkerTask.class.getSimpleName();
    private final Context context;
    private OnSmsListLoaded listener;

    private static final String URI_ALL = "content://sms/";
    private static final String ADDRESS = "address";
    private static final String BODY = "body";
    private static final String DATE = "date";
    private static final String TYPE = "type";
    private static final String DATE_DESC = "date desc";
    private static final String INBOX = "inbox";
    private static final String SENT = "sent";
    private ArrayList<Sms> smsList;

    @Override
    protected ArrayList<Sms> doInBackground(Long... params) {
        return getAllSms(context, params[0]);
    }

    public GetAllSmsWorkerTask(OnSmsListLoaded listener, Context context) {
        this.context = context;
        this.listener = listener;
    }


    private ArrayList<Sms> getAllSms(Context context, long realmID) {
        Sms smsObject;

        Realm realm = RealmHelper.getInstance();
        Contact contact = RealmHelper.getByRealmID(realm, realmID);

        if (contact.getMobileNumber() != null) {
            smsList = new ArrayList<>();


            if (context != null) {
                Log.e(TAG, "getAllSms: ATTEMPTING TO GET SMS" );
                Uri uri = Uri.parse(URI_ALL);
                String[] projection = new String[]{ADDRESS, BODY, DATE, TYPE};
                Cursor cursor = context.getApplicationContext().getContentResolver().query(uri,
                        projection, ADDRESS + "='" + contact.getMobileNumber() + "'", null, DATE_DESC);


                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int totalSMS = cursor.getCount();
                        for (int i = 0; i < totalSMS; i++) {
                            smsObject = new Sms();
                            smsObject.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS)).replaceAll("\\s+", ""));
                            smsObject.setMsg(cursor.getString(cursor.getColumnIndexOrThrow(BODY)));
                            smsObject.setTime(cursor.getString(cursor.getColumnIndexOrThrow(DATE)));
                            smsObject.setType(cursor.getString(cursor.getColumnIndexOrThrow(TYPE)));

                            if (cursor.getString(cursor.getColumnIndexOrThrow(TYPE)).contains("1")) {
                                smsObject.setFolderName(INBOX);
                            } else {
                                smsObject.setFolderName(SENT);
                            }
                            addSmsToRealmDB(realm, smsObject);
                            smsList.add(smsObject);
                            cursor.moveToNext();
                        }
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }
                    }
                }
            }
            RealmHelper.closeRealm(realm);
        }
        return smsList;
    }

    private static void addSmsToRealmDB(Realm realm, final Sms smsObject) {
        realm.executeTransaction(realm1 -> {
            long realmId = 1;
            // TODO: 5/8/17 CHECK TO SEE IF SMS ID EXISTS IN DATABASE
            if (realm1.where(Sms.class).count() > 0) {
                realmId = realm1.where(Sms.class).max("realmId").longValue() + 1; // auto-increment id
            }
            Log.e(TAG, "getAllSms: " + smsObject.getMsg());
            smsObject.setRealmId(realmId);
            realm1.copyToRealmOrUpdate(smsObject);
        });
    }

    @Override
    protected void onPostExecute(ArrayList<Sms> smsArrayList) {
        super.onPostExecute(smsArrayList);
        listener.onSmsListLoaded(smsArrayList);
    }

}




