package com.danielacedo.trabajoficheros;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.DataAsyncHttpResponseHandler;
import com.loopj.android.http.FileAsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    EditText edt_ImagePath, edt_TextPath;
    Button btn_Download;
    TextView txv_galleryPosition, txv_CurrentText, txv_text_position;
    ImageView imv_image;

    private final String errorUploadPath = "http://192.168.1.132/datos/error.php";

    private int currentImage;
    private int currentText;

    private String[] gallery;
    private String[] phrases;

    CustomTimer timerImages;
    CustomTimer timerText;

    private int timerTick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timerTick = 1;
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
                if (checkPathImage())
                    downloadGallery();
                else{
                    imv_image.setImageResource(android.R.color.transparent);
                    txv_galleryPosition.setText("");
                }

                if (checkPathPhrases())
                    downloadPhrases();
                else{
                    txv_CurrentText.setText("");
                    txv_text_position.setText("");
                }
            }
        });

        checkTimerTick();

    }


    private void checkTimerTick(){
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.intervalo)));

        try {
            String line = reader.readLine();

            int intervalo = Integer.parseInt(line);

            if(intervalo > 0){
                timerTick = intervalo;
            }

        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "Error al leer intervalo.txt", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e){
            Toast.makeText(MainActivity.this, "intervalo.txt no contiene un número", Toast.LENGTH_SHORT).show();
        }
    }

    private void set_GalleryDown() {
        if(currentImage == 0){
            currentImage = gallery.length-1;
        }else{
            currentImage = (currentImage-1)%gallery.length;
        }
        setImage();
    }

    private void set_GalleryUp(){
        currentImage = (currentImage+1)%gallery.length;
        setImage();
    }

    private void set_PhraseDown() {
        if(currentText == 0){
            currentText = phrases.length-1;
        }else{
            currentText = (currentText-1)%phrases.length;
        }
        setPhrase();
    }

    private void set_PhraseUp(){
        currentText = (currentText+1)%phrases.length;
        setPhrase();
    }

    private void setImage(){
        try{
            Picasso.Builder picassoBuilder = new Picasso.Builder(MainActivity.this);

            picassoBuilder = picassoBuilder.listener(new Picasso.Listener() {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                    uploadError("Error al cargar imagen de url: "+uri+" .Error: "+exception.getMessage());
                }
            });


            picassoBuilder.build().load(gallery[currentImage]).error(R.drawable.error).placeholder(R.drawable.progressanimation).into(imv_image);
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
            refreshTextPosition();
        }catch(IndexOutOfBoundsException ex){
            Toast.makeText(MainActivity.this, "No hay frases en la galeria", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPathImage(){
        boolean ok = true;

        if(edt_ImagePath.getText().toString().isEmpty()){
            Toast.makeText(MainActivity.this, "Dirección de imagen vacía", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        return ok;
    }

    private boolean checkPathPhrases(){
        boolean ok = true;

        if(edt_TextPath.getText().toString().isEmpty()){
            Toast.makeText(MainActivity.this, "Dirección de frases vacía", Toast.LENGTH_SHORT).show();
            ok = false;
        }

        return ok;
    }

    private void uploadError(String error){

        if(!Patterns.WEB_URL.matcher(errorUploadPath).matches()){
            Log.e("Error","El path para subir errores no es valido");
            return;
        }

        RequestParams params = new RequestParams();
        String errorConFecha = DateTime.now().toString("d-M-y H:m:s") + " : " + error;
        params.add("error", errorConFecha);

        RestClient.post(errorUploadPath, params, new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Problema al subir el error", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Toast.makeText(MainActivity.this, responseString, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadGallery(){


        if(!edt_ImagePath.getText().toString().startsWith("http://")){
            String text = "http://"+ edt_ImagePath.getText().toString();

            edt_ImagePath.setText(text);
        }

        if(!Patterns.WEB_URL.matcher(edt_ImagePath.getText().toString()).matches()){
            Toast.makeText(MainActivity.this, "URL no valida", Toast.LENGTH_SHORT).show();
            return;
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
                Toast.makeText(MainActivity.this, "Fallo en la conexión al descargar archivo de imagenes", Toast.LENGTH_SHORT).show();
                uploadError("Fallo en la conexión al descargar archivo "+edt_ImagePath.getText().toString());
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

                    reader.close();
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

                    if(timerImages != null){
                        timerImages.cancel();
                    }

                    currentImage = 0;
                    setImage();
                    timerImages = new CustomTimer(gallery.length, timerTick, new CustomTimer.TimerCallback() {
                        @Override
                        public void onTick() {
                            set_GalleryUp();
                        }
                    });

                    timerImages.start();
                }else{
                    disableGallery();
                    Toast.makeText(MainActivity.this, "Fichero de imagenes mal formateado", Toast.LENGTH_SHORT).show();
                    uploadError("Fichero de imágenes mal formateado");
                }
            }

        });
    }

    private void downloadPhrases(){

        if(!edt_TextPath.getText().toString().startsWith("http://")){
            String text = "http://"+ edt_TextPath.getText().toString();
            edt_TextPath.setText(text);
        }

        if(!Patterns.WEB_URL.matcher(edt_TextPath.getText().toString()).matches()){
            Toast.makeText(MainActivity.this, "URL no valida", Toast.LENGTH_SHORT).show();
            return;
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
                Toast.makeText(MainActivity.this, "Fallo en la conexión al fichero de frases", Toast.LENGTH_SHORT).show();
                uploadError("Fallo en la conexión al fichero de frases "+edt_TextPath.getText().toString());
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, File response){
                //Read every line and add each link to the list
                progress.dismiss();

                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(response), "UTF-8"));
                    String line;

                    while((line = reader.readLine()) != null){
                        if(!line.equals("")) { //Ignore blank lines
                            urls.add(line);
                        }
                    }

                    reader.close();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "Fallo de lectura del archivo de frases", Toast.LENGTH_SHORT);
                    uploadError("Fallo de lectura del archivo de frases");
                }

                if (urls.size() != 0) {
                    phrases = new String[urls.size()];

                    for (int i = 0; i < urls.size(); i++) {
                        phrases[i] = urls.get(i);
                    }

                    if(timerText != null){
                        timerText.cancel();
                    }

                    currentText = 0;
                    setPhrase();
                    timerText = new CustomTimer(phrases.length, timerTick, new CustomTimer.TimerCallback() {
                        @Override
                        public void onTick() {
                            set_PhraseUp();
                        }
                    });

                    timerText.start();
                }
            }

        });
    }

    private void refreshGalleryPositionText(){
        txv_galleryPosition.setText((currentImage+1)+"/"+gallery.length);
    }

    private void refreshTextPosition(){
        txv_text_position.setText((currentText+1)+"/"+phrases.length);
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
