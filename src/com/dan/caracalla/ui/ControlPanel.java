package com.dan.caracalla.ui;

import com.dan.caracalla.model.Room;
import com.dan.caracalla.regie.Programme;
import com.dan.caracalla.regie.Timeline;
import com.dan.caracalla.render.Sun;
import com.dan.fbutton.FButton;
import com.dan.fcheckbox.FCheckBox;
import com.dan.fcombobox.FComboBox;
import com.dan.fslider.FSlider;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/** Bedienfeld rechts: Ansicht, Sonne, Bild. */
public final class ControlPanel extends JPanel {
    static final Color BG = new Color(22, 23, 27), INK = new Color(226, 219, 205), MUTED = new Color(150, 144, 134),
            ACCENT = new Color(214, 170, 92);

    private final ScenePanel scene;
    private final JLabel timeLbl = new JLabel(), dayLbl = new JLabel();
    private FComboBox rooms;
    private final FSlider time = new FSlider(55, 205, 155), day = new FSlider(1, 365, 172);
    private boolean ready;

    @SuppressWarnings("unchecked")
    public ControlPanel(ScenePanel scene) {
        this.scene = scene;
        setBackground(BG);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        head("ANSICHT");
        rooms = new FComboBox(new Object[]{"Wird aufgebaut …"});
        rooms.setEnabled(false);
        rooms.addActionListener(e -> {
            if (!ready) return;
            Object o = rooms.getSelectedItem();
            if (o instanceof Room && scene.controller() != null) {
                scene.controller().goRoom((Room) o);
                scene.requestFocusInWindow();
            }
        });
        add(row(rooms));
        FButton out = new FButton("Außenansicht");
        out.addActionListener(e -> { if (scene.controller() != null) scene.controller().goOutside(); scene.requestFocusInWindow(); });
        add(row(out));
        FCheckBox orbit = new FCheckBox("Rundflug");
        orbit.addActionListener(e -> { if (scene.controller() != null) scene.controller().autoOrbit = orbit.isSelected(); scene.requestFocusInWindow(); });
        add(row(orbit));
        FCheckBox roof = new FCheckBox("Dächer und Gewölbe");
        roofBox = roof;
        roof.setSelected(true);
        roof.addActionListener(e -> { if (!syncing) scene.setRoof(roof.isSelected()); scene.requestFocusInWindow(); });
        add(row(roof));
        note("Ohne Dach schaut man von oben in die Säle. Das Licht fällt weiter so, als stünde das Dach.");

        gap();
        head("REGIE");
        flights = new FComboBox(Programme.flights().toArray());
        add(row(flights));
        FButton fly = new FButton("Kamerafahrt starten");
        fly.addActionListener(e -> { scene.play((Timeline) flights.getSelectedItem()); scene.requestFocusInWindow(); });
        add(row(fly));
        FButton tour = new FButton("Rundgang in der Badefolge");
        tour.addActionListener(e -> { scene.play(scene.tour()); scene.requestFocusInWindow(); });
        add(row(tour));
        FButton script = new FButton("Drehbuch abspielen");
        script.addActionListener(e -> { scene.play(scene.dayScript()); scene.requestFocusInWindow(); });
        add(row(script));
        note("Das Drehbuch „Ein Tag in den Thermen“ dauert knapp vier Minuten. Maus oder Taste übernimmt die Kamera an Ort und Stelle, Esc hält an.");

        gap();
        head("BLICKE");
        dbLbl.setForeground(MUTED);
        dbLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        add(row(dbLbl));
        views = new FComboBox(new Object[]{"Noch keine Blicke"});
        views.setEnabled(false);
        add(row(views));
        FButton showView = new FButton("Blick zeigen");
        showView.addActionListener(e -> {
            Object o = views.getSelectedItem();
            if (o instanceof com.dan.caracalla.db.CaracallaDb.View) scene.applyView((com.dan.caracalla.db.CaracallaDb.View) o);
            scene.requestFocusInWindow();
        });
        add(row(showView));
        FButton keep = new FButton("Blick merken");
        keep.addActionListener(e -> {
            String def = "Blick " + (scene.views().size() + 1);
            String name = ask("Name für diesen Blick (leer lassen für „" + def + "“):", "Blick merken", def);
            if (name != null) scene.saveView(name, m -> { dbLbl.setText(m); refreshViews(); });
            scene.requestFocusInWindow();
        });
        add(row(keep));
        FButton fromViews = new FButton("Fahrt aus allen Blicken");
        fromViews.addActionListener(e -> {
            String name = ask("Name der Fahrt (leer lassen für „Eigene Fahrt“):", "Fahrt aus allen Blicken", "Eigene Fahrt");
            if (name != null) scene.flightFromViews(name, m -> { dbLbl.setText(m); refreshFlights(); });
            scene.requestFocusInWindow();
        });
        add(row(fromViews));
        dbButtons = new FButton[]{showView, keep, fromViews};
        for (FButton b : dbButtons) b.setEnabled(false);
        note("Gemerkt werden Kamera, Tag, Uhrzeit, Wetter, Dach, Schnitt, Zeitregler, Wärmebild und Wasserweg. Aus den Blicken wird eine Fahrt, sieben Sekunden je Blick.");

        gap();
        head("ZUGABEN");
        FCheckBox cut = new FCheckBox("Schnitt mit Hypokaustum");
        cut.addActionListener(e -> { scene.setCut(cut.isSelected()); scene.requestFocusInWindow(); });
        add(row(cut));
        JLabel cutLbl = new JLabel("Schnittebene  x = 0 m");
        add(row(cutLbl));
        cutLbl.setForeground(INK);
        cutLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        FSlider cutPos = new FSlider(-60, 60, 0);
        cutPos.addChangeListener(e -> {
            scene.setCutX(cutPos.getValue());
            cutLbl.setText("Schnittebene  x = " + cutPos.getValue() + " m");
        });
        add(row(cutPos));
        FButton look = new FButton("Zum Schnitt blicken");
        look.addActionListener(e -> { scene.viewSection(); scene.requestFocusInWindow(); });
        add(row(look));
        note("Der Schnitt öffnet den Zentralbau. Unter den warmen Sälen glüht das Hypokaustum: Heißluft zieht vom Feuer zwischen den Ziegelpfeilern hindurch und in den Wänden hinauf.");
        JLabel ruinLbl = new JLabel("Zeit  ·  um 216 n. Chr.");
        ruinLbl.setForeground(INK);
        ruinLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(row(ruinLbl));
        FSlider ruin = new FSlider(0, 100, 0);
        ruin.addChangeListener(e -> {
            int v = ruin.getValue();
            ruinLbl.setText(v == 0 ? "Zeit  ·  um 216 n. Chr." : v == 100 ? "Zeit  ·  heute" : "Zeit  ·  Verfall " + v + " %");
            if (!syncing) scene.setRuin(v / 100f);
        });
        add(row(ruin));
        note("Links die Thermen in Betrieb, rechts die Ruine von heute: Gewölbe eingestürzt, Marmor fort, das Ziegelmauerwerk offen, Gras in den Sälen.");
        FCheckBox thermo = new FCheckBox("Wärmebild");
        thermo.addActionListener(e -> { scene.setThermo(thermo.isSelected()); scene.requestFocusInWindow(); });
        add(row(thermo));
        FCheckBox water = new FCheckBox("Wasserweg vom Aquädukt");
        water.addActionListener(e -> { scene.setWaterPath(water.isSelected()); scene.requestFocusInWindow(); });
        add(row(water));
        FCheckBox cards = new FCheckBox("Steckbrief per Klick auf Stein");
        cards.setSelected(true);
        cards.addActionListener(e -> { scene.setStoneCards(cards.isSelected()); scene.requestFocusInWindow(); });
        add(row(cards));
        FCheckBox sound = new FCheckBox("Klang mit Nachhall");
        sound.setSelected(true);
        sound.addActionListener(e -> { scene.setSound(sound.isSelected()); scene.requestFocusInWindow(); });
        add(row(sound));
        note("Plätschern, Tropfen und Feuer werden laufend erzeugt, ohne Tondateien. Unter der Kuppel klingt jeder Tropfen sechs Sekunden nach.");
        scene.setDbListener(this::dbReady);
        scene.setExtrasListener(() -> {
            syncing = true;
            roofBox.setSelected(scene.roof());
            if (weatherBox.getSelectedIndex() != (scene.haze() > 0.5 ? 1 : 0)) weatherBox.setSelectedIndex(scene.haze() > 0.5 ? 1 : 0);
            cut.setSelected(scene.cut());
            thermo.setSelected(scene.thermo());
            water.setSelected(scene.waterPath());
            sound.setSelected(scene.sound());
            int rv = Math.round(scene.ruinTarget() * 100);
            if (ruin.getValue() != rv) ruin.setValue(rv);
            syncing = false;
        });

        gap();
        head("SONNE ÜBER ROM");
        add(row(timeLbl));
        add(row(time));
        add(row(dayLbl));
        add(row(day));
        note("Wahre Ortszeit, wie sie eine Sonnenuhr zeigt. Das Caldarium öffnet sich nach Südwesten, zur Nachmittagssonne.");
        time.addChangeListener(e -> sunChanged());
        day.addChangeListener(e -> sunChanged());
        FComboBox lapse = new FComboBox(new Object[]{"Zeitraffer aus", "Zeitraffer · 5 min je s", "Zeitraffer · 20 min je s"});
        lapse.addActionListener(e -> {
            double[] v = {0, 5 / 60.0, 20 / 60.0};
            scene.setTimelapse(v[lapse.getSelectedIndex()]);
            scene.requestFocusInWindow();
        });
        add(row(lapse));
        FComboBox weather = new FComboBox(new Object[]{"Wetter · klar", "Wetter · dunstig"});
        weatherBox = weather;
        weather.addActionListener(e -> { if (!syncing) scene.setHaze(weather.getSelectedIndex() == 0 ? 0 : 1); scene.requestFocusInWindow(); });
        add(row(weather));
        scene.setDayListener(d -> {
            fromScene = true;
            day.setValue(d);
            dayLbl.setText("Tag  " + Sun.dateLabel(d));
            fromScene = false;
        });
        scene.setTimeListener(h -> {
            fromScene = true;
            time.setValue((int) Math.round(h * 10));
            timeLbl.setText("Uhrzeit  " + Sun.timeLabel(h));
            fromScene = false;
        });

        gap();
        head("BILD");
        FComboBox q = new FComboBox(new Object[]{"Qualität · Auto", "Schnell · 50 %", "Mittel · 75 %", "Hoch · 100 %"});
        q.setSelectedIndex(0);
        q.addActionListener(e -> {
            double[] s = {0, 0.5, 0.75, 1.0};
            int i = q.getSelectedIndex();
            if (i == 0) scene.setAutoQuality(); else scene.setScale(s[i]);
            scene.requestFocusInWindow();
        });
        add(row(q));
        note("Auto hält das Bild flüssig, solange sich die Kamera bewegt, und rechnet in voller Auflösung, sobald sie stillsteht.");
        FCheckBox rays = new FCheckBox("Lichtstrahlen in Dampf und Luft");
        rays.setSelected(true);
        rays.addActionListener(e -> { scene.setVolumetric(rays.isSelected()); scene.requestFocusInWindow(); });
        add(row(rays));
        FCheckBox steam = new FCheckBox("Dampf, Nebel und Flimmern");
        steam.setSelected(true);
        steam.addActionListener(e -> { scene.setSteam(steam.isSelected()); scene.requestFocusInWindow(); });
        add(row(steam));
        FButton still = new FButton("Standbild speichern");
        still.addActionListener(e -> { scene.requestStill(); scene.requestFocusInWindow(); });
        FButton cine = new FButton("Kinomodus");
        cine.addActionListener(e -> { scene.setCinema(true); scene.requestFocusInWindow(); });
        FCheckBox glow = new FCheckBox("Überstrahlen (Bloom)");
        glow.setSelected(true);
        glow.addActionListener(e -> { scene.setBloom(glow.isSelected()); scene.requestFocusInWindow(); });
        add(row(glow));
        add(row(still));
        add(row(cine));
        note("Das Standbild wird doppelt so groß gerechnet und landet unter Bilder/Caracalla. Im Kinomodus verschwinden Bedienfeld und Hinweise; Esc oder K holt sie zurück.");

        gap();
        head("TASTEN");
        note("F1 oder H zeigt alle Tasten. Die wichtigsten: 1 bis 5 Säle · 0 Übersicht · F5 Drehbuch · T Rundgang · X Schnitt · Z 216 oder heute · I Wärmebild · L Wasserweg · P Standbild · K Kinomodus · Esc anhalten");
        add(Box.createVerticalGlue());
        sunChanged();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(272, d.height);
    }

