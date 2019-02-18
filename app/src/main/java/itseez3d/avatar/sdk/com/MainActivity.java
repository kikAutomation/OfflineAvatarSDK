/* Copyright (C) Itseez3D, Inc. - All Rights Reserved
* You may not use this file except in compliance with an authorized license
* Unauthorized copying of this file, via any medium is strictly prohibited
* Proprietary and confidential
* UNLESS REQUIRED BY APPLICABLE LAW OR AGREED BY ITSEEZ3D, INC. IN WRITING, SOFTWARE DISTRIBUTED UNDER THE LICENSE IS DISTRIBUTED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OR
* CONDITIONS OF ANY KIND, EITHER EXPRESS OR IMPLIED
* See the License for the specific language governing permissions and limitations under the License.
* Written by Itseez3D, Inc. <support@itseez3D.com>, September 2018
*/

package itseez3d.avatar.sdk.com;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.nio.file.Path;

public class MainActivity extends AppCompatActivity {

    static final int TAKE_PHOTO_REQUEST_CODE = 1;
    static final int CHOOSE_PHOTO_REQUEST_CODE = 2;

    Button cameraButton = null;
    Button choosePhotoButton = null;
    TextView statusText = null;

    File photoFile = null;

    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Context context = this;
        cameraButton = (Button)findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePhotoIntent.resolveActivity(getPackageManager()) != null) {
                    Uri photoUri = FileProvider.getUriForFile(context, "com.example.android.fileprovider", photoFile);
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST_CODE);
                }
            }
        });

        choosePhotoButton = (Button) findViewById(R.id.choose_photo_button);
        choosePhotoButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent selectPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectPhotoIntent.setType("image/*");
                startActivityForResult(selectPhotoIntent, CHOOSE_PHOTO_REQUEST_CODE);
            }
        });

        statusText = (TextView) findViewById(R.id.progress_text);
        statusText.setText("Initializing Avatar SDK...");

        Runnable initRunnable = new Runnable() {
            @Override
            public void run() {
                initialize();
            }
        };
        Thread initThread = new Thread(initRunnable);
        initThread.start();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                0);
        }

        photoFile = new File(getExternalFilesDir(null), "photo.jpg");
    }

    @Override
    protected void onDestroy() {
        deinitializeAvatarSdk();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;


        if (requestCode == TAKE_PHOTO_REQUEST_CODE) {
            generateAvatarFromPhoto(photoFile.toString());
        }
        else if (requestCode == CHOOSE_PHOTO_REQUEST_CODE) {
            String imageFilePath = UriParser.GetPathToFile(this, data.getData());
            generateAvatarFromPhoto(imageFilePath);
        }
    }

    public void updateProgress(final float progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText("Progress: " + String.valueOf(progress) + "%");
            }
        });
    }

    private void initialize() {
        AvatarSdkResourceManager resourceManager = new AvatarSdkResourceManager(this);
        boolean isResourcesExtracted = resourceManager.extractResourcesIfNeeded(getAssets());
        if (!isResourcesExtracted){
            statusText.setText("Unable to extract resources!");
            return;
        }

        final int code = initializeAvatarSdk(resourceManager.getResourcesPath().getPath());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            if (code == 0)
            {
                enableButtons(true);
                statusText.setText("Avatar SDK is initialized!");
            }
            else
                statusText.setText("Unable to initialize Avatar SDK!");
            }
        });
    }

    private void generateAvatarFromPhoto(final String photoPath)
    {
        enableButtons(false);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                final int code = generateAvatar(photoPath);
                final long elapsedTime = System.currentTimeMillis() - startTime;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    enableButtons(true);
                    if (code == 0) {
                        statusText.setText(String.format("Avatar is generated. Calculation time: %f seconds", elapsedTime / 1000.0f));
                    }
                    else
                        statusText.setText("Unable to generate avatar!");
                    }
                });
            }
        };
        Thread generationThread = new Thread(runnable);
        generationThread.start();
    }


    private void enableButtons(boolean isEnabled)
    {
        choosePhotoButton.setEnabled(isEnabled);
        cameraButton.setEnabled(isEnabled);
    }


    private native int generateAvatar(String photoPath);
    private native int initializeAvatarSdk(String resourcePath);
    private native int deinitializeAvatarSdk();
}
