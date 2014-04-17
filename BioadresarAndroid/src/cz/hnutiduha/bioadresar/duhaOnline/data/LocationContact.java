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

public class LocationContact {
	public String email, web, eshop, street, city, person, phone, zip;
	
	public LocationContact(LocationContact origin)
	{
		if (origin == null) return;
		
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
	
	public LocationContact(JSONObject details) throws JSONException
	{
		this.phone = details.getString("phone");
		this.web = details.getString("web");
		this.eshop = details.getString("eshop");
		this.person = details.getString("person");
		this.email = details.getString("email");
		this.street = details.getString("street");
		this.city = details.getString("city");
		this.zip = details.getString("zip");
	}
}