    /** Nach dem Laden: Säle in die Auswahl übernehmen. */
    @SuppressWarnings("unchecked")
    public void modelReady() {
        rooms.removeAllItems();
        for (Room r : scene.model().rooms) rooms.addItem(r);
        rooms.setEnabled(true);
        ready = true;
    }

    private boolean fromScene, syncing;
    private FComboBox flights, views, weatherBox;
    private FCheckBox roofBox;
    private FButton[] dbButtons = new FButton[0];
    private final JLabel dbLbl = new JLabel("Datenbank wird gesucht …");

    /** Fragt nach einem Namen im FStyle-Dialog; null bei Abbruch, leer ergibt den Vorschlag. */
    private String ask(String msg, String title, String def) {
        String s = com.dan.foptionpane.FOptionPane.showInputDialog(this, msg, title, com.dan.foptionpane.FOptionPane.QUESTION_MESSAGE);
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? def : s;
    }

    /** Nach dem Laden der Datenbank: Fahrten und Blicke übernehmen. */
    private void dbReady() {
        boolean ok = scene.dbReady();
        dbLbl.setText("<html><body style='width:185px'>" + scene.dbState() + "</body></html>");
        for (FButton b : dbButtons) b.setEnabled(ok);
        refreshFlights();
        refreshViews();
    }

