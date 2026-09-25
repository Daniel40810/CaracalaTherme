# Erzeugt db/02_inhalte.sql aus den recherchierten Daten (Koordinaten: Länge, Breite).
def q(s): return 'NULL' if s is None else "'" + str(s).replace("'", "''") + "'"
def n(v): return 'NULL' if v is None else repr(v)

ROME = [(12.262,41.779),(12.33,41.80),(12.40,41.83),(12.45,41.85),(12.476,41.876),(12.4925,41.879)]
TYRRH = [(15.62,38.25)]           # Straße von Messina
IONIAN = [(20.0,36.8),(15.75,37.8)] + TYRRH
MALEA = [(23.3,36.3)] + IONIAN
NILE = [(32.72,26.16),(32.24,26.05),(31.7,26.56),(31.18,27.18),(30.75,28.1),(31.1,29.07),(31.25,29.85),(30.55,30.8),(29.92,31.20),(24.0,34.5)] + IONIAN

quarries = [
 ('DOKIMEION','Docimium','İscehisar','Türkei',(30.75,38.87),
  [(30.75,38.87),(30.55,38.52),(30.17,38.07),(29.11,37.84),(27.84,37.86),(27.34,37.94),(26.3,37.45),(25.0,36.9)]+MALEA+ROME,
  'Über Land nach Synnada und Apameia, durch das Mäandertal über Laodikeia und Tralles nach Ephesos, dann zu Schiff um Kap Malea und durch die Straße von Messina nach Portus, den Tiber hinauf nach Rom.',
  'https://link.springer.com/article/10.1007/s12665-020-08943-2'),
 ('SIMITTHUS','Simitthus','Chemtou','Tunesien',(8.58,36.49),
  [(8.58,36.49),(9.2,36.6),(9.75,36.72),(10.06,37.06),(10.4,37.3),(11.0,38.4),(11.2,39.5)]+ROME,
  'Auf gepflasterter Straße zum Bagradas (Medjerda), auf Flussschiffen nach Utica, dort auf Seeschiffe und westlich an Sizilien vorbei nach Portus. Später auch über die Straße nach Thabraca (Tabarka).',
  'https://www.romeartlover.it/Simitthus.html'),
 ('CHASAMBALI',None,'Chasambali bei Larisa','Griechenland',(22.50,39.63),
  [(22.50,39.63),(22.93,39.34),(23.05,39.12),(23.6,39.0),(24.7,38.5),(24.9,38.3),(24.65,38.05),(24.1,37.75)]+MALEA+ROME,
  'Über Land zur Küste, vermutlich nach Demetrias am Golf von Volos (Hafen nicht belegt), zu Schiff um Euböa und Kap Malea nach Portus.',
  'https://en.wikipedia.org/wiki/Verde_antico'),
 ('MONS_PORPHYRITES','Mons Porphyrites','Gebel Dokhan','Ägypten',(33.31,27.25),
  [(33.31,27.25)]+NILE+ROME,
  'Durch die Ostwüste nach Kaine (Qena) am Nil, auf dem Nil nach Alexandria, zu Schiff südlich an Kreta vorbei und durch die Straße von Messina nach Portus.',
  'https://en.wikipedia.org/wiki/Mons_Porphyrites'),
 ('MONS_CLAUDIANUS','Mons Claudianus','Gebel Fatireh','Ägypten',(33.49,26.81),
  [(33.49,26.81)]+NILE+ROME,
  'Durch die Ostwüste nach Qena, den Nil hinab nach Alexandria, zu Schiff nach Portus und den Tiber hinauf. Dass die Granitsäulen der Thermen von hier stammen, ist nicht gesichert; belegt ist ägyptischer Granit.',
  'https://en.wikipedia.org/wiki/Mons_Claudianus'),
 ('LUNA','Luna','Carrara','Italien',(10.10,44.08),
  [(10.10,44.08),(10.02,44.07),(10.0,43.5),(10.6,42.6),(11.6,42.1)]+ROME,
  'Von den Brüchen der Apuanischen Alpen zum Hafen von Luna, zu Schiff die Küste entlang nach Portus.',
  'https://en.wikipedia.org/wiki/Carrara_marble'),
 ('PROKONNESOS','Proconnesus','Marmara-Insel','Türkei',(27.55,40.60),
  [(27.55,40.60),(26.9,40.35),(26.15,40.0),(25.4,39.4),(24.9,38.3),(24.65,38.05),(24.1,37.75)]+MALEA+ROME,
  'Von der Insel im Marmarameer durch die Dardanellen, durch die Ägäis um Kap Malea und durch die Straße von Messina nach Portus.',
  'https://de.wikipedia.org/wiki/Caracalla-Thermen'),
 ('KROKEAI','Krokeai','Krokees bei Sparta','Griechenland',(22.55,36.87),
  [(22.55,36.87),(22.57,36.76),(22.6,36.45),(22.4,36.2)]+IONIAN+ROME,
  'Zum Hafen Gytheion, aus dem Lakonischen Golf um Kap Tainaron und durch das Ionische Meer nach Messina und Portus (Weg nicht eigens belegt).',
  'https://www.aboutartonline.com/le-terme-di-caracalla-il-percorso-tra-i-grandiosi-resti-architettonici-gli-affreschi-di-vigna-guidi-e-le-idee-di-pietra-di-giuseppe-penone/'),
 ('TIBUR','Tibur','Bagni di Tivoli','Italien',(12.74,41.94),
  [(12.74,41.94),(12.62,41.93),(12.52,41.91),(12.51,41.90),(12.50,41.885),(12.4925,41.879)],
  'Auf Ochsenkarren über die Via Tiburtina nach Rom, alternativ auf Flößen über den Aniene.',
  'https://www.poggibros.it/news/roman-travertine/barco-quarries-roman-travertine-tivoli/'),
]

