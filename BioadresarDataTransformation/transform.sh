#!/bin/bash
# FIXME: this is really slooooow

export MYSQL_SCRIPT="$1"
export TARGET_DB="$2"


# dump to mysql
export MYDB_NAME="hnutiduha9650"
export MYDB_USER="duha_importer"
export MYDB_PASS="importer"

function notNULL()
{
	cont=$(echo $* | tr -d ' \t')
	[ ! -z "${cont}" -a "${cont}" != "NULL" ] && return 0
	return 1
}

function importMysql()
{
	cat "${MYSQL_SCRIPT}" | sudo mysql
	echo "GRANT SELECT, DELETE, DROP ON ${MYDB_NAME}.* TO ${MYDB_USER}@localhost IDENTIFIED BY '${MYDB_PASS}'; 
	FLUSH PRIVILEGES;" | sudo mysql
}

function callMysql()
{
	mysql -B -s --user="${MYDB_USER}" --password="${MYDB_PASS}" "${MYDB_NAME}"
}

function removeMysql()
{
	echo "DROP DATABASE ${MYDB_NAME};" | callMysql
}

function callSqlite()
{
	sqlite3 "${TARGET_DB}"
}

function reportError()
{
	echo $* >&2
}

function createBaseLayout()
{
	echo "CREATE TABLE android_metadata (locale TEXT DEFAULT 'cs_CZ');" | callSqlite
	echo "CREATE TABLE config (variable TEXT UNIQUE NOT NULL, value TEXT);" | callSqlite
	echo "INSERT INTO config(variable, value) VALUES ('lastUpdated', '$(date +%s)');" | callSqlite
}