    @SuppressWarnings("unchecked")
    private void refreshFlights() {
        Object sel = flights.getSelectedItem();
        flights.removeAllItems();
        for (Timeline t : scene.flights()) flights.addItem(t);
        if (sel != null) for (int i = 0; i < flights.getItemCount(); i++) if (flights.getItemAt(i).toString().equals(sel.toString())) flights.setSelectedIndex(i);
    }

    @SuppressWarnings("unchecked")
    private void refreshViews() {
        views.removeAllItems();
        java.util.List<com.dan.caracalla.db.CaracallaDb.View> l = scene.views();
        if (l.isEmpty()) { views.addItem("Noch keine Blicke"); views.setEnabled(false); return; }
        for (com.dan.caracalla.db.CaracallaDb.View v : l) views.addItem(v);
        views.setSelectedIndex(l.size() - 1);
        views.setEnabled(true);
    }

    private void sunChanged() {
        if (fromScene) return;
        double h = time.getValue() / 10.0;
        int d = day.getValue();
        timeLbl.setText("Uhrzeit  " + Sun.timeLabel(h));
        dayLbl.setText("Tag  " + Sun.dateLabel(d));
        scene.setSunTime(d, h);
    }

    private void head(String s) {
        JLabel l = new JLabel(s);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(ACCENT);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        add(row(l));
    }

    private void note(String s) {
        JLabel l = new JLabel("<html><body style='width:185px'>" + s + "</body></html>");
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l.setForeground(MUTED);
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));
        add(row(l));
    }

    private void gap() { add(Box.createVerticalStrut(14)); }

    private JComponent row(Component c) {
        if (c == timeLbl || c == dayLbl) {
            ((JLabel) c).setForeground(INK);
            ((JLabel) c).setFont(new Font("SansSerif", Font.PLAIN, 13));
        }
        if (c instanceof FCheckBox) ((FCheckBox) c).setTextColor(INK);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(c, BorderLayout.CENTER);
        p.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, p.getPreferredSize().height + 4));
        return p;
    }
}
