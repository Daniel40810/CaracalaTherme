-- =====================================================================
-- Caracalla-Thermen · Phase 8 · 03 Regie
-- Die eingebauten Fahrten, der Rundgang und das Drehbuch, erzeugt von
-- com.dan.caracalla.tools.RegieExport aus Programme.java (EINGEBAUT = 'J').
-- =====================================================================

-- Rundumblick
INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)
VALUES ('RUNDUMBLICK', 'Rundumblick', 'FAHRT', NULL, 'N', 1, 'J')
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 1, 'J', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'RUNDUMBLICK'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 147.286, 95.435, 216.346, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.000, 186.227, 100.907, 162.263, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.000, 208.116, 105.435, 103.046, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.000, 213.372, 109.022, 43.623, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.000, 203.698, 111.729, -11.821, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.000, 181.656, 113.665, -60.117, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 18.000, 150.227, 114.973, -99.190, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 21.000, 112.417, 115.816, -127.973, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 24.000, 70.976, 116.359, -146.209, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 27.000, 28.247, 116.760, -154.196, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 30.000, -13.870, 117.149, -152.530, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 33.000, -53.837, 117.626, -141.915, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 36.000, -90.348, 118.247, -123.030, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 39.000, -122.171, 119.019, -96.514, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 42.000, -148.007, 119.906, -63.017, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 45.000, -166.405, 120.820, -23.342, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 48.000, -175.767, 121.637, 21.378, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 51.000, -174.443, 122.196, 69.492, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 54.000, -160.941, 122.317, 118.692, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 57.000, -134.216, 121.811, 165.952, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 60.000, -94.012, 120.497, 207.610, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 63.000, -41.189, 118.218, 239.595, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 66.000, 22.032, 114.858, 257.825, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 69.000, 91.979, 110.353, 258.711, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 72.000, 163.651, 104.705, 239.718, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDUMBLICK' AND c.pos = 1
/

-- Flug durch die Achse
INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)
VALUES ('ACHSE', 'Flug durch die Achse', 'FAHRT', NULL, 'N', 2, 'J')
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 1, 'J', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'ACHSE'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 0.000, 27.000, -112.000, 0.000, 6.000, -40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.000, 0.000, 25.000, -60.000, 0.000, 4.000, -36.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.000, 0.000, 8.000, -42.000, 0.000, 5.000, -22.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.500, 0.000, 5.000, -31.000, 0.000, 5.000, -12.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.500, 0.000, 5.000, -20.000, 0.000, 6.000, 0.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 19.500, 0.000, 6.500, -8.000, 0.000, 8.000, 10.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 23.500, 0.000, 6.500, 5.000, 0.000, 6.000, 20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 26.500, 0.000, 5.000, 15.500, 0.000, 5.000, 26.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 29.500, 0.000, 5.000, 22.500, 0.000, 5.500, 36.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 33.000, 0.000, 5.000, 30.500, 0.000, 7.000, 46.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 37.000, 0.000, 7.500, 42.000, 0.000, 20.000, 60.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 42.000, 0.000, 15.000, 49.000, 5.000, 38.000, 56.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 47.000, 0.000, 29.000, 50.000, 0.400, 45.000, 50.600 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'ACHSE' AND c.pos = 1
/

