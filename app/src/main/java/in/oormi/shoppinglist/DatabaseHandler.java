package in.oormi.shoppinglist;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "tasksManager";
    private static final String TABLE_TASKS = "tasks";
    private static final String TABLE_DETAILS = "details";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_QTY = "quantity";
    private static final String KEY_COST = "cost";
    private static final String KEY_NOTES = "notes";
    private static final String KEY_TASKID = "taskid";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_SEQ = "seq";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TASKS_TABLE = "CREATE TABLE " + TABLE_TASKS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_TASKID + " INTEGER,"
                + KEY_ENABLED + " INTEGER"
                + ")";

        db.execSQL(CREATE_TASKS_TABLE);

        String CREATE_DETAILS_TABLE = "CREATE TABLE " + TABLE_DETAILS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_QTY + " TEXT,"
                + KEY_COST + " TEXT,"
                + KEY_NOTES + " TEXT,"
                + KEY_TASKID + " INTEGER,"
                + KEY_SEQ + " INTEGER" +")";

        db.execSQL(CREATE_DETAILS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DETAILS);

        // Create tables again
        onCreate(db);
    }

    // add the new tasks
    void addData(GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, task.getTask());
        values.put(KEY_TASKID, task.getId());
        values.put(KEY_ENABLED, task.getEnabled());

        // Inserting Row
        long id = db.insert(TABLE_TASKS, null, values);
        values.clear();

        ArrayList<ChildInfo> cinfolist = task.getDetailsList();

        for (int n = 0; n < cinfolist.size(); n ++) {
            ChildInfo cinfo = cinfolist.get(n);

            String quantity = cinfo.getQuantity();
            String cost = cinfo.getCost();
            String notes = cinfo.getNotes();

            values.put(KEY_QTY, quantity);
            values.put(KEY_COST, cost);
            values.put(KEY_NOTES, notes);
            values.put(KEY_TASKID, task.getId());
            values.put(KEY_SEQ, n);

            db.insert(TABLE_DETAILS, null, values);
        }

        db.close();
    }
    // add the new tasks
    void insertData(int at, GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();

        //update all taskids in tasks
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int idx = 0, ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(2));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_TASKS + " SET " + KEY_TASKID + " = "
                            + "\"" + String.valueOf(idx + 1) + "\"" + " WHERE " + KEY_ID
                            + " = " + "\"" + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                }
                //ntask++;

            } while (cursor.moveToNext());
        }

        //update all taskids in details
        selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " ORDER BY " + KEY_ID + " ASC";
        cursor = db.rawQuery(selectQuery, null);
        idx = 0;
        ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(4));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_TASKID + " = "
                            + "\"" + String.valueOf(idx + 1) + "\""  + " WHERE " + KEY_ID
                            + " = " + "\"" + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                   }
                //ntask++;

            } while (cursor.moveToNext());
        }

        addData(task);
        cursor.close();
        db.close();
    }

    public List<GroupInfo> getAllTasks() {
        List<GroupInfo> taskList = new ArrayList<GroupInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " ORDER BY " + KEY_TASKID + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                GroupInfo task = new GroupInfo();
                task.setTask(cursor.getString(1), Integer.parseInt(cursor.getString(2)));
                boolean b = cursor.getString(3).equals("1");
                task.setEnabled(b);
                taskList.add(task);
            } while (cursor.moveToNext());
        }
        for (int ntask = 0; ntask < taskList.size(); ntask++) {
            selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " WHERE "
                    + KEY_TASKID + " = " + String.valueOf(taskList.get(ntask).getId())
             + " ORDER BY " + KEY_SEQ + " ASC";
            cursor = db.rawQuery(selectQuery, null);
            ArrayList<ChildInfo> childInfoArrayList = new ArrayList<ChildInfo>();

            if (cursor.moveToFirst()) {
                do {
                    ChildInfo cinfo = new ChildInfo();
                    cinfo.setQuantity(cursor.getString(1));
                    cinfo.setCost(cursor.getString(2));
                    cinfo.setNotes(cursor.getString(3));
                    childInfoArrayList.add(cinfo);
                } while (cursor.moveToNext());
            }
            taskList.get(ntask).setDetailsList(childInfoArrayList);
        }
        cursor.close();
        return taskList;
    }

    public int updateTask(GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, task.getTask());
        values.put(KEY_ENABLED, task.getEnabled());
        //return db.update(TABLE_TASKS, values, KEY_ID + " = ?", new String[] { "ID" });
        return db.update(TABLE_TASKS, values,
                KEY_TASKID + String.format(" = %d", task.getId()), null);
    }

    public void insertStep(GroupInfo task, int at) {
        SQLiteDatabase db = this.getWritableDatabase();
        //update all seq in details
        String selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " WHERE "
                + KEY_TASKID + " = " + task.getId();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int seq = 0;
        int nstep = 1;

        //increment all seq after new one
        if (cursor.moveToFirst()) {
            do {
                seq = Integer.parseInt(cursor.getString(4));
                nstep = Integer.parseInt(cursor.getString(0));
                if (seq>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_SEQ + " = " + "\""
                            + String.valueOf(seq + 1) + "\""  + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(nstep) + "\"";
                    db.execSQL(selectQuery);
                }
            } while (cursor.moveToNext());
        }

        //add new step
        ContentValues values = new ContentValues();
        values.put(KEY_QTY, task.getDetailsList().get(at).getQuantity());
        values.put(KEY_COST, task.getDetailsList().get(at).getCost());
        values.put(KEY_SEQ, task.getDetailsList().get(at).getSequence());
        values.put(KEY_TASKID, task.getId());
        db.insert(TABLE_DETAILS, null, values);

        cursor.close();
        db.close();
    }

    public void deleteStep(GroupInfo task, int at) {
        SQLiteDatabase db = this.getWritableDatabase();
        //remove step
        db.delete(TABLE_DETAILS, KEY_TASKID + " = ? AND " + KEY_SEQ + " = ?",
                new String[] { String.valueOf(task.getId()),
                        String.valueOf(task.getDetailsList().get(at).getSequence()) });

        //update all seq in details
        String selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " WHERE "
                + KEY_TASKID + " = " + task.getId();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int seq = 0;
        int nstep = 1;

        //decrement all seq after deleted one
        if (cursor.moveToFirst()) {
            do {
                seq = Integer.parseInt(cursor.getString(4));
                nstep = Integer.parseInt(cursor.getString(0));
                if (seq>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_SEQ + " = " + "\""
                            + String.valueOf(seq - 1) + "\""  + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(nstep) + "\"";
                    db.execSQL(selectQuery);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public void updateStep(GroupInfo task, int pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        //ContentValues values = new ContentValues();
        String quantity = task.getDetailsList().get(pos).getQuantity();
        String cost = task.getDetailsList().get(pos).getCost();
        String notes = task.getDetailsList().get(pos).getNotes();
        int tid = task.getId();
        int tseq = task.getDetailsList().get(pos).getSequence();

        //db.update(TABLE_DETAILS, values, KEY_TASKID + String.format(" = %d", task.getId()), null);
        String selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_QTY + " = "
                + "\"" + quantity + "\""  + " WHERE " + KEY_TASKID + " = " + tid + " AND "
                + KEY_SEQ + " = " + tseq;
        db.execSQL(selectQuery);

        selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_COST + " = " + "\""
                + cost + "\""  + " WHERE " + KEY_TASKID + " = " + tid
                + " AND " + KEY_SEQ + " = " + tseq;
        db.execSQL(selectQuery);

        selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_NOTES + " = " + "\""
                + notes + "\""  + " WHERE " + KEY_TASKID + " = " + tid
                + " AND " + KEY_SEQ + " = " + tseq;
        db.execSQL(selectQuery);
    }

    public void deleteTask(int at, GroupInfo task) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DETAILS, KEY_TASKID + " = ?", new String[]{ String.valueOf(task.getId()) });
        db.delete(TABLE_TASKS, KEY_TASKID + " = ?", new String[] { String.valueOf(task.getId()) });

        //update all taskids in tasks
        String selectQuery = "SELECT  * FROM " + TABLE_TASKS + " ORDER BY " + KEY_ID + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, null);
        int idx = 0, ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(2));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_TASKS + " SET " + KEY_TASKID + " = " + "\""
                            + String.valueOf(idx - 1) + "\"" + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                }

            } while (cursor.moveToNext());
        }

        //update all taskids in details
        selectQuery = "SELECT  * FROM " + TABLE_DETAILS + " ORDER BY " + KEY_ID + " ASC";
        cursor = db.rawQuery(selectQuery, null);
        idx = 0;
        ntask = 1;

        if (cursor.moveToFirst()) {
            do {
                idx = Integer.parseInt(cursor.getString(4));
                ntask = Integer.parseInt(cursor.getString(0));
                if (idx>=at){
                    selectQuery = "UPDATE " + TABLE_DETAILS + " SET " + KEY_TASKID + " = " + "\""
                            + String.valueOf(idx - 1) + "\""  + " WHERE " + KEY_ID + " = " + "\""
                            + String.valueOf(ntask) + "\"";
                    db.execSQL(selectQuery);
                }

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public int getTaskCount() {
        String countQuery = "SELECT  * FROM " + TABLE_TASKS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int tcount = cursor.getCount();
        cursor.close();

        return tcount;
    }

    public void resetDB () throws SQLException {
        SQLiteDatabase db = this.getWritableDatabase ();
        db.delete(TABLE_TASKS, null, null);
        db.delete(TABLE_DETAILS, null, null);
        db.close ();
    }
}