stones = [
 ('PAVONAZZETTO','Pavonazzetto','marmor Phrygium','MARMOR','DOKIMEION','#D8CFC2',
  'Weißer Marmor mit violetten bis roten Adern aus Phrygien. In der Kaiserzeit einer der teuersten Buntmarmore.',
  'Für die Caracalla-Thermen belegt. In der Szene: große Wandplatten und Felder im Boden.','https://link.springer.com/article/10.1007/s12665-020-08943-2'),
 ('GIALLO_ANTICO','Giallo antico','marmor Numidicum','MARMOR','SIMITTHUS','#C9A15A',
  'Gelber bis ockerfarbener Marmor aus Numidien, oft mit rötlichen Adern.',
  'In den Böden der Thermen belegt. In der Szene: Rahmen der Wandfelder, Böden, Beckenränder und Säulen der Palästra.','https://www.aboutartonline.com/le-terme-di-caracalla-il-percorso-tra-i-grandiosi-resti-architettonici-gli-affreschi-di-vigna-guidi-e-le-idee-di-pietra-di-giuseppe-penone/'),
 ('VERDE_ANTICO','Verde antico','marmor Thessalicum','MARMOR','CHASAMBALI','#2F5A45',
  'Dunkelgrüne Brekzie mit weißen und hellgrünen Brocken aus Thessalien.',
  'In der Szene: Sockel und schmale Leisten. Für die Caracalla-Thermen nicht eigens belegt.','https://en.wikipedia.org/wiki/Verde_antico'),
 ('PORFIDO_ROSSO','Porfido rosso','lapis porphyrites','PORPHYR','MONS_PORPHYRITES','#6E1F25',
  'Purpurroter Porphyr mit hellen Feldspat-Einsprenglingen, nur am Mons Porphyrites gebrochen und dem Kaiserhaus vorbehalten.',
  'In den Böden der Thermen belegt. In der Szene: Scheiben in Wand und Boden, das Labrum im Caldarium.','https://en.wikipedia.org/wiki/Mons_Porphyrites'),
 ('GRANITO_GRIGIO','Grauer Granit','marmor Claudianum','GRANIT','MONS_CLAUDIANUS','#6F6F72',
  'Grauer, fein gesprenkelter Granit (Granodiorit) aus der Ostwüste Ägyptens.',
  'Belegt: die acht Riesensäulen des Frigidariums aus ägyptischem Granit, fast 100 t schwer, und graue Granitsäulen an der Natatio. Der Bruch ist nicht gesichert.','https://en.wikipedia.org/wiki/Baths_of_Caracalla'),
 ('MARMO_LUNENSE','Lunensischer Marmor','marmor Lunense','MARMOR','LUNA','#E8E4DC',
  'Weißer bis hellgrauer Marmor aus den Apuanischen Alpen, der Marmor von Carrara.',
  'Für die Thermen belegt. In der Szene: obere Wandverkleidung, Gesimse und die Statuen (bei den Statuen eine Annahme).','https://de.wikipedia.org/wiki/Caracalla-Thermen'),
 ('MARMO_PROCONNESIO','Prokonnesischer Marmor','marmor Proconnesium','MARMOR','PROKONNESOS','#D9DAD6',
  'Weißer Marmor mit grauen Bändern von der Insel Prokonnesos im Marmarameer, im ganzen Reich verbaut.',
  'Für die Thermen belegt. In der Szene: Gesimse und die Verkleidung der Becken (bei den Becken eine Annahme).','https://de.wikipedia.org/wiki/Caracalla-Thermen'),
 ('SERPENTINO','Serpentino','lapis Lacedaemonius','SERPENTIN','KROKEAI','#1F4A2A',
  'Dunkelgrüner Porphyr mit hellgrünen Einsprenglingen aus Lakonien.',
  'In den Böden der Thermen belegt. In der Szene nicht eigens dargestellt.','https://www.aboutartonline.com/le-terme-di-caracalla-il-percorso-tra-i-grandiosi-resti-architettonici-gli-affreschi-di-vigna-guidi-e-le-idee-di-pietra-di-giuseppe-penone/'),
 ('TRAVERTINO','Travertin','lapis Tiburtinus','KALKSTEIN','TIBUR','#D8CDB6',
  'Heller, poriger Kalkstein aus der Gegend von Tivoli, der Baustein Roms.',
  'In der Szene: Pflaster der Höfe und Wege. Für die Thermen nicht eigens belegt.','https://www.poggibros.it/news/roman-travertine/barco-quarries-roman-travertine-tivoli/'),
]

