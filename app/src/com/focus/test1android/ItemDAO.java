package com.focus.test1android;

/**
 * Created by Harvey Yang on 2015/8/5.
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//
// ��ƥ\�����O
public class ItemDAO{
    // ���W��
    public static final String TABLE_NAME = "AppHourBlock";

    // �s��������W�١A�T�w����
    public static final String KEY_ID = "_id";

    // �䥦������W��
    public static final String APPHOURBLOCK = "appHourBlock";

    // �ϥΤW���ŧi���ܼƫإߪ�檺SQL��O
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    APPHOURBLOCK + " TEXT)";

    // ��Ʈw����
    private SQLiteDatabase db;

    // �غc�l�A�@�몺���γ����ݭn�ק�
    public ItemDAO(Context context) {
        db = MyDBHelper.getDatabase(context);
    }

    // ������Ʈw�A�@�몺���γ����ݭn�ק�
    public void close() {
        db.close();
    }

    // �s�W�Ѽƫ�w������
    public long insert(JSONObject _apphourblock) throws JSONException {
        // �إ߷ǳƷs�W��ƪ�ContentValues����
        String appInString = _apphourblock.toString();

        ContentValues cv = new ContentValues();

        // �[�JContentValues����]�˪��s�W���
        // �Ĥ@�ӰѼƬO���W�١A �ĤG�ӰѼƬO��쪺���

        cv.put(APPHOURBLOCK, appInString);

        // �s�W�@����ƨè�o�s��
        // �Ĥ@�ӰѼƬO���W��
        // �ĤG�ӰѼƬO�S����w���Ȫ��w�]��
        // �ĤT�ӰѼƬO�]�˷s�W��ƪ�ContentValues����
        return db.insert(TABLE_NAME, null, cv);
    }

    // �ק�Ѽƫ�w������
    public boolean update(JSONObject _apphourblock, long id) throws JSONException {
        // �إ߷ǳƭק��ƪ�ContentValues����
        String appInString = _apphourblock.toString();

        ContentValues cv = new ContentValues();

        // �[�JContentValues����]�˪��ק���
        // �Ĥ@�ӰѼƬO���W�١A �ĤG�ӰѼƬO��쪺���
        cv.put(APPHOURBLOCK, appInString);

        // �]�w�ק��ƪ���󬰽s��
        // �榡���u���W�١׸�ơv
        String where = KEY_ID + "=" + id;

        // ����ק��ƨæ^�ǭק諸��Ƽƶq�O�_���\
        return db.update(TABLE_NAME, cv, where, null) > 0;
    }

    // �R���Ѽƫ�w�s�������
    public boolean delete(long id){
        // �]�w��󬰽s���A�榡���u���W��=��ơv
        String where = KEY_ID + "=" + id;
        // �R����w�s����ƨæ^�ǧR���O�_���\
        return db.delete(TABLE_NAME, where , null) > 0;
    }

    // Ū��Ҧ��O�Ƹ��
    public List<String> getAll() throws JSONException {
        /*
        JSONArray hourblocks = new JSONArray();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);
        Log.v("ItemDAO", "start counting");
        while(cursor.moveToNext()){
            Log.v("ItemDAO", "count + 1");
            String appInString = cursor.getString(1);
            hourblocks.put(new JSONObject(appInString));
        }
        Log.v("ItemDAO", "stop counting");

        cursor.close();
        return hourblocks;
        */
        List<String> hourblocks= new ArrayList<String>();
        Cursor cursor = db.query(
                TABLE_NAME, null, null, null, null, null, null, null);
        Log.v("ItemDAO", "start counting");
        while(cursor.moveToNext()){
            Log.v("ItemDAO", "count + 1");
            hourblocks.add(cursor.getString(1));
        }
        Log.v("ItemDAO", "stop counting");

        cursor.close();
        return hourblocks;
    }

    // ��o��w�s������ƪ���
    public JSONObject get(long id) throws JSONException {
        // �ϥνs�����d�߱��
        String where = KEY_ID + "=" + id;
        String appInString;
        // ����d��
        Cursor cursor = db.query(
                TABLE_NAME, null, where, null, null, null, null, null);

        // �p�G���d�ߵ��G
        if (cursor.moveToFirst()) {
            // Ū��]�ˤ@����ƪ�����
            appInString = cursor.getString(1);
            // ����Cursor����
            cursor.close();
            // �^�ǵ��G
            return (new JSONObject(appInString));
        }
        cursor.close();
        return null;
    }

    // ��o��Ƽƶq
    public int getCount() {
        int result = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);

        if (cursor.moveToNext()) {
            result = cursor.getInt(0);
        }

        return result;
    }
}
