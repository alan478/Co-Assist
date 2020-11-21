// Mudi's code

package com.example.covidresourceapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

public class VisualPlannerActivity extends AppCompatActivity {

    public static int length;
    public static int width;

    public static int getLength(){
        return length;
    }

    public static int getWidth(){
        return width;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_planner);

        //listener for generate room
        Button GenRoom = findViewById(R.id.generateRoomButton);
        GenRoom.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button

                //get length as a float
                EditText length_raw = findViewById(R.id.lengthfield);
                length = Integer.valueOf(length_raw.getText().toString());

                //get width as a float
                EditText width_raw = findViewById(R.id.widthfield);
                width = Integer.valueOf(width_raw.getText().toString());

                genRoom();
            }
        });


//        //listener for new corner
//        Button SetCornerButton = findViewById(R.id.setCornerButton);
//
//        SetCornerButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                // Code here executes on main thread after user presses button
//                setCorner();
//            }
//        });


    }

    void genRoom(){
        Intent intent = new Intent(this, ShowEmptyRoom.class);
        startActivity(intent);
    }


//    void setCorner() {
//        Integer position;
//        position = 0;
//        corners.add(position);
//    }


}