mats = [ # mat_no, mat_code, stone, hinweis
 (6,'MARBLE','PAVONAZZETTO','große Wandplatten'),(6,'MARBLE','GIALLO_ANTICO','Rahmen der Wandfelder'),
 (6,'MARBLE','VERDE_ANTICO','Sockel und Leisten'),(6,'MARBLE','PORFIDO_ROSSO','runde Scheiben'),(6,'MARBLE','MARMO_LUNENSE','obere Wand'),
 (7,'FLOOR','GIALLO_ANTICO','Bodenfelder'),(7,'FLOOR','PAVONAZZETTO','Bodenfelder'),(7,'FLOOR','PORFIDO_ROSSO','Scheiben und Bänder'),(7,'FLOOR','VERDE_ANTICO','Fugenbänder'),
 (8,'GRANITE','GRANITO_GRIGIO','Säulen im Frigidarium'),
 (15,'GIALLO','GIALLO_ANTICO','Säulen und Ränder'),
 (16,'PORPHYRY','PORFIDO_ROSSO','Labrum'),
 (20,'CORNICE','MARMO_LUNENSE','Gesimse'),(20,'CORNICE','MARMO_PROCONNESIO','Gesimse'),
 (22,'STATUE','MARMO_LUNENSE','Annahme'),
 (13,'POOL','MARMO_PROCONNESIO','Annahme'),
 (3,'PAVING','TRAVERTINO','Pflaster'),
]