function addCategoriesToProducts()
{
	# NOTE manually created links between categories and products
	zelenina=$(echo "SELECT id FROM produkt WHERE nazev='zelenina'" | callMysql)
	ovoce=$(echo "SELECT id FROM produkt WHERE nazev='ovoce'" | callMysql)
	mleko=$(echo "SELECT id FROM produkt WHERE nazev='mléko a mléčné výrobky'" | callMysql)
	maso=$(echo "SELECT id FROM produkt WHERE nazev='maso'" | callMysql)
	ostatni=$(echo "SELECT id FROM produkt WHERE nazev='ostatní'" | callMysql)
	obili=${ostatni}
	bylinky=${ostatni}
	pecivo=${ostatni}
	lusteniny=${zelenina}
	proFarmare=${ostatni} #ziva zvirata, sadba, setba
	picniny=${ostatni}
	
echo "
UPDATE products SET categoryId=${mleko} WHERE name='kravské mléko';
UPDATE products SET categoryId=${zelenina} WHERE name='mrkev';
UPDATE products SET categoryId=${proFarmare} WHERE name='jehňata';
UPDATE products SET categoryId=${ostatni} WHERE name='vlna';
UPDATE products SET categoryId=${zelenina} WHERE name='cibule';
UPDATE products SET categoryId=${zelenina} WHERE name='dýně Hokaido';
UPDATE products SET categoryId=${zelenina} WHERE name='pastinák';
UPDATE products SET categoryId=${zelenina} WHERE name='česnek';
UPDATE products SET categoryId=${zelenina} WHERE name='brambory';
UPDATE products SET categoryId=${lusteniny} WHERE name='luskoviny';
UPDATE products SET categoryId=${bylinky} WHERE name='byliny';
UPDATE products SET categoryId=${obili} WHERE name='obiloviny';
UPDATE products SET categoryId=${zelenina} WHERE name='okurky';
UPDATE products SET categoryId=${ovoce} WHERE name='jablka';
UPDATE products SET categoryId=${ovoce} WHERE name='hrušky';
UPDATE products SET categoryId=${ovoce} WHERE name='třešně';
UPDATE products SET categoryId=${obili} WHERE name='pšenice';
UPDATE products SET categoryId=${obili} WHERE name='ječmen';
UPDATE products SET categoryId=${lusteniny} WHERE name='tritikale';
UPDATE products SET categoryId=${lusteniny} WHERE name='luštěniny';
UPDATE products SET categoryId=${lusteniny} WHERE name='sója';
UPDATE products SET categoryId=${ostatni} WHERE name='hořčice';
UPDATE products SET categoryId=${ovoce} WHERE name='víno';
UPDATE products SET categoryId=${lusteniny} WHERE name='fazole';
UPDATE products SET categoryId=${lusteniny} WHERE name='cizrna';
UPDATE products SET categoryId=${bylinky} WHERE name='fenykl';
UPDATE products SET categoryId=${ovoce} WHERE name='meruňky';
UPDATE products SET categoryId=${zelenina} WHERE name='kukuřice';
UPDATE products SET categoryId=${lusteniny} WHERE name='hrášek';
UPDATE products SET categoryId=${zelenina} WHERE name='tykve';
UPDATE products SET categoryId=${obili} WHERE name='žito';
UPDATE products SET categoryId=${picniny} WHERE name='pícniny';
UPDATE products SET categoryId=${obili} WHERE name='slunečnice';
UPDATE products SET categoryId=${obili} WHERE name='oves';
UPDATE products SET categoryId=${obili} WHERE name='proso';
UPDATE products SET categoryId=${obili} WHERE name='pohanka';
UPDATE products SET categoryId=${mleko} WHERE name='ovčí sýr';
UPDATE products SET categoryId=${mleko} WHERE name='bryndza';
UPDATE products SET categoryId=${ostatni} WHERE name='med a včelí produkty';
UPDATE products SET categoryId=${ovoce} WHERE name='jahody';
UPDATE products SET categoryId=${proFarmare} WHERE name='semínka';
UPDATE products SET categoryId=${obili} WHERE name='mouka';
UPDATE products SET categoryId=${ostatni} WHERE name='olej';
UPDATE products SET categoryId=${ostatni} WHERE name='mošty';
UPDATE products SET categoryId=${bylinky} WHERE name='koření';
UPDATE products SET categoryId=${ostatni} WHERE name='drogerie';
UPDATE products SET categoryId=${ostatni} WHERE name='vejce';
UPDATE products SET categoryId=${proFarmare} WHERE name='sazenice';
UPDATE products SET categoryId=${ostatni} WHERE name='ořechy';
UPDATE products SET categoryId=${mleko} WHERE name='kozí sýry';
UPDATE products SET categoryId=${pecivo} WHERE name='chléb';
UPDATE products SET categoryId=${maso} WHERE name='drůbeží maso';
UPDATE products SET categoryId=${ostatni} WHERE name='farma Trpola';
UPDATE products SET categoryId=${zelenina} WHERE name='celer';
UPDATE products SET categoryId=${zelenina} WHERE name='rajčata';
UPDATE products SET categoryId=${ovoce} WHERE name='angrešt';
UPDATE products SET categoryId=${ovoce} WHERE name='rybíz';
UPDATE products SET categoryId=${ovoce} WHERE name='švestky';
UPDATE products SET categoryId=${ovoce} WHERE name='švestky test';
UPDATE products SET categoryId=${ovoce} WHERE name='maliny';
UPDATE products SET categoryId=${ovoce} WHERE name='borůvky';
UPDATE products SET categoryId=${ostatni} WHERE name='kůže';
UPDATE products SET categoryId=${zelenina} WHERE name='zelí';
UPDATE products SET categoryId=${zelenina} WHERE name='kedlubna gigant';
UPDATE products SET categoryId=${zelenina} WHERE name='červená řepa';
UPDATE products SET categoryId=${zelenina} WHERE name='petržel';
UPDATE products SET categoryId=${zelenina} WHERE name='pórek';
UPDATE products SET categoryId=${zelenina} WHERE name='kapusta';
UPDATE products SET categoryId=${ovoce} WHERE name='broskve';
UPDATE products SET categoryId=${zelenina} WHERE name='křen';
UPDATE products SET categoryId=${maso} WHERE name='jehněčí maso';
UPDATE products SET categoryId=${proFarmare} WHERE name='seno';
UPDATE products SET categoryId=${proFarmare} WHERE name='krmiva';
UPDATE products SET categoryId=${maso} WHERE name='masné výrobky';
UPDATE products SET categoryId=${maso} WHERE name='skopové';
UPDATE products SET categoryId=${maso} WHERE name='kůzlečí maso';
UPDATE products SET categoryId=${obili} WHERE name='špalda';
UPDATE products SET categoryId=${maso} WHERE name='skopové';
UPDATE products SET categoryId=${ostatni} WHERE name='kožešina';
UPDATE products SET categoryId=${zelenina} WHERE name='paprika';
UPDATE products SET categoryId=${zelenina} WHERE name='cuketa';
UPDATE products SET categoryId=${bylinky} WHERE name='majoránka';
UPDATE products SET categoryId=${zelenina} WHERE name='kopr';
UPDATE products SET categoryId=${ostatni} WHERE name='test';
UPDATE products SET categoryId=${zelenina} WHERE name='ředkvičky';
UPDATE products SET categoryId=${zelenina} WHERE name='dýně';
UPDATE products SET categoryId=${zelenina} WHERE name='salát';
UPDATE products SET categoryId=${mleko} WHERE name='kozí mléko';
UPDATE products SET categoryId=${mleko} WHERE name='kozí jogurt';
UPDATE products SET categoryId=${mleko} WHERE name='kozí tvaroh';
UPDATE products SET categoryId=${maso} WHERE name='hovězí maso';
UPDATE products SET categoryId=${maso} WHERE name='vepřové maso';
UPDATE products SET categoryId=${zelenina} WHERE name='velký výběr zeleniny';
UPDATE products SET categoryId=${maso} WHERE name='králičí maso';
UPDATE products SET categoryId=${maso} WHERE name='skopové maso';
UPDATE products SET categoryId=${ostatni} WHERE name='konopné produkty';
UPDATE products SET categoryId=${mleko} WHERE name='sýr';
UPDATE products SET categoryId=${mleko} WHERE name='tvaroh';
UPDATE products SET categoryId=${mleko} WHERE name='máslo';
UPDATE products SET categoryId=${mleko} WHERE name='podmáslí';
UPDATE products SET categoryId=${mleko} WHERE name='syrovátka';
UPDATE products SET categoryId=${maso} WHERE name='husy';
UPDATE products SET categoryId=${ovoce} WHERE name='ostružiny';
UPDATE products SET categoryId=${bylinky} WHERE name='třezalka';
UPDATE products SET categoryId=${maso} WHERE name='křepelčí maso';
UPDATE products SET categoryId=${lusteniny} WHERE name='daikon';
UPDATE products SET categoryId=${zelenina} WHERE name='brokolice';
UPDATE products SET categoryId=${ovoce} WHERE name='hroznové víno';
UPDATE products SET categoryId=${ovoce} WHERE name='višně';
UPDATE products SET categoryId=${ostatni} WHERE name='džem';
UPDATE products SET categoryId=${ovoce} WHERE name='sušené ovoce';
UPDATE products SET categoryId=${ostatni} WHERE name='pálenka';
UPDATE products SET categoryId=${maso} WHERE name='telecí maso';
UPDATE products SET categoryId=${proFarmare} WHERE name='skot';
UPDATE products SET categoryId=${pecivo} WHERE name='bezlepkové pečivo';
UPDATE products SET categoryId=${zelenina} WHERE name='chřest';
UPDATE products SET categoryId=${zelenina} WHERE name='rajčata';
UPDATE products SET categoryId=${zelenina} WHERE name='tuřín';
UPDATE products SET categoryId=${proFarmare} WHERE name='senáž';
UPDATE products SET categoryId=${zelenina} WHERE name='peluška';
UPDATE products SET categoryId=${proFarmare} WHERE name='telata';
UPDATE products SET categoryId=${mleko} WHERE name='podmaslí';
UPDATE products SET categoryId=${mleko} WHERE name='kefír';
UPDATE products SET categoryId=${pecivo} WHERE name='pečivo';
UPDATE products SET categoryId=${zelenina} WHERE name='pekingské zelí';
UPDATE products SET categoryId=${ovoce} WHERE name='blumy';
UPDATE products SET categoryId=${bylinky} WHERE name='kořenící směsi';
UPDATE products SET categoryId=${ostatni} WHERE name='bylinné koupele';
UPDATE products SET categoryId=${ostatni} WHERE name='biovíno';
UPDATE products SET categoryId=${ostatni} WHERE name='koberce';
UPDATE products SET categoryId=${ovoce} WHERE name='meloun';
UPDATE products SET categoryId=${proFarmare} WHERE name='kůzlata';
UPDATE products SET categoryId=${ostatni} WHERE name='výrobky z vlny';
UPDATE products SET categoryId=${ostatni} WHERE name='sirupy';
UPDATE products SET categoryId=${mleko} WHERE name='žinčica';
UPDATE products SET categoryId=${zelenina} WHERE name='květák';
UPDATE products SET categoryId=${zelenina} WHERE name='pažitka';
UPDATE products SET categoryId=${zelenina} WHERE name='vodnice';
UPDATE products SET categoryId=${zelenina} WHERE name='mangold';
UPDATE products SET categoryId=${ovoce} WHERE name='trnky';
UPDATE products SET categoryId=${bylinky} WHERE name='kmín';
UPDATE products SET categoryId=${bylinky} WHERE name='koriandr';
UPDATE products SET categoryId=${ostatni} WHERE name='povidla';
UPDATE products SET categoryId=${bylinky} WHERE name='bylinné čaje';
UPDATE products SET categoryId=${ovoce} WHERE name='ovce';
UPDATE products SET categoryId=${ovoce} WHERE name='ovocné šťávy';
UPDATE products SET categoryId=${proFarmare} WHERE name='hnojiva';
UPDATE products SET categoryId=${proFarmare} WHERE name='muchovníky';
UPDATE products SET categoryId=${zelenina} WHERE name='topinambury';
UPDATE products SET categoryId=${zelenina} WHERE name='rebarbora';
UPDATE products SET categoryId=${ovoce} WHERE name='bez černý';
UPDATE products SET categoryId=${ostatni} WHERE name='med';
UPDATE products SET categoryId=${bylinky} WHERE name='estragon';
UPDATE products SET categoryId=${bylinky} WHERE name='tymián';
UPDATE products SET categoryId=${ostatni} WHERE name='ryby';
UPDATE products SET categoryId=${ostatni} WHERE name='sterilované výrobky';
" | callSqlite
}


