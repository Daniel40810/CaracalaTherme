-- =====================================================================
-- Caracalla-Thermen · Phase 8 · 99 Abbau
-- Entfernt alle Tabellen mit CAR_ und ihre Spatial-Metadaten. Nur CAR_, sonst nichts.
-- =====================================================================
BEGIN
  FOR t IN (SELECT table_name FROM user_tables
             WHERE table_name IN ('CAR_EVENT', 'CAR_VIEW', 'CAR_KEY', 'CAR_CLIP', 'CAR_TIMELINE',
                                  'CAR_MAT_STONE', 'CAR_STONE', 'CAR_QUARRY', 'CAR_ROOM')
             ORDER BY DECODE(table_name, 'CAR_EVENT', 1, 'CAR_VIEW', 2, 'CAR_KEY', 3, 'CAR_CLIP', 4, 'CAR_TIMELINE', 5,
                                         'CAR_MAT_STONE', 6, 'CAR_STONE', 7, 'CAR_QUARRY', 8, 9)) LOOP
    EXECUTE IMMEDIATE 'DROP TABLE ' || t.table_name || ' CASCADE CONSTRAINTS PURGE';
  END LOOP;
  DELETE FROM user_sdo_geom_metadata WHERE table_name LIKE 'CAR\_%' ESCAPE '\';
  COMMIT;
END;
/
