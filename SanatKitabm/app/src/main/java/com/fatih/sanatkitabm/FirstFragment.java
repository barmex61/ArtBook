package com.fatih.sanatkitabm;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class FirstFragment extends Fragment {
    private SQLiteDatabase db;
    int x=1;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;
    RecyclerView recyclerView;
    public FirstFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db= requireContext().openOrCreateDatabase("Art", Context.MODE_PRIVATE,null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).getSupportActionBar().show();
        recyclerView=view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        artArrayList=new ArrayList<>();
        artAdapter=new ArtAdapter(artArrayList);
        recyclerView.setAdapter(artAdapter);
        getData();
    }

    private void getData(){

        db.execSQL("CREATE TABLE IF NOT EXISTS Art(id INTEGER PRIMARY KEY,art String,artist String,year String,image BLOB)");
        Cursor cursor=db.rawQuery("SELECT id,art FROM Art", null);
        int idX=cursor.getColumnIndex("id");
        int artX=cursor.getColumnIndex("art");
        while (cursor.moveToNext()){
            int id=cursor.getInt(idX);
            String Art=cursor.getString(artX);
            Art artInstance=new Art(Art, id);
            artArrayList.add(artInstance);
            System.out.println("Çağırıldı"+x++);
        } cursor.close();

    }

}