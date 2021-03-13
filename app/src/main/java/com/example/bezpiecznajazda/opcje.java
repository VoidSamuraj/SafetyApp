package com.example.bezpiecznajazda;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class opcje extends AppCompatActivity {
    Button zapis;
    EditText phone;
    SeekBar pasek;
    EditText wartosc;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.opcje);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        zapis =(Button)findViewById(R.id.zapis);
        phone=(EditText)findViewById(R.id.tel);
        phone.setText(Integer.toString(pobierzNr()));
        pasek=(SeekBar)findViewById(R.id.pasek);
        wartosc=(EditText)findViewById(R.id.wartosc);
        wartosc.setText(String.valueOf(pobierzSkale()) );
        pasek.setProgress(pobierzSkale()-1);

        pasek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                wartosc.setText(String.valueOf(progress+1));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        zapis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phone.getText()!=null&&!phone.getText().toString().equals("")){
                int n=Integer.parseInt(phone.getText().toString());
                int w=Integer.parseInt(wartosc.getText().toString());
                zapisz(n,w); 
                }

            }
        });
        super.onCreate(savedInstanceState);
    }
    private void zapisz(int nr,int skala){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=prefs.edit();
        editor.putInt("nr",nr);
        editor.putInt("skala",skala);
        editor.commit();
    }
    private int pobierzNr(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=prefs.edit();
        return prefs.getInt("nr",123456789);

    }
    private int pobierzSkale(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor=prefs.edit();
        return prefs.getInt("skala",1);

    }
}
