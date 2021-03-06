package com.capps.question;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.capps.question.Login.User;
import com.capps.question.Question.Question;

import java.util.IllegalFormatException;

/**
 * Created by varun on 27/3/17.
 */

public class AppDataBase extends SQLiteOpenHelper {


    //TODO:: end transaction and close DB after any connection..
    public static final String ID_COLUMN="id";

    private final static String DB_NAME = "QUESTION_DB";
    private static int DB_VERSION = 4;
    private static AppDataBase INSTANCE=null;

    public static final String USER_T = "users";
    public static final String USER_COLUMN_NAME = "name";
    public static final String USER_COLUMN_EMAIL = "email";
    public static final String USER_COLUMN_ADMIN = "admin";
    private final String USER_COLUMN_PASS = "pass";
    public static final String USER_COLUMN_POINT = "point";
    public static final String USER_COLUMN_FULL_MARK = "full_mark";


    public static final String QUESTION_T = "questions";
    public static final String QUESTION_COLUMN_QUESTION= "question";

    public static final String ANSWER_T = "answers";
    public static final String ANSWER_COLUMN_ANSWER= "answer";
    public static final String ANSWER_COLUMN_QUESTION_ID= "question_id";
    public static final String ANSWER_COLUMN_CURRECT= "currect";

    private AppDataBase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public static AppDataBase getInstance(Context context){
        if (INSTANCE ==null){
            INSTANCE = new AppDataBase(context,DB_NAME,null,DB_VERSION);
        }
        return INSTANCE;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserT = "CREATE TABLE " + USER_T +" (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                        USER_COLUMN_NAME + " VARCHAR(51) NOT NULL," +
                                                        USER_COLUMN_EMAIL + " VARCHAR(51) UNIQUE NOT NULL," +
                                                        USER_COLUMN_PASS + "  VARCHAR(21)," +
                                                        USER_COLUMN_POINT + "  int," +
                                                        USER_COLUMN_FULL_MARK + "  int," +
                                                        USER_COLUMN_ADMIN + " boolean DEFAULT (0)" +
                                                        ");";
        String insertAdmin = "INSERT INTO " + USER_T + " (" + USER_COLUMN_EMAIL + "," + USER_COLUMN_NAME + "," + USER_COLUMN_PASS + "," + USER_COLUMN_ADMIN + ")" +
                "VALUES ('Admin@email.com','ADMIN','12345',1)";

        String createQuestionT = "CREATE TABLE " + QUESTION_T + " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                   QUESTION_COLUMN_QUESTION + " VARCHAR(51) );";

        String createAnswerT= "CREATE TABLE " + ANSWER_T + " (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                                                ANSWER_COLUMN_ANSWER+ " VARCHAR(51)," +
                                                ANSWER_COLUMN_QUESTION_ID+ " INTEGER," +
                                                ANSWER_COLUMN_CURRECT+ " BOOLEAN DEFAULT(0)," +
                                                "FOREIGN KEY(" +  ANSWER_COLUMN_QUESTION_ID + ") REFERENCES " + QUESTION_T + "(id) );";



        db.execSQL(createUserT);
        db.execSQL(insertAdmin);
        db.execSQL(createQuestionT);
        db.execSQL(createAnswerT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


        db.execSQL("DROP TABLE IF EXISTS " + USER_T);
        db.execSQL("DROP TABLE IF EXISTS " + QUESTION_T);
        db.execSQL("DROP TABLE IF EXISTS " + ANSWER_T);
        onCreate(db);

    }

    public Cursor getRow(String sql){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);
        return cursor;
    }

    public Cursor getRow(String sql,String selection){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,new String[] {selection});
        return cursor;
    }

    public Cursor getCoulmnTable(String tableName,String column,boolean includeIdCoumn){
        String sql;
        if (includeIdCoumn){
            sql = "SELECT id," + column + " FROM " + tableName + " ORDER BY " + ID_COLUMN;
        }else {
            sql = "SELECT " + column + " FROM " + tableName + " ORDER BY " + ID_COLUMN;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);
        return cursor;
    }

    public Cursor getAllTable(String tableName){
        String sql = "SELECT * FROM " + tableName + " ORDER BY " + ID_COLUMN;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);
        return cursor;
    }

    public Cursor getAllTable(String tableName,String whereClassColumnName,String whereClassIdValue){
        String sql = "SELECT * FROM " + tableName + " WHERE " + whereClassColumnName + " = ?" + " ORDER BY " + ID_COLUMN;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,new String[]{whereClassIdValue + ""});
        return cursor;
    }

    public short countRows(String tableName){
        String sql = "SELECT COUNT(*) FROM " + tableName;
        Cursor value = this.getReadableDatabase().rawQuery(sql,null,null);
        if (value.moveToFirst())
            return (short) value.getInt(0);
        else
            return 0;
    }




    //Users Method

    //create
    public long createUser(User user){

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(USER_COLUMN_NAME,user.getName());
        values.put(USER_COLUMN_EMAIL,user.getEmail());
        values.put(USER_COLUMN_ADMIN,user.getAdmin());

        try{
            db.beginTransaction();
            int result = (int) db.insertOrThrow(USER_T,null,values);
            db.setTransactionSuccessful();
            db.endTransaction();

            return result;
        }catch (IllegalFormatException err){
            Log.d("ERROR","error in create user in db");
            return -1;
        }

    }

    //check password login
    public boolean checkAdminPass(String pass){
        String command = "SELECT id FROM " + USER_T +
                          " WHERE " +USER_COLUMN_PASS +" = ?";

        if (getRow(command,pass).moveToFirst())
            return true;
        else
            return false;
    }
    //save points to db
    public boolean savePoint(short user_id,short point,short fullMark){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_COLUMN_POINT,point);
        values.put(USER_COLUMN_FULL_MARK,fullMark);


        int rows = db.update(USER_T,values, ID_COLUMN + " = ?;",new String[]{user_id + ""});
        if (rows > 0)
            return true;
        else
            return false;
    }




    //Question Methods

    public long saveQuestion(Question q){
        long rowID=-1;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(QUESTION_COLUMN_QUESTION,q.getmQuestion());
        db.beginTransaction();
        rowID = db.insertOrThrow(QUESTION_T, null,values);
        db.setTransactionSuccessful();
        db.endTransaction();

        return rowID;
    }



    //Answer Method
    public boolean saveAnswer(Answer []answers,long question_id){

        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "INSERT INTO " + ANSWER_T + "(" + ANSWER_COLUMN_ANSWER+ "," +
                                                        ANSWER_COLUMN_QUESTION_ID + "," +
                                                        ANSWER_COLUMN_CURRECT + ") VALUES (?,?,?)";
        SQLiteStatement statement = db.compileStatement(sql);
        short c ;  //TODO::  short >> byte  (for memory)

        try {

            db.beginTransaction();
            for (Answer answer :answers) {
                statement.clearBindings();
                c= answer.isCurrect()? (short) 1 : (short) 0;
                statement.bindString(1,answer.getAnswer());
                statement.bindLong(2,question_id);
                statement.bindLong(3,c);
                statement.executeInsert();
            }
            db.setTransactionSuccessful();
            return true;
        }
        catch (Exception e)
        {
            Log.d("dbError",e.getMessage());
            return false;
        }
        finally
        {
            statement.close();
            db.endTransaction();
            db.close();
        }
    }
}