W='https://en.wikipedia.org/wiki/Baths_of_Caracalla'
events = [ # jahr, bis, datierung, titel, text, zeit, room, quelle_url
 (212,216,'212–216','Bau unter Caracalla','Nach den Ziegelstempeln beginnt der Bau 212. Eine ältere Deutung setzt den Beginn schon 206 unter Septimius Severus an.',0.0,None,'https://www.ostia-antica.org/caracalla/italy/baths-building.htm'),
 (212,213,'212–213','Aqua Antoniniana','Ein eigener Zweig der Aqua Marcia, über den sogenannten Drusus-Bogen geführt, bringt das Wasser zu den Thermen.',0.0,None,'https://www.ostia-antica.org/caracalla/italy/baths-building.htm'),
 (216,None,'216','Einweihung','Caracalla weiht die Thermen ein. Bis zu 1600 Menschen können gleichzeitig baden, nach dem Bericht des Olympiodoros.',0.0,None,'https://soprintendenzaspecialeroma.it/luoghi/terme-di-caracalla/'),
 (218,235,'218–235','Umfassung vollendet','Unter Elagabal und Severus Alexander entstehen die äußeren Hallen. Um 235 ist die Anlage im Wesentlichen fertig.',0.02,None,W),
 (270,275,'unter Aurelian','Wiederherstellung nach einem Brand','Aurelian lässt Schäden nach einem Brand beheben.',0.04,None,W),
 (284,305,'unter Diokletian','Wiederherstellung','Weitere Arbeiten unter Diokletian.',0.05,None,W),
 (306,337,'unter Konstantin','Umbau des Caldariums','Unter Konstantin wird das Caldarium umgebaut.',0.06,'CALDARIUM',W),
 (537,None,'537','Die Goten kappen die Leitungen','Bei der Belagerung Roms durch Witigis werden die Aquädukte unterbrochen. Die Thermen fallen trocken und werden aufgegeben.',0.3,None,'https://soprintendenzaspecialeroma.it/luoghi/terme-di-caracalla/'),
 (847,None,'847','Erdbeben','Ein Erdbeben richtet schwere Zerstörungen an.',0.5,None,W),
 (1524,None,'1524','Grabungslizenz','Papst Clemens VII. erlaubt Kardinal Lorenzo Pucci, in den Ruinen zu graben.',0.7,None,W),
 (1545,1547,'1545–1547','Grabungen der Farnese','Unter Papst Paul III. graben die Farnese nach Skulpturen und Bauteilen.',0.73,None,W),
 (1546,None,'1546','Farnesischer Stier und Herkules','Beide Kolossalgruppen kommen 1546 zutage. Heute stehen sie im Archäologischen Nationalmuseum Neapel.',0.75,None,'https://en.wikipedia.org/wiki/Farnese_Bull'),
 (1560,1565,'1560–1565','Eine Granitsäule für Florenz','Pius IV. schenkt Cosimo I. eine Granitsäule von der Natatio. Sie reist über Tiber, Meer und Arno nach Florenz und steht seit 1565 als Colonna della Giustizia an der Piazza Santa Trinita.',0.78,'NATATIO','https://it.wikipedia.org/wiki/Colonne_di_Firenze'),
 (1824,None,'1824','Die Athletenmosaiken','Graf Egidio di Velo findet die Mosaiken mit Athleten. Sie liegen heute in den Vatikanischen Museen.',0.85,None,'https://de.wikipedia.org/wiki/Caracalla-Thermen'),
 (1858,1869,'1858–1869','Das Haus in der Vigna Guidi','Unter den Thermen kommt ein älteres Wohnhaus mit Fresken zum Vorschein.',0.86,None,'https://soprintendenzaspecialeroma.it/luoghi/terme-di-caracalla/'),
 (1878,1879,'1878–1879','Grabungen Fiorellis','Mosaiken im Caldarium und in der westlichen Palästra werden freigelegt.',0.88,None,W),
 (1912,None,'1912','Das Mithräum','Unter den Thermen wird das größte Mithräum Roms entdeckt. Andere Quellen nennen 1938.',0.9,None,W),
 (1937,1993,'1937–1993','Opernbühne','Im Caldarium spielt jeden Sommer die Oper von Rom.',0.93,'CALDARIUM',W),
 (1960,None,'1960','Olympische Spiele','Die Wettbewerbe im Turnen finden in den Thermen statt.',0.95,None,W),
 (1990,None,'7. Juli 1990','Die drei Tenöre','Carreras, Domingo und Pavarotti singen zum ersten Mal gemeinsam, in den Caracalla-Thermen.',0.96,None,'https://de.wikipedia.org/wiki/Caracalla-Thermen'),
 (2012,None,'2012','Unterirdische Gänge','Nach der Restaurierung öffnen das Mithräum (November) und ein Teil der unterirdischen Gänge (Dezember) für Besucher.',0.98,None,'https://www.thehistoryblog.com/archives/22128'),
 (2019,None,'2019','Weitere Gänge','Ein weiterer Abschnitt der Gänge mit einem Ziegelofen wird zugänglich.',1.0,None,'https://www.smithsonianmag.com/smart-news/you-can-now-tour-tunnels-beneath-romes-baths-caracalla-180972457/'),
]