function addProducts()
{
	#create categories
	echo 'CREATE TABLE categories (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	
	#create products
	echo 'CREATE TABLE products (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, categoryId INTEGER);' | sqlite3 "${TARGET_DB}"
	
	echo 'SELECT id,je_kategorie,nazev FROM produkt;' | callMysql \
		| sed -e 's/\t/\;/g' | while IFS=';' read id kategorie nazev; do 
			if [ "${kategorie}" = "ano" ]; then
				echo "insert into categories(_id,name) values(${id},'${nazev}');" | callSqlite
				[ "$?" != 0 ] && reportError "inserting category ${nazev} with id ${id} failed"
			else
				echo "insert into products(_id,name) values(${id},'${nazev}');" | callSqlite
				[ "$?" != 0 ] && reportError "inserting product ${nazev} with id ${id} failed"
			fi
		done

	addCategoriesToProducts
}


function addLocations()
{
	# add location types
	echo 'CREATE TABLE locationTypes (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);' | callSqlite
	[ "$?" != 0 ] && reportError "failed to create table locationTypes"
	echo "select distinct(typ) from divize;" | callMysql | while read type; do 
		echo "insert into locationTypes(name) values('${type}');" | callSqlite; 
		[ "$?" != 0 ] && reportError "failed add location type ${type}"
	done
	
	# create locations
	echo 'CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, gpsLatitude REAL NOT NULL, gpsLongtitude REAL NOT NULL, description TEXT, typeId INTEGER);' | callSqlite
	[ "$?" != 0 ] && reportError "failed to create table locations"
	
	# fill locations
	echo "select divize.id, divize.typ, kontakt.latitude, kontakt.longtitude, producent.nazev, divize.nazevdivize, divize.poznamka from producent, divize, kontakt where producent.id = divize.producent_id and  kontakt.divize_id = divize.id ;" \
		| callMysql \
		| sed -e 's/\t/\;/g' \
		| while IFS=';' read locationId typ lat lon name divizionName comment; do 
			notNULL "${divizionName}" && name="${name} (${divizionName})"
			typeId=$(echo "select _id from locationTypes WHERE name='${typ}';" | callSqlite)
			[ "$?" != 0 ] && echo "failed to find out typeId for type ${type}"
			echo "INSERT INTO locations(_id, name, gpsLatitude, gpsLongtitude, description, typeId) VALUES(${locationId}, '${name}', ${lat}, ${lon}, '${comment}', ${typeId});" | callSqlite
			[ "$?" != 0 ] && reportError "failed to insert location ${name}"
		done
		
}



