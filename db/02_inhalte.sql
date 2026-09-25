-- =====================================================================
-- Caracalla-Thermen · Phase 8 · 02 Inhalte
-- Säle, Steinbrüche mit Wegen nach Rom, Steinsorten, Zuordnung zu den
-- Szenenmaterialien, Baugeschichte. Erzeugt von db/gen/inhalte.py.
-- Koordinaten: Länge, Breite (WGS 84, SRID 8307), gerundet.
-- ZEIT: Stelle am Zeitregler (0 = 216, 1 = heute). Sie folgt dem Bild
-- des Verfalls in der Szene, nicht linear den Jahren.
-- =====================================================================

INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,
  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,
  eye_x, eye_y, eye_z, look_x, look_y, look_z)
VALUES ('PALAESTRA_W', 'Palästra West', 'Offener Hof mit Säulenhallen an drei Seiten für Laufen, Ringen und Ballspiel vor dem Bad.', 1, 'RECHTECK', 67.0, 20.5, NULL, 'N', 'N', NULL, NULL, 1.0, 0.14, -93, 0, -39, -62, 20, 39, NULL, -68.5, 1.8, 31, -90, 6, -12)
/
INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,
  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,
  eye_x, eye_y, eye_z, look_x, look_y, look_z)
VALUES ('PALAESTRA_O', 'Palästra Ost', 'Der Hof auf der anderen Seite, gespiegelt zur Palästra West.', NULL, 'RECHTECK', 67.0, 20.5, NULL, 'N', 'N', NULL, NULL, 1.0, 0.14, 62, 0, -39, 93, 20, 39, NULL, 68.5, 1.8, 31, 90, 6, -12)
/
INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,
  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,
  eye_x, eye_y, eye_z, look_x, look_y, look_z)
VALUES ('CALDARIUM', 'Caldarium', 'Der heiße Rundsaal unter der großen Kuppel, mit drei Wannen in den Fensterbuchten und dem Labrum in der Mitte. Beheizt über Hypokaustum und Wandröhren.', 2, 'RUND', 35.0, 35.0, 44.5, 'J', 'J', 42, 40, 6.5, 0.55, 0, 0.3, 50, 0, 44, 0, 17.2, -2, 2.2, 36.5, 6, 13, 66)
/
INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,
  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,
  eye_x, eye_y, eye_z, look_x, look_y, look_z)
VALUES ('TEPIDARIUM', 'Tepidarium', 'Der lauwarme Saal zwischen heiß und kalt, mit zwei Becken zu den Seiten.', 3, 'RECHTECK', 20.0, 12.5, 22.0, 'J', 'J', 30, 29, 3.2, 0.4, -10, 0.2, 20, 10, 22, 32.5, NULL, 0, 1.8, 21, 0, 9, 56)
/
INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,
  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,
  eye_x, eye_y, eye_z, look_x, look_y, look_z)
VALUES ('FRIGIDARIUM', 'Frigidarium', 'Der kalte Saal unter drei Kreuzgratgewölben auf acht Granitsäulen, mit Kaltwasserbecken in den Seitenräumen.', 4, 'RECHTECK', 58.0, 24.0, 33.0, 'J', 'N', NULL, 16, 4.8, 0.48, -29, 0.2, -12, 29, 33, 12, NULL, -25.5, 1.9, 7, 12, 13, -4)
/
INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,
  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,
  eye_x, eye_y, eye_z, look_x, look_y, look_z)
VALUES ('NATATIO', 'Natatio', 'Das große Freibad vor der Schauwand mit drei Reihen von Statuennischen.', 5, 'RECHTECK', 54.0, 23.0, NULL, 'N', 'N', NULL, NULL, 1.3, 0.18, -31.4, -2, -52.5, 31.4, 24, -27, NULL, -24, 2.2, -29.5, 16, 8, -52)
/

INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('DOKIMEION', 'Docimium', 'İscehisar', 'Türkei',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(30.75, 38.87, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      30.75, 38.87,
      30.55, 38.52,
      30.17, 38.07,
      29.11, 37.84,
      27.84, 37.86,
      27.34, 37.94,
      26.3, 37.45,
      25.0, 36.9,
      23.3, 36.3,
      20.0, 36.8,
      15.75, 37.8,
      15.62, 38.25,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Über Land nach Synnada und Apameia, durch das Mäandertal über Laodikeia und Tralles nach Ephesos, dann zu Schiff um Kap Malea und durch die Straße von Messina nach Portus, den Tiber hinauf nach Rom.', 'https://link.springer.com/article/10.1007/s12665-020-08943-2')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('SIMITTHUS', 'Simitthus', 'Chemtou', 'Tunesien',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(8.58, 36.49, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      8.58, 36.49,
      9.2, 36.6,
      9.75, 36.72,
      10.06, 37.06,
      10.4, 37.3,
      11.0, 38.4,
      11.2, 39.5,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Auf gepflasterter Straße zum Bagradas (Medjerda), auf Flussschiffen nach Utica, dort auf Seeschiffe und westlich an Sizilien vorbei nach Portus. Später auch über die Straße nach Thabraca (Tabarka).', 'https://www.romeartlover.it/Simitthus.html')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('CHASAMBALI', NULL, 'Chasambali bei Larisa', 'Griechenland',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(22.5, 39.63, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      22.5, 39.63,
      22.93, 39.34,
      23.05, 39.12,
      23.6, 39.0,
      24.7, 38.5,
      24.9, 38.3,
      24.65, 38.05,
      24.1, 37.75,
      23.3, 36.3,
      20.0, 36.8,
      15.75, 37.8,
      15.62, 38.25,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Über Land zur Küste, vermutlich nach Demetrias am Golf von Volos (Hafen nicht belegt), zu Schiff um Euböa und Kap Malea nach Portus.', 'https://en.wikipedia.org/wiki/Verde_antico')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('MONS_PORPHYRITES', 'Mons Porphyrites', 'Gebel Dokhan', 'Ägypten',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(33.31, 27.25, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      33.31, 27.25,
      32.72, 26.16,
      32.24, 26.05,
      31.7, 26.56,
      31.18, 27.18,
      30.75, 28.1,
      31.1, 29.07,
      31.25, 29.85,
      30.55, 30.8,
      29.92, 31.2,
      24.0, 34.5,
      20.0, 36.8,
      15.75, 37.8,
      15.62, 38.25,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Durch die Ostwüste nach Kaine (Qena) am Nil, auf dem Nil nach Alexandria, zu Schiff südlich an Kreta vorbei und durch die Straße von Messina nach Portus.', 'https://en.wikipedia.org/wiki/Mons_Porphyrites')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('MONS_CLAUDIANUS', 'Mons Claudianus', 'Gebel Fatireh', 'Ägypten',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(33.49, 26.81, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      33.49, 26.81,
      32.72, 26.16,
      32.24, 26.05,
      31.7, 26.56,
      31.18, 27.18,
      30.75, 28.1,
      31.1, 29.07,
      31.25, 29.85,
      30.55, 30.8,
      29.92, 31.2,
      24.0, 34.5,
      20.0, 36.8,
      15.75, 37.8,
      15.62, 38.25,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Durch die Ostwüste nach Qena, den Nil hinab nach Alexandria, zu Schiff nach Portus und den Tiber hinauf. Dass die Granitsäulen der Thermen von hier stammen, ist nicht gesichert; belegt ist ägyptischer Granit.', 'https://en.wikipedia.org/wiki/Mons_Claudianus')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('LUNA', 'Luna', 'Carrara', 'Italien',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(10.1, 44.08, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      10.1, 44.08,
      10.02, 44.07,
      10.0, 43.5,
      10.6, 42.6,
      11.6, 42.1,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Von den Brüchen der Apuanischen Alpen zum Hafen von Luna, zu Schiff die Küste entlang nach Portus.', 'https://en.wikipedia.org/wiki/Carrara_marble')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('PROKONNESOS', 'Proconnesus', 'Marmara-Insel', 'Türkei',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(27.55, 40.6, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      27.55, 40.6,
      26.9, 40.35,
      26.15, 40.0,
      25.4, 39.4,
      24.9, 38.3,
      24.65, 38.05,
      24.1, 37.75,
      23.3, 36.3,
      20.0, 36.8,
      15.75, 37.8,
      15.62, 38.25,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Von der Insel im Marmarameer durch die Dardanellen, durch die Ägäis um Kap Malea und durch die Straße von Messina nach Portus.', 'https://de.wikipedia.org/wiki/Caracalla-Thermen')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('KROKEAI', 'Krokeai', 'Krokees bei Sparta', 'Griechenland',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(22.55, 36.87, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      22.55, 36.87,
      22.57, 36.76,
      22.6, 36.45,
      22.4, 36.2,
      20.0, 36.8,
      15.75, 37.8,
      15.62, 38.25,
      12.262, 41.779,
      12.33, 41.8,
      12.4, 41.83,
      12.45, 41.85,
      12.476, 41.876,
      12.4925, 41.879)),
  'Zum Hafen Gytheion, aus dem Lakonischen Golf um Kap Tainaron und durch das Ionische Meer nach Messina und Portus (Weg nicht eigens belegt).', 'https://www.aboutartonline.com/le-terme-di-caracalla-il-percorso-tra-i-grandiosi-resti-architettonici-gli-affreschi-di-vigna-guidi-e-le-idee-di-pietra-di-giuseppe-penone/')
