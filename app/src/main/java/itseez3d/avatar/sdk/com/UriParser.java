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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class UriParser
{
    public static String GetPathToFile(Context context, Uri uri)
    {
        if (DocumentsContract.isDocumentUri(context, uri))
        {
            if (IsExternalStorageDocument(uri))
            {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                if ("primary".equals(type.toLowerCase()))
                {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                else
                {
                    return "//sdcard/" + split[1];
                }
                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (IsDownloadsDocument(uri))
            {
                String id = DocumentsContract.getDocumentId(uri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                String dataColumn = GetDataColumn(context, contentUri, null, null);
                return dataColumn;
            }
            // MediaProvider
            else if (IsMediaDocument(uri))
            {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type.toLowerCase()))
                {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("video".equals(type.toLowerCase()))
                {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if ("audio".equals(type.toLowerCase()))
                {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = "_id=?";
                String[] selectionArgs = new String[] { split[1] };
                String dataColumn = GetDataColumn(context, contentUri, selection, selectionArgs);
                return dataColumn;
            }
            else if (isGoogleDriveDocument(uri))
            {
                //TODO: implement copying from Google Drive
                return "";
            }
        }
        // MediaStore (and general)
        else if ("content".equals(uri.getScheme().toLowerCase()))
        {
            // Return the remote address
            if (isGooglePhotosUri(uri))
            {
                return uri.getLastPathSegment();
            }
            String dataColumn = GetDataColumn(context, uri, null, null);
            return dataColumn;
        }
        // File
        else if ("file".equals(uri.getScheme().toLowerCase()))
        {
            return uri.getPath();
        }

        return null;
    }

    private static boolean IsExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean IsMediaDocument(Uri uri)
    {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static boolean IsDownloadsDocument(Uri uri)
    {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isGoogleDriveDocument(Uri uri)
    {
        return "com.google.android.apps.docs.storage".equals(uri.getAuthority());
    }

    private static boolean isGooglePhotosUri(Uri uri)
    {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    private static String GetDataColumn(Context context, Uri uri, String selection, String[] selectionArgs)
    {
        Cursor cursor = null;
        String column = "_data";
        String[] projection = { column };
        try
        {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst())
            {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        }
        finally
        {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

}
