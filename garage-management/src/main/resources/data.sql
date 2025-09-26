-- Initialisation des données : 10 accessories, 7 vehicles, 5 garages
-- Attention : les noms de tables et colonnes supposent la stratégie de nommage Spring (snake_case)
-- Si les tables diffèrent, vérifier dans la console H2 et adapter.

SET REFERENTIAL_INTEGRITY FALSE;

-- GARAGES
INSERT INTO garage (id, name, address, telephone, email) VALUES (1,'Garage Centre Ville','12 Rue Centrale','0102030405','contact1@garage.local');
INSERT INTO garage (id, name, address, telephone, email) VALUES (2,'Garage Nord','5 Avenue du Nord','0102030406','contact2@garage.local');
INSERT INTO garage (id, name, address, telephone, email) VALUES (3,'Garage Sud','48 Route du Sud','0102030407','contact3@garage.local');
INSERT INTO garage (id, name, address, telephone, email) VALUES (4,'Garage Ouest','7 Boulevard Ouest','0102030408','contact4@garage.local');
INSERT INTO garage (id, name, address, telephone, email) VALUES (5,'Garage Est','99 Chemin de l''Est','0102030409','contact5@garage.local');

-- VEHICLES
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (1,'Renault Clio',2022,'GASOLINE');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (2,'Renault Megane',2023,'DIESEL');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (3,'Renault Zoe',2024,'ELECTRIC');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (4,'Renault Austral',2023,'HYBRID');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (5,'Renault Kangoo',2021,'DIESEL');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (6,'Renault Scenic',2020,'GASOLINE');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (7,'Renault Master',2022,'DIESEL');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (8,'Renault Twingo',2024,'ELECTRIC');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (9,'Renault Captur',2023,'HYBRID');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (10,'Renault Arkana',2022,'GASOLINE');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (11,'Renault Espace',2021,'DIESEL');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (12,'Renault Koleos',2023,'HYBRID');
INSERT INTO vehicle (id, brand, year_of_manufacture, fuel_type) VALUES (13,'Renault Koleos',2024,'DIESEL');

-- ACCESSORIES
INSERT INTO accessory (id, name, description, price, type) VALUES ( 1,'GPS','Système navigation intégré',299.90,'ELECTRONIC');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 2,'Caméra recul','Caméra haute définition',180.00,'ELECTRONIC');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 3,'Attelage','Attelage remorque renforcé',420.00,'MECHANIC');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 4,'Tapis caoutchouc','Tapis résistants',59.99,'CONFORT');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 5,'Chargeur rapide','Chargeur véhicule électrique',650.00,'ELECTRIC');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 6,'Barres de toit','Aluminium universel',210.00,'EXTERIOR');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 7,'Pack LED','Éclairage intérieur LED',130.00,'ELECTRIC');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 8,'Filet coffre','Organisation espace arrière',45.00,'CONFORT');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 9,'Alarm Plus','Système alarme avancé',360.00,'SECURITY');
INSERT INTO accessory (id, name, description, price, type) VALUES ( 10,'Siège enfant','Homologué ISOFIX',250.00,'SAFETY');



-- TABLES DE JOINTURE
-- Hypothèse 1 : ManyToMany Garage <-> Vehicle => table garage_vehicle (garage_id, vehicles_id)
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (1,1);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (1,2);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (1,3);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (2,4);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (2,5);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (3,6);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (3,7);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (4,8);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (4,9);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (5,10);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (5,11);
INSERT INTO vehicle_garages (garage_id, vehicle_id) VALUES (5,12);

-- Hypothèse 2 : Unidirectional OneToMany Vehicle -> accessories => table vehicle_accessories (vehicle_id, accessories_id)
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (1,1);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (2,2);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (2,3);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (1,4);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (3,5);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (4,6);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (3,7);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (4,8);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (3,9);
INSERT INTO accessory_vehicles (vehicle_id, accessory_id) VALUES (5,10);

-- AJOUT / CORRECTION : HORAIRES D'OUVERTURE (OpeningSlot)
-- IMPORTANT : OpeningSlot possède un champ embeddé 'openingTime' contenant 'startTime' et 'endTime'.
-- Sans @AttributeOverride, Hibernate crée simplement les colonnes start_time et end_time.
-- Table OpeningSlot : opening_slot (id, day_of_week, start_time, end_time)

DELETE FROM garage_opening_slots; -- nettoyage si reload
DELETE FROM opening_slot;

INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (1,'MONDAY','08:00:00','12:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (2,'MONDAY','13:30:00','18:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (3,'TUESDAY','08:00:00','12:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (4,'TUESDAY','13:30:00','18:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (5,'MONDAY','07:30:00','12:30:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (6,'MONDAY','14:00:00','19:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (7,'TUESDAY','07:30:00','12:30:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (8,'TUESDAY','14:00:00','19:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (9,'SATURDAY','09:00:00','13:00:00');
INSERT INTO opening_slot (id, day_of_week, start_time, end_time) VALUES (10,'SATURDAY','14:00:00','16:00:00');

-- Table de jointure : garage_opening_slots (garage_id, opening_slots_id)
DELETE FROM garage_opening_slots;
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (1,1);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (1,2);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (1,9);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (1,10);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (2,3);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (2,4);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (3,5);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (3,6);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (4,7);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (4,8);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (5,1);
INSERT INTO garage_opening_slots (garage_id, opening_slots_id) VALUES (5,4);

SET REFERENTIAL_INTEGRITY TRUE;

-- IMPORTANT : Réaligner les compteurs IDENTITY pour éviter que Hibernate réessaie d'insérer des IDs déjà utilisés
ALTER TABLE accessory      ALTER COLUMN id RESTART WITH 11; -- prochain = 11 (nous avons 1..10)
ALTER TABLE vehicle        ALTER COLUMN id RESTART WITH 14;  -- prochain = 14 (nous avons 1..7)
ALTER TABLE garage         ALTER COLUMN id RESTART WITH 6;  -- prochain = 6 (nous avons 1..5)
ALTER TABLE opening_slot   ALTER COLUMN id RESTART WITH 11; -- prochain = 11 (nous avons 1..10)

-- Vérification rapide (optionnel en console H2):
-- SELECT * FROM garage;
-- SELECT * FROM vehicle;
-- SELECT * FROM accessory;

-- Vérification rapide (exemple horaires) :
-- SELECT g.name, os.day_of_week, os.start_time, os.end_time FROM garage g
-- JOIN garage_opening_slots gos ON g.id = gos.garage_id
-- JOIN opening_slot os ON gos.opening_slots_id = os.id
-- ORDER BY g.id, os.day_of_week, os.start_time;
