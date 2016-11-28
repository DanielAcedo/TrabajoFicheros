package com.danielacedo.trabajoficheros;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    EditText edt_ImagePath, edt_TextPath;
    Button btn_Download;
    TextView txv_galleryPosition, txv_CurrentText, txv_text_position;
    ImageView imv_image;

    private int currentImage;
    private int currentText;

    private String[] gallery;
    private String[] phrases;

    private int timerTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTick = 0;
        currentImage = 0;
        currentText = 0;

        gallery = new String[1];
        phrases = new String[1];

        edt_ImagePath = (EditText)findViewById(R.id.edt_ImagesPath);
        edt_TextPath = (EditText)findViewById(R.id.edt_TextPath);
        btn_Download = (Button)findViewById(R.id.btn_Download);
        imv_image = (ImageView)findViewById(R.id.imv_image);
        txv_galleryPosition = (TextView)findViewById(R.id.txv_galleryPosition);
        txv_CurrentText = (TextView)findViewById(R.id.txv_CurrentText);
        txv_text_position = (TextView)findViewById(R.id.txv_text_position);

        disableGallery();

        btn_Download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkPath()){
                    //downloadGallery();
                    downloadPhrases();
                }
            }
        });

    }

    private void setBtn_GalleryDown() {
        if(currentImage == 0){
            currentImage = gallery.length-1;
        }else{
            currentImage = (currentImage-1)%gallery.length;
        }
        setImage();
    }

    private void setBtn_GalleryUp(){
        currentImage = (currentImage+1)%gallery.length;
        setImage();
    }

    private void setImage(){
        try{
            Picasso.with(MainActivity.this).load(gallery[currentImage]).error(R.drawable.error).placeholder(R.drawable.progressanimation).into(imv_image);
            refreshGalleryPositionText();
        }
        catch(IndexOutOfBoundsException ex){
            Toast.makeText(MainActivity.this, "No hay imagenes en la galeria", Toast.LENGTH_SHORT).show();
        }
        catch(IllegalArgumentException ex){
            Toast.makeText(MainActivity.this, "La galeria no está correctamente formateada", Toast.LENGTH_SHORT).show();
        }

    }

    private void setPhrase(){
        try{
            txv_CurrentText.setText(phrases[currentText]);

        }catch(IndexOutOfBoundsException ex){
            Toast.makeText(MainActivity.this, "No hay frases en la galeria", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPath(){
        boolean ok = true;

        if(edt_ImagePath.getText().toString().isEmpty()){
            Toast.makeText(MainActivity.this, "Dirección vacía", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        return ok;
    }

    private void downloadGallery(){


        if(!edt_ImagePath.getText().toString().startsWith("http://")){
            String text = "http://"+ edt_ImagePath.getText().toString();
            edt_ImagePath.setText(text);
        }

        final ProgressDialog progress = new ProgressDialog(MainActivity.this);

        RestClient.get(edt_ImagePath.getText().toString(), new FileAsyncHttpResponseHandler(MainActivity.this) {
            private List<String> urls = new ArrayList<String>();

            @Override
            public void onStart() {
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage("Conectando . . .");
                progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        RestClient.cancelRequests(getApplicationContext(), true);
                    }
                });
                progress.show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                progress.dismiss();
                disableGallery();
                Toast.makeText(MainActivity.this, "Fallo en la conexión", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response){
                //Read every line and add each link to the list
                progress.dismiss();

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;

                    while((line = reader.readLine()) != null){
                        if(!line.equals("")) { //Ignore blank lines
                            urls.add(line);
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Fallo de lectura del archivo", Toast.LENGTH_SHORT);
                }

                if (urls.size() != 0)
                    gallery = new String[urls.size()];

                for(int i = 0; i<urls.size(); i++){
                    gallery[i] = urls.get(i);
                }

                if(checkGalleryFile()){
                    enableGallery();
                    setImage();
                }else{
                    disableGallery();
                    Toast.makeText(MainActivity.this, "Fichero mal formateado", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void downloadPhrases(){

        if(!edt_TextPath.getText().toString().startsWith("http://")){
            String text = "http://"+ edt_TextPath.getText().toString();
            edt_TextPath.setText(text);
        }

        final ProgressDialog progress = new ProgressDialog(MainActivity.this);

        RestClient.get(edt_TextPath.getText().toString(), new FileAsyncHttpResponseHandler(MainActivity.this) {
            private List<String> urls = new ArrayList<String>();

            @Override
            public void onStart() {
                progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progress.setMessage("Conectando . . .");
                progress.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        RestClient.cancelRequests(getApplicationContext(), true);
                    }
                });
                progress.show();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                progress.dismiss();
                Toast.makeText(MainActivity.this, "Fallo en la conexión", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response){
                //Read every line and add each link to the list
                progress.dismiss();

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    String line;

                    while((line = reader.readLine()) != null){
                        if(!line.equals("")) { //Ignore blank lines
                            urls.add(line);
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Fallo de lectura del archivo", Toast.LENGTH_SHORT);
                }

                if (urls.size() != 0)
                    phrases = new String[urls.size()];

                for(int i = 0; i<urls.size(); i++){
                    phrases[i] = urls.get(i);
                }


            }

        });
    }

    private void refreshGalleryPositionText(){
        txv_galleryPosition.setText((currentImage+1)+"/"+gallery.length);
        txv_text_position.setText((currentText+1)+"/"+txv_text_position);
    }

    private void enableGallery(){
        refreshGalleryPositionText();
    }

    private void disableGallery(){
        txv_galleryPosition.setText("");
        txv_text_position.setText("");
    }

    private boolean checkGalleryFile(){
        boolean result = true;

        for (int i = 0; i<gallery.length; i++){
            if(!Patterns.WEB_URL.matcher(gallery[i]).matches()){
                result = false;
                break;
            }
        }

        return result;
    }

}
