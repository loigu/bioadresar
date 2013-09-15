#!/bin/bash

ORIGINAL=$1
NEW=$2

cp ${ORIGINAL} ${NEW}

echo 'drop table locations;' | sqlite3 ${NEW}
echo 'CREATE VIRTUAL TABLE locations USING fts3(_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, gpsLatitude REAL NOT NULL, gpsLongtitude REAL NOT NULL, description TEXT, typeId INTEGER);' | sqlite3 ${NEW}

