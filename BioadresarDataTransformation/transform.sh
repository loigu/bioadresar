#!/bin/bash
# FIXME: this is really slooooow

export MYSQL_SCRIPT="$1"
export TARGET_DB="$2"
export EXPORT_DATE=$(date --date $(echo ${MYSQL_SCRIPT} | sed -e 's/.*-\([^.]*\).*/\1/') +%s)


# dump to mysql
export MYDB_NAME="hnutiduha9650"
export MYDB_USER="duha_importer"
export MYDB_PASS="importer"

function notNULL()
{
	cont=$(echo $* | tr -d ' \t')
	[ ! -z "${cont}" -a "${cont}" != "NULL" -a "${cont}" != "null" ] && return 0
	return 1
}

function emptyToNull()
{
	if notNULL $*; then
		echo "'$*'"
	else
		echo "null"
	fi
}

function log()
{
	echo "$(date): $*"
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
	log "creating base layout"
	echo "CREATE TABLE android_metadata (locale TEXT DEFAULT 'cs_CZ');" | callSqlite
	echo "CREATE TABLE config (variable TEXT UNIQUE NOT NULL, value TEXT);" | callSqlite
	echo "INSERT INTO config(variable, value) VALUES ('lastUpdated', '${EXPORT_DATE}');" | callSqlite
}

function addCategoriesToProducts()
{
	log "adding categories to products"
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
	log "adding products"
	#create categories
	echo 'CREATE TABLE categories (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	
	#create products
	echo 'CREATE TABLE products (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, categoryId INTEGER);' | sqlite3 "${TARGET_DB}"
	
	echo 'SELECT id,je_kategorie,nazev FROM produkt;' | callMysql \
		| sed -e 's/\t/\;/g' | while IFS=';' read id kategorie nazev; do 
			if [ "${kategorie}" = "ano" ]; then
				echo "insert into categories(_id,name) values(${id},'${nazev}');"
				# | callSqlite
				# [ "$?" != 0 ] && reportError "inserting category ${nazev} with id ${id} failed"
			else
				echo "insert into products(_id,name) values(${id},'${nazev}');"
				# | callSqlite
				# [ "$?" != 0 ] && reportError "inserting product ${nazev} with id ${id} failed"
			fi
		done | callSqlite

	addCategoriesToProducts
}

function addActivities()
{
	log "adding activities"
	#create activities
	echo 'CREATE TABLE activities (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	
	echo 'SELECT id,nazev FROM cinnost;' | callMysql \
		| while read id nazev; do 
			echo "insert into activities(_id,name) values(${id},'${nazev}');"
			# | callSqlite
			# [ "$?" != 0 ] && reportError "inserting product ${nazev} with id ${id} failed"
		done | callSqlite
}


function addLocations()
{
	log "adding locations"
	# add location types
	echo 'CREATE TABLE locationTypes (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT);' | callSqlite
	[ "$?" != 0 ] && reportError "failed to create table locationTypes"
	local id=0
	local tt=$(mktemp ./locType.XXXXXX)
	echo "select distinct(typ) from divize;" | callMysql | while read type; do 
		echo "insert into locationTypes(_id, name) values(${id}, '${type}');"
		echo "export typ_${type}=${id}" >> ${tt}
		id=$(expr ${id} + 1)
		# | callSqlite; 
		# [ "$?" != 0 ] && reportError "failed add location type ${type}"
	done | callSqlite
	source ${tt}
	rm ${tt}
	
	# create locations
	echo 'CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, gpsLatitude REAL NOT NULL, gpsLongtitude REAL NOT NULL, description TEXT, typeId INTEGER);' | callSqlite
	[ "$?" != 0 ] && reportError "failed to create table locations"
	
	# fill locations
	echo "select divize.id, divize.typ, kontakt.latitude, kontakt.longtitude, producent.nazev, divize.nazevdivize, divize.poznamka from producent, divize, kontakt where producent.id = divize.producent_id and  kontakt.divize_id = divize.id ;" \
		| callMysql \
		| sed -e 's/\t/\;/g' \
		| while IFS=';' read locationId typ lat lon name divizionName comment; do 
			notNULL "${divizionName}" && name="${name} (${divizionName})"
			typeId=$(eval echo \$\{typ_${typ}\})
			echo "INSERT INTO locations(_id, name, gpsLatitude, gpsLongtitude, description, typeId) VALUES(${locationId}, '${name}', ${lat}, ${lon}, $(emptyToNull ${comment}), ${typeId});"
			# | callSqlite
			# [ "$?" != 0 ] && reportError "failed to insert location ${name}"
		done | callSqlite
}



function addContacts()
{
	log "adding contacts"
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
			notNULL "${city}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${CITY}, '${city}');"
			notNULL "${street}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${STREET}, '${street}');"
			notNULL "${email}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${EMAIL}, '${email}');"
			notNULL "${phone}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${PHONE}, '${phone}');"
			notNULL "${eshop}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${ESHOP}, '${eshop}');"
			
			if notNULL "${web}" && notNULL "${web2}"; then
				echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${WEB}, '${web} ${web2}');"
			else
				notNULL "${web}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${WEB}, '${web}');"
				notNULL "${web2}" && echo "INSERT INTO contacts(locationId, type, contact) VALUES (${locationId}, ${WEB}, '${web2}');"
			fi
		done | callSqlite
}