rooms = [ # code,name,funktion,badefolge,form,l,b,h,ueb,beh,tl,tw,rt,wet,box(6),radius,eye,look
 ('PALAESTRA_W','Palästra West','Offener Hof mit Säulenhallen an drei Seiten für Laufen, Ringen und Ballspiel vor dem Bad.',1,'RECHTECK',67.0,20.5,None,'N','N',None,None,1.0,0.14,(-93,0,-39,-62,20,39),None,(-68.5,1.8,31),(-90,6,-12)),
 ('PALAESTRA_O','Palästra Ost','Der Hof auf der anderen Seite, gespiegelt zur Palästra West.',None,'RECHTECK',67.0,20.5,None,'N','N',None,None,1.0,0.14,(62,0,-39,93,20,39),None,(68.5,1.8,31),(90,6,-12)),
 ('CALDARIUM','Caldarium','Der heiße Rundsaal unter der großen Kuppel, mit drei Wannen in den Fensterbuchten und dem Labrum in der Mitte. Beheizt über Hypokaustum und Wandröhren.',2,'RUND',35.0,35.0,44.5,'J','J',42,40,6.5,0.55,(0,0.3,50,0,44,0),17.2,(-2,2.2,36.5),(6,13,66)),
 ('TEPIDARIUM','Tepidarium','Der lauwarme Saal zwischen heiß und kalt, mit zwei Becken zu den Seiten.',3,'RECHTECK',20.0,12.5,22.0,'J','J',30,29,3.2,0.4,(-10,0.2,20,10,22,32.5),None,(0,1.8,21),(0,9,56)),
 ('FRIGIDARIUM','Frigidarium','Der kalte Saal unter drei Kreuzgratgewölben auf acht Granitsäulen, mit Kaltwasserbecken in den Seitenräumen.',4,'RECHTECK',58.0,24.0,33.0,'J','N',None,16,4.8,0.48,(-29,0.2,-12,29,33,12),None,(-25.5,1.9,7),(12,13,-4)),
 ('NATATIO','Natatio','Das große Freibad vor der Schauwand mit drei Reihen von Statuennischen.',5,'RECHTECK',54.0,23.0,None,'N','N',None,None,1.3,0.18,(-31.4,-2,-52.5,31.4,24,-27),None,(-24,2.2,-29.5),(16,8,-52)),
]

def geom_point(p): return "MDSYS.SDO_GEOMETRY(2001, 8307, MDSYS.SDO_POINT_TYPE(%s, %s, NULL), NULL, NULL)" % p
def geom_line(pts):
    ords = ',\n      '.join('%s, %s' % p for p in pts)
    return "MDSYS.SDO_GEOMETRY(2002, 8307, NULL, MDSYS.SDO_ELEM_INFO_ARRAY(1, 2, 1), MDSYS.SDO_ORDINATE_ARRAY(\n      %s))" % ords

