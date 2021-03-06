package com.jahnold.syncaudiobookplayer.Activities;

//Heavily based on code from
//https://github.com/mburman/Android-File-Explore
//	Version of Aug 13, 2011
//Also contributed:
//  Sugan Krishnan (https://github.com/rgksugan) - Jan 2013.
//

//Project type now is Android library: 
//  http://developer.android.com/guide/developing/projects/projects-eclipse.html#ReferencingLibraryProject

//General Java imports 
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;

//Android imports 
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.view.*;
//import android.widget.*;

import com.jahnold.syncaudiobookplayer.R;


public class FileBrowserActivity extends ActionBarActivity {
	// Intent Action Constants
	public static final String INTENT_ACTION_SELECT_DIR = "ua.com.vassiliev.androidfilebrowser.SELECT_DIRECTORY_ACTION";
	public static final String INTENT_ACTION_SELECT_FILE = "ua.com.vassiliev.androidfilebrowser.SELECT_FILE_ACTION";

	// Intent parameters names constants
	public static final String startDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPath";
	public static final String returnDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPathRet";
	public static final String returnFileParameter = "ua.com.vassiliev.androidfilebrowser.filePathRet";
	public static final String showCannotReadParameter = "ua.com.vassiliev.androidfilebrowser.showCannotRead";
	public static final String filterExtension = "ua.com.vassiliev.androidfilebrowser.filterExtension";

	// Stores names of traversed directories
	ArrayList<String> pathDirsList = new ArrayList<>();

	// Check if the first level of the directory structure is the one showing
	// private Boolean firstLvl = true;

	private static final String LOGTAG = "F_PATH";

	private List<Item> fileList = new ArrayList<Item>();
	private File path = null;
	private String chosenFile;
	// private static final int DIALOG_LOAD_FILE = 1000;

	ArrayAdapter<Item> adapter;

	private boolean showHiddenFilesAndDirs = true;
	private boolean directoryShownIsEmpty = false;
	private String filterFileExtension = null;

	// Action constants
	private static int currentAction = -1;
	private static final int SELECT_DIRECTORY = 1;
	private static final int SELECT_FILE = 2;

    private String mBookId;
    private int mPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// In case of
		// ua.com.vassiliev.androidfilebrowser.SELECT_DIRECTORY_ACTION
		// Expects com.mburman.fileexplore.directoryPath parameter to
		// point to the start folder.
		// If empty or null, will start from SDcard root.
		setContentView(R.layout.activity_filebrowser);

		// Set action for this activity
		Intent thisInt = this.getIntent();
		currentAction = SELECT_DIRECTORY;// This would be a default action in
											// case not set by intent
		if (thisInt.getAction().equalsIgnoreCase(INTENT_ACTION_SELECT_FILE)) {

			currentAction = SELECT_FILE;
		}

		showHiddenFilesAndDirs = thisInt.getBooleanExtra(showCannotReadParameter, true);
		filterFileExtension = thisInt.getStringExtra(filterExtension);

        // if a book id has been passed in grab it
        mBookId = thisInt.getStringExtra("book_id");
        mPosition = thisInt.getIntExtra("position", -1);

		setInitialDirectory();