-- Aufstieg im Licht
INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)
VALUES ('AUFSTIEG', 'Aufstieg im Licht', 'FAHRT', NULL, 'N', 3, 'J')
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 1, 'J', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'AUFSTIEG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -9.867, 2.000, 46.409, 3.453, 7.000, 51.257 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 1.600, -8.179, 2.225, 43.948, 2.863, 7.725, 52.118 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.200, -5.928, 2.868, 42.133, 2.075, 8.868, 52.753 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 4.800, -3.336, 3.883, 41.078, 1.168, 10.383, 53.123 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.400, -0.642, 5.224, 40.822, 0.225, 12.224, 53.212 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 8.000, 1.921, 6.844, 41.335, -0.672, 14.344, 53.033 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.600, 4.145, 8.696, 42.522, -1.451, 16.696, 52.617 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.200, 5.866, 10.734, 44.235, -2.053, 19.234, 52.018 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.800, 6.975, 12.912, 46.291, -2.441, 21.912, 51.298 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.400, 7.423, 15.183, 48.490, -2.598, 24.683, 50.529 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.000, 7.222, 17.500, 50.632, -2.528, 27.500, 49.779 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 17.600, 6.443, 19.817, 52.538, -2.255, 30.317, 49.112 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 19.200, 5.201, 22.088, 54.063, -1.820, 33.088, 48.578 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 20.800, 3.644, 24.266, 55.109, -1.275, 35.766, 48.212 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.400, 1.937, 26.304, 55.626, -0.678, 38.304, 48.031 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 24.000, 0.245, 28.156, 55.620, -0.086, 40.656, 48.033 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 25.600, -1.282, 29.776, 55.143, 0.449, 42.776, 48.200 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 27.200, -2.525, 31.117, 54.287, 0.884, 44.500, 48.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 28.800, -3.401, 32.132, 53.171, 1.190, 44.500, 48.890 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 30.400, -3.871, 32.775, 51.930, 1.355, 44.500, 49.325 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 32.000, -3.939, 33.000, 50.695, 1.379, 44.500, 49.757 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'AUFSTIEG' AND c.pos = 1
/

-- Dem Wasser folgen
INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)
VALUES ('WASSERWEG', 'Dem Wasser folgen', 'FAHRT', NULL, 'J', 4, 'J')
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 1, 'J', NULL, NULL, NULL, 'Aqua Antoniniana', 'Ein eigener Zweig der Aqua Marcia bringt das Wasser auf Bögen heran, oben in einem gemauerten Kanal.' FROM car_timeline WHERE code = 'WASSERWEG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 470.000, 46.000, 262.000, 330.000, 10.000, 214.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 8.000, 330.000, 30.000, 246.000, 190.000, 10.000, 214.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.000, 180.000, 22.000, 238.000, 60.000, 9.000, 208.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.000, 96.000, 24.000, 232.000, 44.000, 6.000, 186.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 1
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 2, 'N', NULL, NULL, NULL, 'Zisternen und Bleirohre', 'Unter den Stufen des Stadions liegen die Zisternen mit 64 Kammern. Von dort laufen Bleirohre unter dem Garten zum Zentralbau.' FROM car_timeline WHERE code = 'WASSERWEG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 96.000, 24.000, 232.000, 44.000, 6.000, 186.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 7.000, 70.000, 30.000, 205.000, 24.000, 0.000, 160.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.000, 40.000, 26.000, 138.000, 4.000, 0.000, 96.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 20.000, 26.000, 18.000, 104.000, 2.000, 2.000, 74.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 2
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 3, 'N', NULL, NULL, NULL, 'Über dem Feuer', 'Das Wasser für die heißen Wannen läuft durch Kessel über den Praefurnien. Die Heißluft zieht unter den Boden und in die Wände.' FROM car_timeline WHERE code = 'WASSERWEG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 26.000, 18.000, 104.000, 2.000, 2.000, 74.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.000, 14.000, 7.500, 84.000, 0.000, 3.000, 71.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.000, 6.000, 6.200, 79.500, 0.000, 3.400, 71.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.000, 0.500, 8.500, 76.000, 0.000, 5.000, 60.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 21.000, 0.000, 8.000, 64.000, 0.000, 3.000, 52.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 27.000, -5.500, 4.400, 57.500, 0.000, 1.300, 50.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'WASSERWEG' AND c.pos = 3
/