out = ['-- =====================================================================',
       '-- Caracalla-Thermen · Phase 8 · 02 Inhalte',
       '-- Säle, Steinbrüche mit Wegen nach Rom, Steinsorten, Zuordnung zu den',
       '-- Szenenmaterialien, Baugeschichte. Erzeugt von db/gen/inhalte.py.',
       '-- Koordinaten: Länge, Breite (WGS 84, SRID 8307), gerundet.',
       '-- ZEIT: Stelle am Zeitregler (0 = 216, 1 = heute). Sie folgt dem Bild',
       '-- des Verfalls in der Szene, nicht linear den Jahren.',
       '-- =====================================================================', '']
for r in rooms:
    code,name,fn,bf,form,l,b,h,ue,be,tl,tw,rt,wet,box,rad,eye,look = r
    out.append("INSERT INTO car_room (code, name, funktion, badefolge, form, laenge_m, breite_m, hoehe_m, ueberdacht, beheizt,\n"
      "  temp_luft_c, temp_wasser_c, nachhall_s, hall_anteil, box_x0, box_y0, box_z0, box_x1, box_y1, box_z1, radius,\n"
      "  eye_x, eye_y, eye_z, look_x, look_y, look_z)\nVALUES (%s)\n/" % ', '.join(
      [q(code),q(name),q(fn),n(bf),q(form),n(l),n(b),n(h),q(ue),q(be),n(tl),n(tw),n(rt),n(wet)] + [n(v) for v in box] + [n(rad)] + [n(v) for v in eye] + [n(v) for v in look]))
out.append('')
for c,na,nh,land,pt,line,txt,src in quarries:
    out.append("INSERT INTO car_quarry (code, name_antik, name_heute, land_heute, lage, weg, weg_text, quelle)\nVALUES (%s, %s, %s, %s,\n  %s,\n  %s,\n  %s, %s)\n/" %
               (q(c),q(na),q(nh),q(land),geom_point(pt),geom_line(line),q(txt),q(src)))
out.append("UPDATE car_quarry SET weg_km = ROUND(MDSYS.SDO_GEOM.SDO_LENGTH(weg, 0.05, 'unit=KM'))\n/")
out.append('')
for c,na,ant,art,qc,hexc,desc,inth,src in stones:
    out.append("INSERT INTO car_stone (code, name, name_antik, art, quarry_id, farbe_hex, beschreibung, in_den_thermen, quelle)\n"
               "SELECT %s, %s, %s, %s, quarry_id, %s, %s, %s, %s FROM car_quarry WHERE code = %s\n/" % (q(c),q(na),q(ant),q(art),q(hexc),q(desc),q(inth),q(src),q(qc)))
out.append('')
for no,mc,sc,h in mats:
    out.append("INSERT INTO car_mat_stone (mat_no, stone_id, mat_code, hinweis)\nSELECT %d, stone_id, %s, %s FROM car_stone WHERE code = %s\n/" % (no,q(mc),q(h),q(sc)))
out.append('')
for j,bis,dat,tit,txt,z,room,url in events:
    out.append("INSERT INTO car_event (jahr, jahr_bis, datierung, titel, text, zeit, room_id, quelle, quelle_url)\n"
               "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)\n/" % (j,n(bis),q(dat),q(tit),q(txt),n(z),
               'NULL' if room is None else "(SELECT room_id FROM car_room WHERE code = %s)" % q(room),
               q('Wikipedia' if 'wikipedia' in url else url.split('/')[2]), q(url)))
out.append('COMMIT\n/')
open('/home/claude/caracalla/db/02_inhalte.sql','w').write('\n'.join(out)+'\n')
print(len(rooms),len(quarries),len(stones),len(mats),len(events))
