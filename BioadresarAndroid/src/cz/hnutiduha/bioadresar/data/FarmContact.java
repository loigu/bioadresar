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

package cz.hnutiduha.bioadresar.data;

import java.util.LinkedList;

public class FarmContact {
	public LinkedList <String> phoneNumbers;
	public String email, web, eshop, street, city, person;
	
	public FarmContact(FarmContact origin)
	{
		if (origin == null) return;
		
		email = origin.email;
		web = origin.web;
		eshop = origin.eshop;
		street = origin.street;
		city = origin.city;
		person = origin.person;
		
		if (origin.phoneNumbers != null)
			phoneNumbers = (LinkedList<String>) origin.phoneNumbers.clone();
	}

	public FarmContact() {
		
	}
}
