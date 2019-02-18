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


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AvatarSdkResourceManager {
    private final String RESOURCES_DIR = "resources";

    private File resourcesDirectory = null;

    public  AvatarSdkResourceManager(Context context)
    {
        resourcesDirectory = new File(context.getExternalFilesDir(null), RESOURCES_DIR);
    }


    public boolean extractResourcesIfNeeded(AssetManager assetManager)
    {
        try {
            if (!resourcesDirectory.exists())
                resourcesDirectory.mkdir();

            String[] resourceFiles = assetManager.list("resources");
            if (resourceFiles.length == resourcesDirectory.listFiles().length)
                return true;

            for(int i=0; i<resourceFiles.length; i++)
            {
                if (!extractResourceFile(resourceFiles[i], assetManager))
                    return false;
            }

            return true;
        }
        catch (IOException exc) {
            Log.e("Extract resources", "Unable to extract resources", exc);
            return  false;
        }
    }

    public File getResourcesPath()
    {
        if (resourcesDirectory != null)
            return resourcesDirectory;
        return null;
    }

    private boolean extractResourceFile(String resourceName, AssetManager assetManager)
    {
        boolean isExtracted = false;
        InputStream input = null;
        OutputStream output = null;
        try {
            input = assetManager.open("resources/" + resourceName);
            output = new FileOutputStream(new File(resourcesDirectory, resourceName));

            byte[] buffer = new byte[32 * 1024 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
            output.close();
            input.close();

            isExtracted = true;
        }
        catch(Exception exc){
            Log.e("Extract resources", String.format("Unable to extract resources file: %s", resourceName), exc);
        }
        return isExtracted;
    }


}
