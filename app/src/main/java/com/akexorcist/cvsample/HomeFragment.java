package com.akexorcist.cvsample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.cloudvision.CVRequest;
import com.akexorcist.cloudvision.CVResponse;
import com.akexorcist.cloudvision.CloudVision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Headers;

/**
 * Created by ndiaz on 08-06-2018.
 */

public class HomeFragment extends Fragment implements View.OnClickListener, CloudVision.Callback {

    private final static String apiKey = "AIzaSyBIiNrqWaxjZJwub6JHxvmxMYhfbZc7_qM";
    private static final int TAKE_PICTURE = 1;
    private static final int IMAGE_FROM_GALLERY = 2;
    private ImageButton btnCamara;
    private ImageButton btnGaleria;
    private TextView txtScanned;
    private ImageView imagePreview;
    private Activity activity;
    private Context context;
    private Uri uriPhotoForScan;
    private String pathPhotoForScan;
    private ImageButton btnCheck;
    private Bitmap imageForScan;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        activity = getActivity();
        context = getContext();

        btnCamara = (ImageButton) view.findViewById(R.id.btnCamara);
        btnCamara.setOnClickListener(this);

        btnGaleria = (ImageButton) view.findViewById(R.id.btnFiles);
        btnGaleria.setOnClickListener(this);

        btnCheck = (ImageButton) view.findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(this);

        txtScanned = (TextView) view.findViewById(R.id.txtScanned);

        imagePreview = (ImageView) view.findViewById(R.id.image_container);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCamara:
                openCameraForResult();
                break;
            case R.id.btnFiles:
                openGalleryForResult();
                break;
            case R.id.btnCheck:
                ocrScanStart();
                break;
            default:
        }
    }

    private void ocrScanStart() {
        Log.d("SCAN_INIT", "Inicio de detección");
        String data = CloudVision.convertBitmapToBase64String(this.imageForScan);
        CVRequest request = createCVRequest(data);
        CloudVision.runImageDetection(apiKey, request, this);
    }

    private CVRequest createCVRequest(String data) {
        CVRequest.Image image = new CVRequest.Image(data);
        CVRequest.Feature featureLabelDetection = new CVRequest.Feature(CVRequest.FeatureType.LABEL_DETECTION, 5);
        CVRequest.Feature featureTextDetected = new CVRequest.Feature(CVRequest.FeatureType.TEXT_DETECTION, 5);
        List<CVRequest.Feature> featureList = new ArrayList<>();
        featureList.add(featureLabelDetection);
        featureList.add(featureTextDetected);
        List<CVRequest.Request> requestList = new ArrayList<>();
        requestList.add(new CVRequest.Request(image, featureList));
        return new CVRequest(requestList);
    }

    private void openGalleryForResult() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, IMAGE_FROM_GALLERY);
    }

    private void openCameraForResult() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(), UUID.randomUUID() + ".jpg");
        uriPhotoForScan = Uri.fromFile(photo);
        pathPhotoForScan = photo.getAbsolutePath();
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    previewPhoto(data);
                }
                break;
            case IMAGE_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK) {
                    previewPhoto(data);
                }
                break;
        }
    }

    private void previewPhoto(Intent data) {
        if (data.getData() != null && data.getExtras() != null) {
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            imageForScan = (Bitmap) extras.get("data");
            imagePreview.setImageBitmap(bitmap);
        } else {
            Toast.makeText(context, "No se pudo obtener imágen, por favor reintente", Toast.LENGTH_LONG).show();
        }
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onImageDetectionSuccess(boolean isSuccess, int statusCode, Headers headers, CVResponse cvResponse) {
        if (cvResponse != null && cvResponse.isResponsesAvailable()) {
            CVResponse.Response response = cvResponse.getResponse(0);
            if (response.isLabelAvailable()) {
//                LabelAdapter adapter = new LabelAdapter(response.getLabels());
//                lvLabel.setAdapter(adapter);
                String s = response.getTexts().get(0).getDescription();
                txtScanned.setText(s);

                Log.d("NICO", "FIN de reconocimiento");
            }
        }
    }

    @Override
    public void onImageDetectionFailure(Throwable t) {

    }
}
