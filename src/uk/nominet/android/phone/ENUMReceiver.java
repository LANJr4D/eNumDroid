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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class ENUMReceiver extends BroadcastReceiver {

	/* Log tag */
	static private final String TAG = "ENUMReceiver";
	
	/* Exported constants */
	static public final String BYPASS_PREFIX = "**";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		Log.d(TAG, "action " + action + " received");

		/* check connectivity and update notification on every received vent */
		boolean online = ENUMUtil.updateNotification(context);
		
		/* and do this if it's an outgoing call */
		if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
			String number = getResultData(); 
			if (number == null) return;
			
			Log.d(TAG, "number = " + number);
			if (online && number.startsWith("+")) {
				setResultData(null);
				
				Intent newIntent = new Intent(context, ENUMList.class);
				newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				newIntent.setData(Uri.fromParts("tel", number, null));
				context.startActivity(newIntent);
			} else {
				if (number.startsWith(BYPASS_PREFIX)) {
					setResultData(number.substring(BYPASS_PREFIX.length()));
				} else {
					setResultData(number);
				}
			}
		}
	}
}
