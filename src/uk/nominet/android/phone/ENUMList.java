/*
 * Copyright 2009 Nominet UK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.nominet.android.phone;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class ENUMList extends ListActivity implements ViewBinder, OnMenuItemClickListener {

	/* Log tag */
	static private final String TAG = "ENUMList";
	
	/* Private constants */
	static private final String[] COLUMN_NAMES = new String[] { "uri" };
	static private final int[] VIEW_NAMES = new int[] { R.id.text1 };

	static private final String URI_TEL		= "tel";
	
	static private final String TYPE_EMAIL	= "email";
	static private final String TYPE_LOC	= "loc";
	static private final String TYPE_PSTN	= "pstn";
	static private final String TYPE_SIP	= "sip";
	static private final String TYPE_SMS	= "sms";
	static private final String TYPE_TEL    = "tel";
	static private final String TYPE_VOICE	= "voice";
	static private final String TYPE_WEB	= "web";
	static private final String TYPE_XMPP	= "xmpp";
	
	/* Member variables */
	private Cursor mCursor = null;
	private String mNumber = null;

	private void dialOriginalNumber() {
		try {
			Uri uri = Uri.fromParts(URI_TEL, ENUMReceiver.BYPASS_PREFIX + mNumber, null);
			Intent intent = new Intent(Intent.ACTION_CALL, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		Toast toast = Toast.makeText(this, R.string.toast_progress, Toast.LENGTH_SHORT);
		toast.show();

		/* ask the ENUM ContentProvider for the records */
		mNumber = getIntent().getData().getSchemeSpecificPart();
		Uri uri = Uri.withAppendedPath(ENUMProvider.CONTENT_URI, mNumber);
		mCursor = managedQuery(uri, null, null, null, null);
		
		/* none found - tell the user then dial the original number */
		if (mCursor == null || mCursor.getCount() <= 0) {
			toast.setText(R.string.toast_notfound);
			toast.show();
			dialOriginalNumber();
			return;
		}

		/* show the list of records */
		toast.cancel();
		setContentView(R.layout.property_list);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.property_row, mCursor, COLUMN_NAMES, VIEW_NAMES);
		adapter.setViewBinder(this);
		setListAdapter(adapter);
	}

	public boolean setViewValue(View view, Cursor cursor, int columnindex) {
		TextView v = (TextView) view;
		String[] services = cursor.getString(1).split(":");
		String type = services[0];

		/* set icon and strip URI prefix for known URI types */
		int rid = android.R.drawable.ic_menu_help;
		if (TYPE_PSTN.equals(type) || TYPE_SIP.equals(type)
				|| TYPE_VOICE.equals(type) || TYPE_TEL.equals(type)) {
			rid = android.R.drawable.ic_menu_call;
		} else if (TYPE_WEB.equals(type)) {
			rid = R.drawable.ic_menu_home;
		} else if (TYPE_LOC.equals(type)) {
			rid = android.R.drawable.ic_menu_compass;
		} else if (TYPE_EMAIL.equals(type) || TYPE_SMS.equals(type)) {
			rid = android.R.drawable.ic_menu_send;
		} else if (TYPE_XMPP.equals(type)) {
			rid = R.drawable.ic_menu_cc;
		}
		v.setGravity(Gravity.CENTER_VERTICAL);
		v.setCompoundDrawablesWithIntrinsicBounds(rid, 0, 0, 0);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView,
	 * android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		mCursor.moveToPosition(position);

		String services[] = mCursor.getString(1).split(":");
		String type = services[0];
		
		Uri uri = Uri.parse(mCursor.getString(2));
		String action = Intent.ACTION_DEFAULT;

		// Use non-default action for some URI schemes
		if (TYPE_PSTN.equals(type) || TYPE_SIP.equals(type) || TYPE_VOICE.equals(type) ||
				TYPE_TEL.equals(type)) {
			action = Intent.ACTION_CALL;
		} else if (TYPE_SMS.equals(type)) {
			action = Intent.ACTION_SENDTO;
		}

		// fudge tel: URIs to prevent looping
		if (URI_TEL.equals(uri.getScheme())) {
			uri = Uri.fromParts(uri.getScheme(), ENUMReceiver.BYPASS_PREFIX
					+ uri.getSchemeSpecificPart(), uri.getFragment());
		}

		// try and start the right intent, and warn if one wasn't found
		try {
			Intent intent = new Intent(action, uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Log.v(TAG, "starting intent " + uri + " with action " + action);
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.toast_notsupported, Toast.LENGTH_LONG).show();
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem m = menu.add(R.string.menu_text_bypass);
		m.setIcon(android.R.drawable.ic_menu_call);
		m.setOnMenuItemClickListener(this);
		return true;
	}
	
	public boolean onMenuItemClick(MenuItem item) {
		dialOriginalNumber();
		return true;
	}
}