-- Rundgang
INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)
VALUES ('RUNDGANG', 'Rundgang', 'RUNDGANG', NULL, 'N', 5, 'J')
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 1, 'J', NULL, NULL, NULL, 'Ankunft', 'Wer die Thermen betrat, legte zuerst in den Umkleiden ab und ging dann hinaus in eine der beiden Palästren.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -40.000, 32.000, -150.000, -60.000, 10.000, -40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.000, -76.000, 30.000, -64.000, -78.000, 8.000, -20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.000, -78.000, 26.000, -42.000, -80.000, 4.000, 0.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.500, -77.000, 9.000, -14.000, -72.000, 3.000, 20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.000, -68.500, 1.800, 26.000, -90.000, 6.000, -12.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 1
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 2, 'N', NULL, NULL, NULL, 'Die Palästra', 'Ein offener Hof mit Säulenhallen an drei Seiten. Hier wurde gelaufen, gerungen und Ball gespielt, bevor es in die heißen Säle ging.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -68.500, 1.800, 26.000, -90.000, 6.000, -12.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.200, -68.500, 1.800, 26.000, -90.000, 6.000, -12.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.000, -68.500, 1.800, 26.000, -80.000, 6.000, 30.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 2
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 3, 'N', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -68.500, 1.800, 26.000, -80.000, 6.000, 30.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.800, -67.600, 1.700, 27.200, -64.500, 2.000, 25.200 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.337, -64.500, 1.700, 25.200, -63.000, 2.000, 24.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.138, -63.000, 1.700, 24.000, -58.500, 2.000, 24.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.013, -58.500, 1.700, 24.000, -40.000, 2.000, 25.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.732, -40.000, 1.700, 25.000, -35.000, 2.000, 25.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.815, -35.000, 1.700, 25.000, -30.600, 2.000, 25.200 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.651, -30.600, 1.700, 25.200, -30.200, 2.000, 30.400 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 18.824, -30.200, 1.700, 30.400, -20.000, 2.000, 30.600 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 23.075, -20.000, 1.700, 30.600, -14.000, 2.000, 28.300 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 25.752, -14.000, 1.700, 28.300, -11.000, 2.000, 25.800 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 27.379, -11.000, 1.700, 25.800, -6.500, 2.000, 25.800 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 29.254, -6.500, 1.700, 25.800, -2.000, 2.000, 28.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 31.441, -2.000, 1.700, 28.500, 0.000, 2.000, 31.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 32.775, 0.000, 1.700, 31.000, -0.500, 2.000, 34.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 34.248, -0.500, 1.700, 34.500, -1.000, 2.000, 38.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 3
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 4, 'N', NULL, NULL, NULL, 'Das Caldarium', 'Der heiße Rundsaal, 35 Meter weit, mit einer Kuppel fast so groß wie die des Pantheons. Die Wannen liegen unter den großen Fenstern nach Südwesten.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -0.500, 1.700, 34.500, -1.000, 2.000, 38.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.200, -2.000, 1.800, 37.000, 6.000, 13.000, 66.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.000, -2.000, 1.800, 37.000, -14.000, 11.000, 60.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 4
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 5, 'N', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -2.000, 1.800, 37.000, -14.000, 11.000, 60.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 5
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.033, 0.000, 1.700, 33.000, 0.000, 2.000, 29.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 5
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.624, 0.000, 1.700, 29.500, 0.000, 2.000, 26.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 5
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 6, 'N', NULL, NULL, NULL, 'Das Tepidarium', 'Der lauwarme Saal zwischen heiß und kalt. Seitlich liegen zwei Becken mit lauwarmem Wasser.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 0.000, 1.700, 29.500, 0.000, 2.000, 26.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 6
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.200, 0.000, 1.800, 29.000, 0.000, 9.000, 19.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 6
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.000, 0.000, 1.800, 29.000, -9.000, 4.000, 25.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 6
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 7, 'N', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 0.000, 1.800, 29.000, -9.000, 4.000, 25.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.084, 0.000, 1.700, 24.000, 0.000, 2.000, 19.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 4.167, 0.000, 1.700, 19.000, 0.000, 2.000, 15.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.625, 0.000, 1.700, 15.500, 0.000, 2.000, 11.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 7.292, 0.000, 1.700, 11.500, 2.500, 2.000, 7.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.257, 2.500, 1.700, 7.500, 5.000, 2.000, 3.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 7
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 8, 'N', NULL, NULL, NULL, 'Das Frigidarium', 'Der kalte Saal, 59 mal 24 Meter, unter drei Kreuzgratgewölben auf acht Granitsäulen. In den Seitenräumen liegen die Kaltwasserbecken.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 2.500, 1.700, 7.500, 5.000, 2.000, 3.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.200, 4.000, 1.800, 6.000, -25.000, 12.000, -3.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.000, 4.000, 1.800, 6.000, 25.000, 13.000, -3.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 8
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 9, 'N', NULL, NULL, NULL, NULL, NULL FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 4.000, 1.800, 6.000, 25.000, 13.000, -3.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.493, 1.500, 1.700, -2.000, 0.000, 2.000, -9.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.475, 0.000, 1.700, -9.000, 0.000, 2.000, -15.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 8.975, 0.000, 1.700, -15.000, 0.000, 2.000, -21.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.475, 0.000, 1.700, -21.000, 0.000, 2.000, -25.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 13.350, 0.000, 1.700, -25.500, -1.500, 2.000, -27.600 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.426, -1.500, 1.700, -27.600, -9.000, 2.000, -27.650 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 17.551, -9.000, 1.700, -27.650, -20.000, 2.000, -27.650 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.134, -20.000, 1.700, -27.650, -31.000, 2.000, -27.650 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 9
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 10, 'N', NULL, NULL, NULL, 'Die Natatio', 'Das große Freibad, 54 mal 23 Meter, vor einer Schauwand mit drei Reihen von Statuennischen. Hier endete der Weg durch die Bäder.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -20.000, 1.700, -27.650, -31.000, 2.000, -27.650 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 10
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.200, -24.000, 2.000, -27.800, 16.000, 8.000, -52.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 10
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 13.000, -24.000, 2.000, -27.800, -6.000, 13.000, -52.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 10
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 11, 'N', NULL, NULL, NULL, 'Thermae Antoninianae', 'Eröffnet 216 n. Chr. unter Kaiser Caracalla. Bis zu 1600 Menschen badeten hier gleichzeitig.' FROM car_timeline WHERE code = 'RUNDGANG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -24.000, 2.000, -27.800, -6.000, 13.000, -52.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 11
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 4.500, -20.000, 13.000, -37.000, -5.000, 10.000, -20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 11
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.000, -8.000, 48.000, -95.000, 0.000, 10.000, 0.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 11
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.000, 45.000, 95.000, -185.000, 0.000, 12.000, 10.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'RUNDGANG' AND c.pos = 11
/