		parseDirectoryPath();
		loadFileList();
		this.createFileListAdapter();
		this.initializeButtons();
		this.initializeFileListView();
		updateCurrentDirectoryTextView();

	}

	private void setInitialDirectory() {
		Intent thisInt = this.getIntent();
		String requestedStartDir = thisInt
				.getStringExtra(startDirectoryParameter);

		if (requestedStartDir != null && requestedStartDir.length() > 0) {
			File tempFile = new File(requestedStartDir);
			if (tempFile.isDirectory())
				this.path = tempFile;
		}

		if (this.path == null) {// No or invalid directory supplied in intent
								// parameter
			if (Environment.getExternalStorageDirectory().isDirectory() && Environment.getExternalStorageDirectory().canRead())
				path = Environment.getExternalStorageDirectory();
			else
				path = new File("/");
		}
			// parameter
	}

	private void parseDirectoryPath() {
		pathDirsList.clear();
		String pathString = path.getAbsolutePath();
		String[] parts = pathString.split("/");
		int i = 0;
		while (i < parts.length) {
			pathDirsList.add(parts[i]);
			i++;
		}
	}

	private void initializeButtons() {
		Button upDirButton = (Button) this.findViewById(R.id.upDirectoryButton);
		upDirButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				loadDirectoryUp();
				loadFileList();
				adapter.notifyDataSetChanged();
				updateCurrentDirectoryTextView();
			}
		});

		Button selectFolderButton = (Button) this.findViewById(R.id.selectCurrentDirectoryButton);
		if (currentAction == SELECT_DIRECTORY) {
			selectFolderButton.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {

					returnDirectoryFinishActivity();
				}
			});
		} else {
			selectFolderButton.setVisibility(View.GONE);
		}
	}

	private void loadDirectoryUp() {
		// present directory removed from list
		String s = pathDirsList.remove(pathDirsList.size() - 1);
		// path modified to exclude present directory
		path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
		fileList.clear();
	}

	private void updateCurrentDirectoryTextView() {
		int i = 0;
		String curDirString = "";
		while (i < pathDirsList.size()) {
			curDirString += pathDirsList.get(i) + "/";
			i++;
		}
		if (pathDirsList.size() == 0) {
			((Button) this.findViewById(R.id.upDirectoryButton)).setEnabled(false);
			curDirString = "/";
		}
        else {
            ((Button) this.findViewById(R.id.upDirectoryButton)).setEnabled(true);
        }

		((TextView) this.findViewById(R.id.currentDirectoryTextView)).setText("Current directory: " + curDirString);
	}

	private void showToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	private void initializeFileListView() {
		ListView lView = (ListView) this.findViewById(R.id.fileListView);
		lView.setBackgroundColor(Color.LTGRAY);
		LinearLayout.LayoutParams lParam = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		lParam.setMargins(15, 5, 15, 5);
		lView.setAdapter(this.adapter);
		lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {

                chosenFile = fileList.get(position).file;
				File sel = new File(path + "/" + chosenFile);

				if (sel.isDirectory()) {
					if (sel.canRead()) {
						// Adds chosen directory to list
						pathDirsList.add(chosenFile);
						path = new File(sel + "");

						loadFileList();
						adapter.notifyDataSetChanged();
						updateCurrentDirectoryTextView();

					}
                    else {
						showToast("Path does not exist or cannot be read");
					}
				}
				else {


					if (!directoryShownIsEmpty) {

						returnFileFinishActivity(sel.getAbsolutePath());
					}
				}
			}
		});
	}

	private void returnDirectoryFinishActivity() {

        Intent retIntent = new Intent();
		retIntent.putExtra(returnDirectoryParameter, path.getAbsolutePath());

        // pass the book id back if we've got it
        if (mBookId != null) {
            retIntent.putExtra("book_id", mBookId);
        }
        if (mPosition != -1) {
            retIntent.putExtra("position", mPosition);
        }

		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}

	private void returnFileFinishActivity(String filePath) {

        Intent retIntent = new Intent();
		retIntent.putExtra(returnFileParameter, filePath);
		this.setResult(RESULT_OK, retIntent);
		this.finish();
	}

	private void loadFileList() {
		try {
			path.mkdirs();
		} catch (SecurityException e) {
			Log.e(LOGTAG, "unable to write on the sd card ");
		}
		fileList.clear();

		if (path.exists() && path.canRead()) {
			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					File sel = new File(dir, filename);
					boolean showReadableFile = showHiddenFilesAndDirs || sel.canRead();
					// Filters based on whether the file is hidden or not
					if (currentAction == SELECT_DIRECTORY) {

                        return (sel.isDirectory() && showReadableFile);

					}
					if (currentAction == SELECT_FILE) {

						// If it is a file check the extension if provided
						if (sel.isFile() && filterFileExtension != null) {
							return (showReadableFile && sel.getName().endsWith(filterFileExtension));
						}
						return (showReadableFile);
					}
					return true;
				}// public boolean accept(File dir, String filename) {
			};

			String[] fList = path.list(filter);
			this.directoryShownIsEmpty = false;
			for (int i = 0; i < fList.length; i++) {
				// Convert into file path
				File sel = new File(path, fList[i]);

				int drawableID = R.drawable.file_icon;
				boolean canRead = sel.canRead();
				// Set drawables
				if (sel.isDirectory()) {
					if (canRead) {
						drawableID = R.drawable.folder_icon;
					} else {
						drawableID = R.drawable.folder_icon_light;
					}
				}
				fileList.add(i, new Item(fList[i], drawableID, canRead));
			}// for (int i = 0; i < fList.length; i++) {
			if (fileList.size() == 0) {

				this.directoryShownIsEmpty = true;
				fileList.add(0, new Item("Directory is empty", -1, true));
			} else {// sort non empty list
				Collections.sort(fileList, new ItemFileNameComparator());
			}
		} else {
			Log.e(LOGTAG, "path does not exist or cannot be read");
		}

	}

	private void createFileListAdapter() {
		adapter = new ArrayAdapter<Item>(
                this,
				android.R.layout.select_dialog_item, android.R.id.text1,
				fileList
        ) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {

				// creates view
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);

				// put the image on the text view
				int drawableID = 0;
				if (fileList.get(position).icon != -1) {
					// If icon == -1, then directory is empty
					drawableID = fileList.get(position).icon;
				}
				textView.setCompoundDrawablesWithIntrinsicBounds(drawableID, 0, 0, 0);
				textView.setEllipsize(null);

				// add margin between image and text (support various screen
				// densities)
				// int dp5 = (int) (5 *
				// getResources().getDisplayMetrics().density + 0.5f);
				int dp3 = (int) (3 * getResources().getDisplayMetrics().density + 0.5f);

				// centered
				textView.setCompoundDrawablePadding(dp3);
				textView.setBackgroundColor(Color.LTGRAY);
				return view;
			}
		};
	}

	private class Item {
		public String file;
		public int icon;
		public boolean canRead;

		public Item(String file, Integer icon, boolean canRead) {
			this.file = file;
			this.icon = icon;
		}

		@Override
		public String toString() {
			return file;
		}
	}

	private class ItemFileNameComparator implements Comparator<Item> {
		public int compare(Item lhs, Item rhs) {
			return lhs.file.toLowerCase().compareTo(rhs.file.toLowerCase());
		}
	}

//	public void onConfigurationChanged(Configuration newConfig) {
//		super.onConfigurationChanged(newConfig);
//
//	}

	public static long getFreeSpace(String path) {

        StatFs stat = new StatFs(path);
		return (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();

	}

	public static String formatBytes(long bytes) {

		String retStr = "";
		// One binary gigabyte equals 1,073,741,824 bytes.
		if (bytes > 1073741824) {// Add GB
			long gbs = bytes / 1073741824;
			retStr += (new Long(gbs)).toString() + "GB ";
			bytes = bytes - (gbs * 1073741824);
		}
		// One MB - 1048576 bytes
		if (bytes > 1048576) {// Add GB
			long mbs = bytes / 1048576;
			retStr += (new Long(mbs)).toString() + "MB ";
			bytes = bytes - (mbs * 1048576);
		}
		if (bytes > 1024) {
			long kbs = bytes / 1024;
			retStr += (new Long(kbs)).toString() + "KB";
			bytes = bytes - (kbs * 1024);
		} else
			retStr += (new Long(bytes)).toString() + " bytes";
		return retStr;
	}

}
