package com.mglavas.plugins.filechooser;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;

import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.ActivityCallback;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.lang.Exception;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@CapacitorPlugin(name = "FileChooser")
public class FileChooserPlugin extends Plugin {
	
	/** @see <a href="https://stackoverflow.com/a/17861016/459881"></a>*/
	private byte[] getBytesFromInputStream (InputStream is) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buffer = new byte[0xFFFF];

		for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
			os.write(buffer, 0, len);
		}

		return os.toByteArray();
	}

	/** @see <a href="https://stackoverflow.com/a/23270545/459881"></a>*/
	private String getDisplayName (ContentResolver contentResolver, Uri uri) {
		String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
		Cursor metaCursor = contentResolver.query(
				uri, projection, null, null, null
		);

		if (metaCursor != null) {
			try {
				if (metaCursor.moveToFirst()) {
					return metaCursor.getString(0);
				}
			} finally {
				metaCursor.close();
			}
		}

		return "File";
	}

	@PluginMethod
	public void chooseFile(PluginCall call) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		String mimeType = call.getString("mimeType", "*/*");
		intent.setType(mimeType);
		if (mimeType != null && !mimeType.equals("*/*")) {
			intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType.split(","));
		}
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		Intent chooser = Intent.createChooser(intent, "Select File");
		startActivityForResult(call, chooser, "onChooserResult");
	}

	@ActivityCallback
	public void onChooserResult(PluginCall call, ActivityResult activityResult) {
		int resultCode = activityResult.getResultCode();
		try {
			if (resultCode == Activity.RESULT_OK) {
				Intent result = activityResult.getData();

				if (result != null) {
					Uri uri = result.getData();
					ContentResolver contentResolver = getActivity().getContentResolver();
					String name = getDisplayName(contentResolver, uri);

					String mediaType = contentResolver.getType(uri);
					if (mediaType == null || mediaType.isEmpty()) {
						mediaType = "application/octet-stream";
					}

					String base64 = "";

					boolean includeData = Boolean.TRUE.equals(call.getBoolean("includeData", false));
					if (includeData) {
						byte[] bytes = getBytesFromInputStream(contentResolver.openInputStream(uri));
						base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
					}

					JSObject file = new JSObject()
							.put("data", base64)
							.put("mediaType", mediaType)
							.put("name", name)
							.put("uri", uri.toString());
					call.resolve(file);
				}
				else {
					call.reject("File URI was null.");
				}
			}
			else if (resultCode == Activity.RESULT_CANCELED) {
				call.reject("RESULT_CANCELED");
			}
			else {
				call.reject("Result code " + resultCode);
			}
		}
		catch (Exception err) {
			call.reject("Failed to read file: " + err.getMessage());
		}
	}
}