/
INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)
VALUES ('TIBUR', 'Tibur', 'Bagni di Tivoli', 'Italien',
  MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(12.74, 41.94, NULL), NULL, NULL),
  MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(
      12.74, 41.94,
      12.62, 41.93,
      12.52, 41.91,
      12.51, 41.9,
      12.5, 41.885,
      12.4925, 41.879)),
  'Auf Ochsenkarren über die Via Tiburtina nach Rom, alternativ auf Flößen über den Aniene.', 'https://www.poggibros.it/news/roman-travertine/barco-quarries-roman-travertine-tivoli/')
/
UPDATE car_quarry SET weg_km = ROUND(MDSYS.SDO_GEOM.SDO_LENGTH(weg, 0.05, 'unit=KM'))
/

INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'PAVONAZZETTO', 'Pavonazzetto', 'marmor Phrygium', 'MARMOR', quarry_id, '#D8CFC2', 'Weißer Marmor mit violetten bis roten Adern aus Phrygien. In der Kaiserzeit einer der teuersten Buntmarmore.', 'Für die Caracalla-Thermen belegt. In der Szene: große Wandplatten und Felder im Boden.', 'https://link.springer.com/article/10.1007/s12665-020-08943-2' FROM car_quarry WHERE code = 'DOKIMEION'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'GIALLO_ANTICO', 'Giallo antico', 'marmor Numidicum', 'MARMOR', quarry_id, '#C9A15A', 'Gelber bis ockerfarbener Marmor aus Numidien, oft mit rötlichen Adern.', 'In den Böden der Thermen belegt. In der Szene: Rahmen der Wandfelder, Böden, Beckenränder und Säulen der Palästra.', 'https://www.aboutartonline.com/le-terme-di-caracalla-il-percorso-tra-i-grandiosi-resti-architettonici-gli-affreschi-di-vigna-guidi-e-le-idee-di-pietra-di-giuseppe-penone/' FROM car_quarry WHERE code = 'SIMITTHUS'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'VERDE_ANTICO', 'Verde antico', 'marmor Thessalicum', 'MARMOR', quarry_id, '#2F5A45', 'Dunkelgrüne Brekzie mit weißen und hellgrünen Brocken aus Thessalien.', 'In der Szene: Sockel und schmale Leisten. Für die Caracalla-Thermen nicht eigens belegt.', 'https://en.wikipedia.org/wiki/Verde_antico' FROM car_quarry WHERE code = 'CHASAMBALI'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'PORFIDO_ROSSO', 'Porfido rosso', 'lapis porphyrites', 'PORPHYR', quarry_id, '#6E1F25', 'Purpurroter Porphyr mit hellen Feldspat-Einsprenglingen, nur am Mons Porphyrites gebrochen und dem Kaiserhaus vorbehalten.', 'In den Böden der Thermen belegt. In der Szene: Scheiben in Wand und Boden, das Labrum im Caldarium.', 'https://en.wikipedia.org/wiki/Mons_Porphyrites' FROM car_quarry WHERE code = 'MONS_PORPHYRITES'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'GRANITO_GRIGIO', 'Grauer Granit', 'marmor Claudianum', 'GRANIT', quarry_id, '#6F6F72', 'Grauer, fein gesprenkelter Granit (Granodiorit) aus der Ostwüste Ägyptens.', 'Belegt: die acht Riesensäulen des Frigidariums aus ägyptischem Granit, fast 100 t schwer, und graue Granitsäulen an der Natatio. Der Bruch ist nicht gesichert.', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla' FROM car_quarry WHERE code = 'MONS_CLAUDIANUS'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'MARMO_LUNENSE', 'Lunensischer Marmor', 'marmor Lunense', 'MARMOR', quarry_id, '#E8E4DC', 'Weißer bis hellgrauer Marmor aus den Apuanischen Alpen, der Marmor von Carrara.', 'Für die Thermen belegt. In der Szene: obere Wandverkleidung, Gesimse und die Statuen (bei den Statuen eine Annahme).', 'https://de.wikipedia.org/wiki/Caracalla-Thermen' FROM car_quarry WHERE code = 'LUNA'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'MARMO_PROCONNESIO', 'Prokonnesischer Marmor', 'marmor Proconnesium', 'MARMOR', quarry_id, '#D9DAD6', 'Weißer Marmor mit grauen Bändern von der Insel Prokonnesos im Marmarameer, im ganzen Reich verbaut.', 'Für die Thermen belegt. In der Szene: Gesimse und die Verkleidung der Becken (bei den Becken eine Annahme).', 'https://de.wikipedia.org/wiki/Caracalla-Thermen' FROM car_quarry WHERE code = 'PROKONNESOS'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'SERPENTINO', 'Serpentino', 'lapis Lacedaemonius', 'SERPENTIN', quarry_id, '#1F4A2A', 'Dunkelgrüner Porphyr mit hellgrünen Einsprenglingen aus Lakonien.', 'In den Böden der Thermen belegt. In der Szene nicht eigens dargestellt.', 'https://www.aboutartonline.com/le-terme-di-caracalla-il-percorso-tra-i-grandiosi-resti-architettonici-gli-affreschi-di-vigna-guidi-e-le-idee-di-pietra-di-giuseppe-penone/' FROM car_quarry WHERE code = 'KROKEAI'
/
INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)
SELECT 'TRAVERTINO', 'Travertin', 'lapis Tiburtinus', 'KALKSTEIN', quarry_id, '#D8CDB6', 'Heller, poriger Kalkstein aus der Gegend von Tivoli, der Baustein Roms.', 'In der Szene: Pflaster der Höfe und Wege. Für die Thermen nicht eigens belegt.', 'https://www.poggibros.it/news/roman-travertine/barco-quarries-roman-travertine-tivoli/' FROM car_quarry WHERE code = 'TIBUR'
/

INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 6, stone_id, 'MARBLE', 'große Wandplatten' FROM car_stone WHERE code = 'PAVONAZZETTO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 6, stone_id, 'MARBLE', 'Rahmen der Wandfelder' FROM car_stone WHERE code = 'GIALLO_ANTICO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 6, stone_id, 'MARBLE', 'Sockel und Leisten' FROM car_stone WHERE code = 'VERDE_ANTICO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 6, stone_id, 'MARBLE', 'runde Scheiben' FROM car_stone WHERE code = 'PORFIDO_ROSSO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 6, stone_id, 'MARBLE', 'obere Wand' FROM car_stone WHERE code = 'MARMO_LUNENSE'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 7, stone_id, 'FLOOR', 'Bodenfelder' FROM car_stone WHERE code = 'GIALLO_ANTICO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 7, stone_id, 'FLOOR', 'Bodenfelder' FROM car_stone WHERE code = 'PAVONAZZETTO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 7, stone_id, 'FLOOR', 'Scheiben und Bänder' FROM car_stone WHERE code = 'PORFIDO_ROSSO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 7, stone_id, 'FLOOR', 'Fugenbänder' FROM car_stone WHERE code = 'VERDE_ANTICO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 8, stone_id, 'GRANITE', 'Säulen im Frigidarium' FROM car_stone WHERE code = 'GRANITO_GRIGIO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 15, stone_id, 'GIALLO', 'Säulen und Ränder' FROM car_stone WHERE code = 'GIALLO_ANTICO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 16, stone_id, 'PORPHYRY', 'Labrum' FROM car_stone WHERE code = 'PORFIDO_ROSSO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 20, stone_id, 'CORNICE', 'Gesimse' FROM car_stone WHERE code = 'MARMO_LUNENSE'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 20, stone_id, 'CORNICE', 'Gesimse' FROM car_stone WHERE code = 'MARMO_PROCONNESIO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 22, stone_id, 'STATUE', 'Annahme' FROM car_stone WHERE code = 'MARMO_LUNENSE'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 13, stone_id, 'POOL', 'Annahme' FROM car_stone WHERE code = 'MARMO_PROCONNESIO'
/
INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)
SELECT 3, stone_id, 'PAVING', 'Pflaster' FROM car_stone WHERE code = 'TRAVERTINO'
/

INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (212, 216, '212–216', 'Bau unter Caracalla', 'Nach den Ziegelstempeln beginnt der Bau 212. Eine ältere Deutung setzt den Beginn schon 206 unter Septimius Severus an.', 0.0, NULL, 'www.ostia-antica.org', 'https://www.ostia-antica.org/caracalla/italy/baths-building.htm')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (212, 213, '212–213', 'Aqua Antoniniana', 'Ein eigener Zweig der Aqua Marcia, über den sogenannten Drusus-Bogen geführt, bringt das Wasser zu den Thermen.', 0.0, NULL, 'www.ostia-antica.org', 'https://www.ostia-antica.org/caracalla/italy/baths-building.htm')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (216, NULL, '216', 'Einweihung', 'Caracalla weiht die Thermen ein. Bis zu 1600 Menschen können gleichzeitig baden, nach dem Bericht des Olympiodoros.', 0.0, NULL, 'soprintendenzaspecialeroma.it', 'https://soprintendenzaspecialeroma.it/luoghi/terme-di-caracalla/')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (218, 235, '218–235', 'Umfassung vollendet', 'Unter Elagabal und Severus Alexander entstehen die äußeren Hallen. Um 235 ist die Anlage im Wesentlichen fertig.', 0.02, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (270, 275, 'unter Aurelian', 'Wiederherstellung nach einem Brand', 'Aurelian lässt Schäden nach einem Brand beheben.', 0.04, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (284, 305, 'unter Diokletian', 'Wiederherstellung', 'Weitere Arbeiten unter Diokletian.', 0.05, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (306, 337, 'unter Konstantin', 'Umbau des Caldariums', 'Unter Konstantin wird das Caldarium umgebaut.', 0.06, (SELECT room_id FROM car_room WHERE code = 'CALDARIUM'), 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (537, NULL, '537', 'Die Goten kappen die Leitungen', 'Bei der Belagerung Roms durch Witigis werden die Aquädukte unterbrochen. Die Thermen fallen trocken und werden aufgegeben.', 0.3, NULL, 'soprintendenzaspecialeroma.it', 'https://soprintendenzaspecialeroma.it/luoghi/terme-di-caracalla/')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (847, NULL, '847', 'Erdbeben', 'Ein Erdbeben richtet schwere Zerstörungen an.', 0.5, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1524, NULL, '1524', 'Grabungslizenz', 'Papst Clemens VII. erlaubt Kardinal Lorenzo Pucci, in den Ruinen zu graben.', 0.7, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1545, 1547, '1545–1547', 'Grabungen der Farnese', 'Unter Papst Paul III. graben die Farnese nach Skulpturen und Bauteilen.', 0.73, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1546, NULL, '1546', 'Farnesischer Stier und Herkules', 'Beide Kolossalgruppen kommen 1546 zutage. Heute stehen sie im Archäologischen Nationalmuseum Neapel.', 0.75, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Farnese_Bull')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1560, 1565, '1560–1565', 'Eine Granitsäule für Florenz', 'Pius IV. schenkt Cosimo I. eine Granitsäule von der Natatio. Sie reist über Tiber, Meer und Arno nach Florenz und steht seit 1565 als Colonna della Giustizia an der Piazza Santa Trinita.', 0.78, (SELECT room_id FROM car_room WHERE code = 'NATATIO'), 'Wikipedia', 'https://it.wikipedia.org/wiki/Colonne_di_Firenze')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1824, NULL, '1824', 'Die Athletenmosaiken', 'Graf Egidio di Velo findet die Mosaiken mit Athleten. Sie liegen heute in den Vatikanischen Museen.', 0.85, NULL, 'Wikipedia', 'https://de.wikipedia.org/wiki/Caracalla-Thermen')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1858, 1869, '1858–1869', 'Das Haus in der Vigna Guidi', 'Unter den Thermen kommt ein älteres Wohnhaus mit Fresken zum Vorschein.', 0.86, NULL, 'soprintendenzaspecialeroma.it', 'https://soprintendenzaspecialeroma.it/luoghi/terme-di-caracalla/')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1878, 1879, '1878–1879', 'Grabungen Fiorellis', 'Mosaiken im Caldarium und in der westlichen Palästra werden freigelegt.', 0.88, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1912, NULL, '1912', 'Das Mithräum', 'Unter den Thermen wird das größte Mithräum Roms entdeckt. Andere Quellen nennen 1938.', 0.9, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1937, 1993, '1937–1993', 'Opernbühne', 'Im Caldarium spielt jeden Sommer die Oper von Rom.', 0.93, (SELECT room_id FROM car_room WHERE code = 'CALDARIUM'), 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1960, NULL, '1960', 'Olympische Spiele', 'Die Wettbewerbe im Turnen finden in den Thermen statt.', 0.95, NULL, 'Wikipedia', 'https://en.wikipedia.org/wiki/Baths_of_Caracalla')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (1990, NULL, '7. Juli 1990', 'Die drei Tenöre', 'Carreras, Domingo und Pavarotti singen zum ersten Mal gemeinsam, in den Caracalla-Thermen.', 0.96, NULL, 'Wikipedia', 'https://de.wikipedia.org/wiki/Caracalla-Thermen')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (2012, NULL, '2012', 'Unterirdische Gänge', 'Nach der Restaurierung öffnen das Mithräum (November) und ein Teil der unterirdischen Gänge (Dezember) für Besucher.', 0.98, NULL, 'www.thehistoryblog.com', 'https://www.thehistoryblog.com/archives/22128')
/
INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)
VALUES (2019, NULL, '2019', 'Weitere Gänge', 'Ein weiterer Abschnitt der Gänge mit einem Ziegelofen wird zugänglich.', 1.0, NULL, 'www.smithsonianmag.com', 'https://www.smithsonianmag.com/smart-news/you-can-now-tour-tunnels-beneath-romes-baths-caracalla-180972457/')
/
COMMIT
/
