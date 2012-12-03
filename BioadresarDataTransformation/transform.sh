MYSQL_SCRIPT="$1"
TARGET_DB="$2"


# dump to mysql
MYDB_NAME="hnutiduha9650"
MYDB_USER="duha_importer"
MYDB_PASS="importer"

function importMysql()
{
	cat "${MYSQL_SCRIPT}" | sudo mysql
	echo "grant SELECT, DROP ON ${MYDB_NAME}.* TO ${MYDB_USER}@localhost IDENTIFIED BY '${MYDB_PASS}'; 
	FLUSH PRIVILEGES;" | sudo mysql
}

function callMysql()
{
	mysql -B -s --user="${MYDB_USER}" --password="${MYDB_PASS}" "${MYDB_NAME}"
}

function removeMysql()
{
	callMysql "DROP ${MYDB_NAME};"
}

function callSqlite()
{
	sqlite3 "${TARGET_DB}"
}


function addProducts()
{
	#create categories
	echo 'CREATE TABLE category (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);' | callSqlite
	
	#create products
	echo 'CREATE TABLE product (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, category_id);' | sqlite3 "${TARGET_DB}"
	
	echo 'SELECT id,je_kategorie,nazev FROM produkt;' | callMysql | while read id kategorie nazev; do 
	if [ "${kategorie}" = "ano" ]; then
		echo "insert into category(_id,name) values(${id},'${nazev}'" | callSqlite
	else
		echo "insert into product(_id,name) values(${id},'${nazev}'" | callSqlite
	fi
	
	# TODO: manually create links between categories and products
}

#create farms
CREATE TABLE farm (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, gps_lat REAL NOT NULL, gps_long REAL NOT NULL, desc TEXT, type TEXT);

#create table contacts
CREATE TABLE contact (_id INTEGER PRIMARY KEY AUTOINCREMENT, farm_id INTEGER, type TEXT, contact TEXT, UNIQUE (farm_id, type, contact));

#fill farms & contacts

#producent -> jmena farem
CREATE TABLE IF NOT EXISTS `producent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nazev` varchar(60) COLLATE utf8_czech_ci NOT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci AUTO_INCREMENT=599 ;

# divize -> popisy pro jednotlive farmy
CREATE TABLE IF NOT EXISTS `divize` (
  `id` int(11) NOT NULL AUTO_INCREMENT, -> farm id ?
  `producent_id` int(11) NOT NULL,
  `nazevdivize` varchar(60) COLLATE utf8_czech_ci DEFAULT NULL,
  `typ` enum('farma','eshop','obchod','bioklub','ostatni') COLLATE utf8_czech_ci DEFAULT NULL,
  `poznamka` varchar(2000) COLLATE utf8_czech_ci DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_divize_producent1` (`producent_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci COMMENT='pohled divize_all: SELECT `divize`.`id` AS `id`,`divize`.`ty' AUTO_INCREMENT=672 ;

# kontakt -> kontakty k divizim (farmam)
CREATE TABLE IF NOT EXISTS `kontakt` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `mobil` varchar(45) COLLATE utf8_czech_ci DEFAULT NULL,
  `web` varchar(90) COLLATE utf8_czech_ci DEFAULT NULL,
  `web2` varchar(90) COLLATE utf8_czech_ci NOT NULL,
  `ulice` varchar(45) COLLATE utf8_czech_ci DEFAULT NULL,
  `kraj` enum('PHA','JHC','JHM','KVK','VYS','KHK','LBK','MSK','OLK','PAK','PLK','STC','ULK','ZLK') COLLATE utf8_czech_ci DEFAULT NULL,
  `mesto` varchar(45) COLLATE utf8_czech_ci DEFAULT NULL,
  `web_eshop` varchar(45) COLLATE utf8_czech_ci DEFAULT NULL,
  `divize_id` int(11) DEFAULT NULL,
  `email` varchar(150) COLLATE utf8_czech_ci DEFAULT NULL,
  `zobrazit_mapu` enum('ano','ne') COLLATE utf8_czech_ci NOT NULL DEFAULT 'ne',
  `latitude` double DEFAULT NULL,
  `longtitude` double DEFAULT NULL,
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `fotka` varchar(60) COLLATE utf8_czech_ci DEFAULT NULL,
  `osoba` varchar(60) COLLATE utf8_czech_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `divize_id` (`divize_id`),
  KEY `fk_kontakt_divize1` (`divize_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_czech_ci AUTO_INCREMENT=629 ;


#create farm:category table
CREATE TABLE farm_category (_id INTEGER PRIMARY KEY AUTOINCREMENT, farm_id INTEGER, category_id INTEGER, UNIQUE (farm_id, category_id));

#create farm:product table
CREATE TABLE farm_product (_id INTEGER PRIMARY KEY AUTOINCREMENT, farm_id INTEGER, product_id INTEGER, UNIQUE (farm_id, product_id));

# fill products and categories

#produkuje - match mezi farmami a produkty. TODO; poznamky, predevsim ignorujeme
CREATE TABLE IF NOT EXISTS `produkuje` (
  `poznamka` varchar(60) CHARACTER SET utf8 COLLATE utf8_czech_ci DEFAULT NULL,
  `divize_id` int(11) NOT NULL,
  `produkt_id` int(11) NOT NULL,
  `predevsim` enum('ano','ne') NOT NULL DEFAULT 'ne',
  `last_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `fk_produkuje_divize1` (`divize_id`),
  KEY `fk_produkuje_produkt1` (`produkt_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


kraje -> ignorovat (souradnice na stredy kraju)
kraje_old -> ignorovat (zkratky pro kraje)
devel -> ignorovat
divize_all, divize_viditelne -> nejake helpery pro web, ignorovat
cinnost -> TODO, zatim ignorujeme
dela -> TODO: match mezi divizema a cinnostmy (s poznamkou), zatim ignorujeme
