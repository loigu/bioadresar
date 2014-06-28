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

import org.json.JSONException;
import org.json.JSONObject;

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

	public LocationContact(JSONObject details) throws JSONException {
		this.person = details.getString("person");
		this.street = details.getString("street");
		this.city = details.getString("city");
		this.zip = details.getString("zip");
		this.phone = details.getString("phone");
		this.email = details.getString("email");
		this.web = details.getString("web");
		this.eshop = details.getString("eshop");
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
	
	private static void appendToBuilder(StringBuilder bldr, String prefix, String something)
	{
		if (something == null || something.isEmpty())
			return;
		
		if (bldr.length() > 0)
			bldr.append(", ");
		
		bldr.append(prefix);
		bldr.append(something);
	}
	
	public String toString()
	{
		StringBuilder bldr = new StringBuilder();
		appendToBuilder(bldr, "", person);
		appendToBuilder(bldr, "", street);
		appendToBuilder(bldr, "", city);
		appendToBuilder(bldr, "phone", phone);
		appendToBuilder(bldr, "web", web);
		appendToBuilder(bldr, "eshop", eshop);
		
		return bldr.toString(); 
	}

}