-- Ein Tag in den Thermen
INSERT INTO car_timeline (code, name, art, tag_im_jahr, wasserweg, reihenfolge, eingebaut)
VALUES ('DREHBUCH_TAG', 'Ein Tag in den Thermen', 'DREHBUCH', 80, 'N', 6, 'J')
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 1, 'J', 5.850, 6.600, 0.800, 'Rom, im Jahr 216', 'Kurz nach Sonnenaufgang über den Thermae Antoninianae, den Thermen des Caracalla.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 114.162, 146.858, 319.656, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.833, 128.018, 147.166, 305.772, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 1.667, 140.770, 147.332, 291.453, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.500, 152.473, 147.354, 276.873, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.333, 163.207, 147.240, 262.183, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 4.167, 173.068, 147.001, 247.517, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.000, 182.168, 146.654, 232.977, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.833, 190.627, 146.222, 218.643, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.667, 198.565, 145.725, 204.565, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 7.500, 206.105, 145.188, 190.766, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 8.333, 213.361, 144.629, 177.240, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.167, 220.433, 144.066, 163.960, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 10.000, 227.411, 143.511, 150.877, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 10.833, 234.361, 142.972, 137.921, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.667, 241.331, 142.448, 125.011, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.500, 248.343, 141.936, 112.055, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 13.333, 255.395, 141.426, 98.956, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.167, 262.457, 140.905, 85.615, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.000, 269.475, 140.356, 71.940, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.833, 276.371, 139.762, 57.846, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.667, 283.041, 139.105, 43.263, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 17.500, 289.363, 138.371, 28.138, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 18.333, 295.199, 137.549, 12.441, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 19.167, 300.400, 136.632, -3.834, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 20.000, 304.806, 135.620, -20.667, 0.000, 12.000, 6.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 1
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 2, 'J', 6.600, 7.600, 0.800, 'Morgennebel', 'Über der Natatio, dem großen Freibad, liegt noch der Nebel der Nacht.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 0.000, 17.000, -152.000, 0.000, 10.000, -60.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.000, 0.000, 20.000, -105.000, 0.000, 8.000, -50.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 2
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 18.000, 0.000, 26.000, -64.000, 0.000, 2.000, -38.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 2
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 3, 'J', 8.000, 10.500, 0.300, 'Durch die Achse', 'Natatio, Frigidarium, Tepidarium und Caldarium liegen hintereinander: vom kalten zum heißen Wasser.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 0.000, 27.000, -112.000, 0.000, 6.000, -40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.000, 0.000, 25.000, -60.000, 0.000, 4.000, -36.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.000, 0.000, 8.000, -42.000, 0.000, 5.000, -22.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.500, 0.000, 5.000, -31.000, 0.000, 5.000, -12.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.500, 0.000, 5.000, -20.000, 0.000, 6.000, 0.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 19.500, 0.000, 6.500, -8.000, 0.000, 8.000, 10.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 23.500, 0.000, 6.500, 5.000, 0.000, 6.000, 20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 26.500, 0.000, 5.000, 15.500, 0.000, 5.000, 26.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 29.500, 0.000, 5.000, 22.500, 0.000, 5.500, 36.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 33.000, 0.000, 5.000, 30.500, 0.000, 7.000, 46.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 37.000, 0.000, 7.500, 42.000, 0.000, 20.000, 60.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 42.000, 0.000, 15.000, 49.000, 5.000, 38.000, 56.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 47.000, 0.000, 29.000, 50.000, 0.400, 45.000, 50.600 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 3
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 4, 'J', 10.500, 12.500, 0.000, 'Das Caldarium', 'Unter der Kuppel sammelt sich der Dampf der heißen Wannen.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -9.867, 2.000, 46.409, 3.453, 7.000, 51.257 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 1.100, -8.179, 2.225, 43.948, 2.863, 7.725, 52.118 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.200, -5.928, 2.868, 42.133, 2.075, 8.868, 52.753 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.300, -3.336, 3.883, 41.078, 1.168, 10.383, 53.123 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 4.400, -0.642, 5.224, 40.822, 0.225, 12.224, 53.212 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.500, 1.921, 6.844, 41.335, -0.672, 14.344, 53.033 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.600, 4.145, 8.696, 42.522, -1.451, 16.696, 52.617 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 7.700, 5.866, 10.734, 44.235, -2.053, 19.234, 52.018 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 8.800, 6.975, 12.912, 46.291, -2.441, 21.912, 51.298 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.900, 7.423, 15.183, 48.490, -2.598, 24.683, 50.529 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.000, 7.222, 17.500, 50.632, -2.528, 27.500, 49.779 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 12.100, 6.443, 19.817, 52.538, -2.255, 30.317, 49.112 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 13.200, 5.201, 22.088, 54.063, -1.820, 33.088, 48.578 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.300, 3.644, 24.266, 55.109, -1.275, 35.766, 48.212 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.400, 1.937, 26.304, 55.626, -0.678, 38.304, 48.031 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.500, 0.245, 28.156, 55.620, -0.086, 40.656, 48.033 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 17.600, -1.282, 29.776, 55.143, 0.449, 42.776, 48.200 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 18.700, -2.525, 31.117, 54.287, 0.884, 44.500, 48.500 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 19.800, -3.401, 32.132, 53.171, 1.190, 44.500, 48.890 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 20.900, -3.871, 32.775, 51.930, 1.355, 44.500, 49.325 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.000, -3.939, 33.000, 50.695, 1.379, 44.500, 49.757 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 4
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 5, 'J', 12.500, 14.000, 0.000, 'Das Frigidarium', 'Drei Kreuzgratgewölbe auf acht Granitsäulen. Die Thermenfenster in den Lünetten tragen das Licht in den kalten Saal.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 24.000, 2.200, -5.000, -10.000, 10.000, 2.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 5
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.000, 4.000, 3.000, 0.000, -24.000, 13.000, -2.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 5
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.000, -18.000, 4.000, 5.000, -28.000, 15.000, -5.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 5
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 6, 'J', 14.000, 15.300, 0.000, 'Die Palästra', 'In den Säulenhallen und im offenen Hof trieb man Sport, bevor es in die heißen Säle ging.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -71.500, 1.800, -27.000, -88.000, 5.000, -8.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 6
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 10.000, -72.500, 2.000, -3.000, -90.000, 6.000, -2.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 6
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 20.000, -71.500, 2.200, 21.000, -88.000, 7.000, 4.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 6
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 7, 'J', 15.300, 16.300, 0.000, 'Nachmittag im Caldarium', 'Die großen Fenster öffnen sich nach Südwesten. Jetzt fällt die Sonne in den Dampf.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, -10.000, 2.000, 36.000, 6.000, 14.000, 66.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 13.000, -8.000, 2.600, 37.000, 7.000, 17.000, 65.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 7
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 26.000, -6.000, 3.200, 38.000, 8.000, 20.000, 64.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 7
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 8, 'J', 17.000, 18.400, 0.300, 'Abendlicht', 'Die Sonne sinkt hinter die Kuppel des Caldariums.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 60.643, 50.240, -65.038, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 1.083, 52.992, 50.744, -67.457, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 2.167, 45.296, 51.221, -69.354, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 3.250, 37.596, 51.664, -70.756, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 4.333, 29.929, 52.069, -71.695, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 5.417, 22.319, 52.430, -72.207, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 6.500, 14.788, 52.746, -72.322, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 7.583, 7.346, 53.014, -72.075, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 8.667, 0.000, 53.233, -71.492, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 9.750, -7.249, 53.404, -70.600, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 10.833, -14.405, 53.526, -69.417, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.917, -21.474, 53.598, -67.957, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 13.000, -28.464, 53.623, -66.228, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 14.083, -35.381, 53.598, -64.230, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 15.167, -42.233, 53.526, -61.960, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 16.250, -49.022, 53.404, -59.407, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 17.333, -55.746, 53.233, -56.555, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 18.417, -62.399, 53.014, -53.387, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 19.500, -68.967, 52.746, -49.880, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 20.583, -75.432, 52.430, -46.014, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 21.667, -81.767, 52.069, -41.767, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.750, -87.937, 51.664, -37.119, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 23.833, -93.904, 51.221, -32.055, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 24.917, -99.621, 50.744, -26.565, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 26.000, -105.038, 50.240, -20.643, 0.000, 20.000, 40.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 8
/
INSERT INTO car_clip (timeline_id, pos, harter_schnitt, stunde_von, stunde_bis, dunst, titel, text)
SELECT timeline_id, 9, 'J', 18.400, 19.200, 0.600, 'Um 537 n. Chr.', 'Im Krieg gegen die Goten werden die Wasserleitungen gekappt. Die Thermen fallen trocken und verfallen.' FROM car_timeline WHERE code = 'DREHBUCH_TAG'
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 0.000, 60.000, 40.000, 160.000, 0.000, 15.000, 20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 11.000, 100.000, 70.000, 240.000, 0.000, 14.000, 20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 9
/
INSERT INTO car_key (clip_id, t_s, eye_x, eye_y, eye_z, look_x, look_y, look_z)
SELECT c.clip_id, 22.000, 150.000, 110.000, 330.000, 0.000, 12.000, 20.000 FROM car_clip c JOIN car_timeline t ON t.timeline_id = c.timeline_id WHERE t.code = 'DREHBUCH_TAG' AND c.pos = 9
/

COMMIT
/
