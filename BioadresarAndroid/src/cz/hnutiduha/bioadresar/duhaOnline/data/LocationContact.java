/*  This file is part of BioAdresar.
	Copyright 2012 Jiri Prokop, Jiri Zouhar (zouhar@trilobajt.cz)

    BioAdresar is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    BioAdresar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BioAdresar.  If not, see <http://www.gnu.org/licenses/>.
 */

package cz.hnutiduha.bioadresar.duhaOnline.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;

public class LocationContact implements Comparable<LocationContact> {
	public String email, web, eshop, street, city, person, phone, zip;

	public LocationContact(LocationContact origin) {
		if (origin == null)
			return;

		email = origin.email;
		web = origin.web;
		eshop = origin.eshop;
		street = origin.street;
		city = origin.city;
		person = origin.person;
		phone = origin.phone;
		zip = origin.zip;
	}

	public LocationContact() {

	}
	
	private String getStringOrDefault(JSONObject details, String fieldName, String defaultValue)
	{
		try
		{
			return details.getString(fieldName);
		} catch (JSONException ex)
		{
			Log.w("data", "can't get " + fieldName, ex);
			return defaultValue;
		}
	}
	
	private boolean parseExplodedEmail(JSONObject details)
	{
		if (!details.has("emailsExploded")) { return false; }
		
		try
		{
			StringBuilder bldr = new StringBuilder();
			JSONArray emails = details.getJSONArray("emailsExploded");
			for (int i = 0; i < emails.length(); i++)
			{
				JSONObject email = emails.getJSONObject(i);
				bldr.append(((i != 0) ? ", " : "") + email.getString("id") + "@" + email.get("domain") + "." + email.get("tld"));
			}
			this.email = bldr.toString();
			return true;
			
		} catch (JSONException ex)
		{
			Log.e("data", "invalid email format", ex);
		}
		
		return false;
	}
	
	private void parseEmail(JSONObject details)
	{
		if (!parseExplodedEmail(details))
			this.email = getStringOrDefault(details, "email", null);
	}

	public LocationContact(JSONObject details) {
		this.person = getStringOrDefault(details, "person", this.person);
		this.street = getStringOrDefault(details, "street", this.street);
		this.city = getStringOrDefault(details, "city", this.city);
		this.zip = getStringOrDefault(details, "zip", this.zip);
		this.phone = getStringOrDefault(details, "phone", this.phone);
		this.web = getStringOrDefault(details, "web", this.web);
		this.eshop = getStringOrDefault(details, "eshop", this.eshop);
		
		parseEmail(details);
		
		/* unused fields
		this.region = getStringOrDefault(details, "region", this.region);
		this.web2 = getStringOrDefault(details, "web2", this.web2);
		*/
	}

	@Override
	public int compareTo(LocationContact otherContact) {
		int res = person.compareTo(otherContact.person);
		if (res != 0)
			return res;

		res = street.compareTo(otherContact.street);
		if (res != 0)
			return res;

		res = city.compareTo(otherContact.city);
		if (res != 0)
			return res;

		res = zip.compareTo(otherContact.zip);
		if (res != 0)
			return res;

		res = phone.compareTo(otherContact.phone);
		if (res != 0)
			return res;

		res = email.compareTo(otherContact.email);
		if (res != 0)
			return res;

		res = web.compareTo(otherContact.web);
		if (res != 0)
			return res;

		res = eshop.compareTo(otherContact.eshop);
		if (res != 0)
			return res;

		return 0;
	}
	
	private static void appendToBuilder(StringBuilder bldr, String separator, String prefix, String something)
	{
		if (something == null || TextUtils.isEmpty(something))
			return;
		
		if (bldr.length() > 0)
			bldr.append(separator);
		
		bldr.append(prefix);
		bldr.append(something);
	}
	
	public String toString(String separator)
	{
		StringBuilder bldr = new StringBuilder();
		appendToBuilder(bldr, separator, "", person);
		appendToBuilder(bldr, separator, "", street);
		appendToBuilder(bldr, separator, "", city);
		appendToBuilder(bldr, separator, "", zip);
		appendToBuilder(bldr, separator, "mail", email);
		appendToBuilder(bldr, separator, "phone", phone);
		appendToBuilder(bldr, separator, "web", web);
		appendToBuilder(bldr, separator, "eshop", eshop);
		
		return bldr.toString(); 
	}
	
	public String toString()
	{
		return toString(", ");
	}
	public String formatAddress(String separator)
	{
		StringBuilder bldr = new StringBuilder();
		appendToBuilder(bldr, separator, "", person);
		appendToBuilder(bldr, separator, "", street);
		appendToBuilder(bldr, separator, "", city);
		appendToBuilder(bldr, separator, "", zip);
		
		return bldr.toString();
	}

}