function addContacts()
{
	# add contact types. NOTE: api 
	echo 'CREATE TABLE contactTypes (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);' | callSqlite
	CITY=1
	STREET=2
	EMAIL=3
	WEB=4
	ESHOP=5
	PHONE=6
	echo "INSERT INTO contactTypes(_id, name) VALUES (${CITY}, 'city'), (${STREET}, 'street'), (${EMAIL}, 'email'), (${WEB}, 'web'), (${ESHOP}, 'eshop'), (${PHONE}, 'phone');" | callSqlite

	# FIXME: what about two divizions with one producent?
	# create table contacts
	echo 'CREATE TABLE contacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER, type INTEGER, contact TEXT, UNIQUE (locationId, type, contact));' | callSqlite

	# fill contacts
	echo "SELECT divize.id, kontakt.mobil, kontakt.email, kontakt.web, kontakt.web2, kontakt.web_eshop, kontakt.ulice, kontakt.mesto FROM producent, divize, kontakt WHERE producent.id = divize.producent_id and  kontakt.divize_id = divize.id ;" \
		| callMysql \
		| sed -e 's/\t/\;/g' \
		| while IFS=';' read locationId phone email web web2 eshop street city; do 
			notNULL "${city}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${CITY}, '${city}');" | callSqlite
			notNULL "${street}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${STREET}, '${street}');" | callSqlite
			notNULL "${email}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${EMAIL}, '${email}');" | callSqlite
			notNULL "${phone}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${PHONE}, '${phone}');" | callSqlite
			notNULL "${eshop}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${ESHOP}, '${eshop}');" | callSqlite
			
			if notNULL "${web}" && notNULL "${web2}"; then
				echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${WEB}, '${web} ${web2}');" | callSqlite
			else
				notNULL "${web}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${WEB}, '${web}');" | callSqlite
				notNULL "${web2}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${WEB}, '${web2}');" | callSqlite
			fi
		done
}


