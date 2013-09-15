#!/bin/bash

TARGET_DB=$1

function callSqlite()
{
	tee -a log | sqlite3 "${TARGET_DB}"
}


echo 'ALTER TABLE Locations ADD COLUMN searchKeywords TEXT;' | callSqlite

echo 'SELECT _id FROM locations;' | callSqlite | while read id; do
	SUM=$(echo 'select categories.name from categories, location_category WHERE categories._id = location_category.categoryId AND location_category.locationId = '$id';' | callSqlite; \
	 echo 'select products.name FROM products, location_product WHERE products._id = location_product.productId AND location_product.locationId = '$id';' | callSqlite; \
	 echo 'select activities.name FROM activities, location_activity WHERE activities._id = location_activity.activityId AND location_activity.locationId = '$id';' | callSqlite; \
	 echo 'select locationTypes.name from locationTypes, locations WHERE locations.typeId = locationTypes._id AND locations._id = '${id}';' | callSqlite \
	 )
	echo "UPDATE Locations SET searchKeywords = '${SUM}' WHERE _id=${id};" | callSqlite
done