function addProductsToLocations()
{
	log "adding products to locations"
	#create farm:product table
	echo 'CREATE TABLE location_product (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER NOT NULL, productId INTEGER NOT NULL, comment TEXT, UNIQUE (locationId, productId));' | callSqlite
	
	echo "SELECT _id FROM categories;" | callSqlite | while read id; do
		echo "DELETE FROM produkuje where produkt_id=${id};" | callMysql
	done
	
	echo 'SELECT divize_id, produkt_id, poznamka FROM produkuje;' | callMysql \
		| while read locationId productId note; do
			echo "INSERT INTO location_product(locationId, productId, comment) VALUES(${locationId}, ${productId}, $(emptyToNull ${note}));"
		done | callSqlite
}

function addCategoriesToLocations()
{
	#helper table, not really needed
	log "adding categories to locations"
	#create farm:category table (can be computed from farm:product, but this is faster...
	echo 'CREATE TABLE location_category (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER NOT NULL, categoryId INTEGER NOT NULL, UNIQUE (locationId, categoryId));' | callSqlite

	# sqlite is fucked up, we can't read and write at the same time
	local tt=$(mktemp locat.XXXXXX)
	echo "select distinct products.categoryId, location_product.locationId from products, location_product where products._id = location_product.productId;" | callSqlite \
		| while IFS="|" read categoryId locationId; do
			notNULL "${categoryId}" && echo "INSERT INTO location_category(locationId, categoryId) VALUES(${locationId}, ${categoryId});" >> ${tt}
		done
		
		cat ${tt} | callSqlite
		rm ${tt}
}

function addActivitiesToLocations()
{
	log "adding activities to locations"
	#create farm:activity table
	echo 'CREATE TABLE location_activity (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER NOT NULL, activityId INTEGER NOT NULL, comment TEXT, UNIQUE (locationId, activityId));' | callSqlite

	echo 'SELECT divize_id, cinnost_id, poznamka FROM dela;' | callMysql \
		| while read locationId activityId note; do
			echo "INSERT INTO location_activity(locationId, activityId, comment) VALUES(${locationId}, ${activityId}, $(emptyToNull ${note}));"
		done | callSqlite
}

function deleteLocation()
{
	log "deleting location $1"
	for tab in contacts location_product location_category location_activity; do
		echo "delete from ${tab} where locationId = $1;" | callSqlite
	done
	echo "delete from locations where _id = $1;" | callSqlite
}

function deleteProduct()
{
	log "deleting product $1"
	echo "delete from products where _id=$1;" | callSqlite
	echo "delete from location_product where productId=$1;" | callSqlite
}

function joinProducts()
{
	log "changing product $2 to $1"
	echo "update location_product set productId=$1 where productId=$2;" | callSqlite
	deleteProduct $2
}

function joinFarms()
{
	log "moving production from farm $1 to farm $2"
	for tab in product category activity; do
		echo "update location_${tab} set locationId = $2 where locationId = $1;" | callSqlite
	done
}

function addRegions()
{
	echo 'CREATE TABLE regions (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, zoom INTEGER NOT NULL, gpsLatitude REAL NOT NULL, gpsLongtitude REAL NOT NULL);' | callSqlite
	echo 'SELECT zoom, souradnice, nazev FROM kraje;' | callMysql | while read zoom coordinates name; do
		lat=$(echo ${coordinates} | cut -d ',' -f 1)
		lon=$(echo ${coordinates} | cut -d ',' -f 2)
		echo "INSERT INTO regions(name, zoom, gpsLatitude, gpsLongtitude) VALUES('${name}', ${zoom}, ${lat}, ${lon});"
	done | callSqlite
}

function fixtures_v4()
{
	joinFarms 601 639
	joinFarms 494 631

	for location in 543 542 601 494; do
		deleteLocation ${location}
	done
}

function fixtures_v5()
{
	# found in products
	# (60, 'farma Trpola', 'ne', 'ano'),
	# (66, 'švestky test', 'ne', 'ano'),
	# (96, 'test', 'ne', 'ano'),
	for i in 60 66 96; do
		deleteProduct $i
	done

	# (12, 'luskoviny', '', 'ano'),
	# (22, 'luštěniny', '', 'ano'),
	joinProducts 22 12

	echo "update contacts set contact='www.ekofarma-arnika.estranky.cz' where contact='www,ekofarma-arnika.estranky.cz';" | callSqlite

	# (85, 'skopové', 'ne', 'ano'),
	# (89, 'skopové', 'ne', 'ano'),
	joinProducts 85 89

	# (62, 'rajčata', 'ne', 'ano'),
	# (129, 'rajčata', 'ne', 'ano'),
	joinProducts 62 129
	
	# TODO:
	# (105, 'velký výběr zeleniny', 'ne', 'ano'),
	# ^ neni to totez co kategorie 'zelenina'?
	# (84, 'masné výrobky', 'ne', 'ano'),
	# ^ neduplikuji mirne kategorii maso?
}

function addBookmarkColumn()
{
	echo 'ALTER TABLE locations ADD COLUMN bookmark INTEGER NOT NULL DEFAULT 0;' | callSqlite
}

function call()
{
	importMysql

	createBaseLayout
	addProducts
	addActivities
	addLocations
	addContacts
	addProductsToLocations
	addCategoriesToLocations
	addActivitiesToLocations

	removeMysql

	fixtures_v4
	fixtures_v5
	
	addRegions # version 7
	addBookmarkColumn # version 8
}

call

#devel -> ignorovat
#divize_all, divize_viditelne -> nejake helpery pro web, ignorovat