function addProductsToLocations()
{
	#create farm:product table
	echo 'CREATE TABLE location_product (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER, productId INTEGER, comment TEXT, UNIQUE (locationId, productId));' | callSqlite
	
	#create farm:category table (can be computed from farm:product, but this is faster...
	echo 'CREATE TABLE location_category (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER, categoryId INTEGER, UNIQUE (locationId, categoryId));' | callSqlite
	
	echo "SELECT _id FROM categories;" | callSqlite | while read id; do
		echo "DELETE FROM produkuje where produkt_id=${id};" | callMysql
	done
	
	echo 'SELECT divize_id, produkt_id, poznamka FROM produkuje;' | callMysql \
		| while read locationId productId note; do
			echo "INSERT INTO location_product(locationId, productId, comment) VALUES(${locationId}, ${productId}, '${note}');" | callSqlite
			
			# TODO: this could be done by distinct in one call, not repeating million times
			categoryId=$(echo "select categoryId from products where _id=${productId};" | callSqlite)
			notNULL "${categoryId}" && echo "INSERT INTO location_category(locationId, categoryId) VALUES(${locationId}, ${categoryId});" | callSqlite &>/dev/null
		done
}

#cinnost -> TODO, zatim ignorujeme
#dela -> TODO: match mezi divizema a cinnostmy (s poznamkou), zatim ignorujeme

importMysql

createBaseLayout
addProducts
addLocations
addContacts
addProductsToLocations

removeMysql

#kraje -> ignorovat (souradnice na stredy kraju)
#kraje_old -> ignorovat (zkratky pro kraje)
#devel -> ignorovat
#divize_all, divize_viditelne -> nejake helpery pro web, ignorovat

