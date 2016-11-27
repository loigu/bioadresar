#!/bin/bash

TARGET_DB="$1"

function log()
{
	echo "$(date): $*"
}

function callSqlite()
{
	tee -a log | sqlite3 "${TARGET_DB}"
}

function createBaseLayout()
{
	log "creating base layout"
	echo "CREATE TABLE android_metadata (locale TEXT DEFAULT 'cs_CZ');" | callSqlite
	echo "CREATE TABLE config (variable TEXT UNIQUE NOT NULL, value TEXT);" | callSqlite
	echo "INSERT INTO config(variable, value) VALUES ('lastUpdated', '0');" | callSqlite

	echo 'CREATE TABLE products (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	echo 'CREATE TABLE activities (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	
	
	echo 'CREATE TABLE locationTypes (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	# NOTE: coex doesn't send lastChange through api, we store our last change instead
	echo 'CREATE TABLE locations (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, gpsLatitude REAL NOT NULL, gpsLongitude REAL NOT NULL, description TEXT, typeId INTEGER NOT NULL, lastChange INTEGER NOT NULL);' | callSqlite

	echo 'CREATE TABLE contacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER UNIQUE, person TEXT, street TEXT, city TEXT, zip TEXT, phone TEXT, email TEXT, web TEXT, eshop TEXT);' | callSqlite

	echo 'CREATE TABLE location_product (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER NOT NULL, productId INTEGER NOT NULL, comment TEXT, mainProduct INTEGER NOT NULL, UNIQUE (locationId, productId));' | callSqlite
		echo 'CREATE TABLE location_activity (_id INTEGER PRIMARY KEY AUTOINCREMENT, locationId INTEGER NOT NULL, activityId INTEGER NOT NULL, comment TEXT, mainActivity INTEGER NOT NULL, UNIQUE (locationId, activityId));' | callSqlite
	
}

function buildFtsTable()
{
	log "create fts"
	
	echo 'CREATE VIRTUAL TABLE locations_fts USING fts3(
		_id INTEGER,
		name STRING,
		description STRING,
		typeName STRING,
		activities STRING,
		products STRING,
		contacts STRING,
		other STRING,
		UNIQUE(_id)
	);' | callSqlite
}

if [ -z "${TARGET_DB}" ]; then
	echo "usage $0 <targetDb>" >&2
	exit 1
fi

createBaseLayout
buildFtsTable

echo "INSERT into config('variable', 'value') values('databaseVersion', '6');" | callSqlite

exit 0

