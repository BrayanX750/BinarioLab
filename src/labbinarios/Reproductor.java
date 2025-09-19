/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template  
 */
package labbinarios;

/**
 *
 * @author Usuario
 */
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javazoom.jl.converter.Converter;

public class Reproductor extends JFrame {
    private final Lista lista = new Lista();

    private JList<String> vista;
    private DefaultListModel<String> modelo;
    private final JLabel portada;
    private final JLabel info;
    private final JLabel tiempo;
    private final JSlider slider;
    private final JButton botonPlay;
    private final JButton botonStop;
    private final JButton botonPause;
    private final JButton botonAdd;
    private final JButton botonSelect;
    private final JButton botonRemove;

    private Cancion actual = null;
    private boolean pausado = false;
    

    private Clip clip;
    private long pauseAtMicros = 0;
    private final Map<String, File> wavCache = new HashMap<>();
    private final Timer timer;
    private boolean userDragging = false;

    private final Color bg = new Color(18, 18, 18);
    private final Color panel = new Color(24, 24, 24);
    private final Color text = new Color(245, 245, 245);
    private final Color sub = new Color(179, 179, 179);
    private final Color accent = new Color(29, 185, 84);

    private final File storeFile = new File(System.getProperty("user.home"), "reproductor_playlist.bin");

    public Reproductor() {
        setTitle("Reproductor");
        setSize(880, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bg);

        modelo = new DefaultListModel<>();
        vista = new JList<>(modelo);
        vista.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        vista.setBackground(panel);
        vista.setForeground(text);
        vista.setSelectionBackground(new Color(50, 50, 50));
        vista.setSelectionForeground(text);
        vista.setFixedCellHeight(28);
        vista.setBorder(new EmptyBorder(8, 10, 8, 10));
        vista.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setBackground(sel ? new Color(50, 50, 50) : panel);
                l.setForeground(sel ? text : sub);
                l.setFont(l.getFont().deriveFont(Font.PLAIN, 14f));
                return l;
            }
        });
        JScrollPane scroll = new JScrollPane(vista);
        scroll.getViewport().setBackground(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setPreferredSize(new Dimension(380, 0));

        portada = new JLabel("Sin imagen", SwingConstants.CENTER);
        portada.setPreferredSize(new Dimension(340, 340));
        portada.setOpaque(true);
        portada.setBackground(panel);
        portada.setForeground(sub);

        info = new JLabel("Sin canción", SwingConstants.CENTER);
        info.setOpaque(true);
        info.setBackground(panel);
        info.setForeground(text);
        info.setBorder(new EmptyBorder(10, 10, 6, 10));
        info.setFont(info.getFont().deriveFont(Font.PLAIN, 14f));

        tiempo = new JLabel("--:-- / --:--", SwingConstants.CENTER);
        tiempo.setOpaque(true);
        tiempo.setBackground(panel);
        tiempo.setForeground(sub);
        tiempo.setBorder(new EmptyBorder(4, 10, 6, 10));
        tiempo.setFont(tiempo.getFont().deriveFont(Font.BOLD, 13f));

        slider = new JSlider(0, 1000, 0);
        slider.setBackground(panel);
        slider.setForeground(accent);
        slider.setBorder(new EmptyBorder(0, 10, 10, 10));
        slider.setPaintTicks(false);
        slider.setPaintLabels(false);
        slider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) { userDragging = true; }
            @Override
            public void mouseReleased(MouseEvent e) { userDragging = false; seekFromSlider(); }
        });
        slider.addChangeListener(e -> { if (userDragging) tiempoPreview(); });

        JPanel infoBox = new JPanel(new GridLayout(3, 1, 0, 0));
        infoBox.setBackground(bg);
        infoBox.add(info);
        infoBox.add(tiempo);
        infoBox.add(slider);

        JPanel panelDerecha = new JPanel(new BorderLayout());
        panelDerecha.setBackground(bg);
        panelDerecha.add(portada, BorderLayout.CENTER);
        panelDerecha.add(infoBox, BorderLayout.SOUTH);

        botonPlay = crearBoton("Reproducir");
        botonStop = crearBoton("Detener");
        botonPause = crearBoton("Pausar");
        botonAdd = crearBoton("Agregar");
        botonSelect = crearBoton("Seleccionar");
        botonRemove = crearBoton("Borrar");

        JPanel barraBtns = new JPanel(new GridLayout(2, 3, 10, 10));
        barraBtns.setBorder(new EmptyBorder(12, 12, 12, 12));
        barraBtns.setBackground(bg);
        barraBtns.add(botonAdd);
        barraBtns.add(botonSelect);
        barraBtns.add(botonRemove);
        barraBtns.add(botonPlay);
        barraBtns.add(botonPause);
        barraBtns.add(botonStop);

        add(scroll, BorderLayout.WEST);
        add(panelDerecha, BorderLayout.CENTER);
        add(barraBtns, BorderLayout.SOUTH);

        botonAdd.addActionListener(e -> addSong());
        botonSelect.addActionListener(e -> selectSong());
        botonRemove.addActionListener(e -> removeSong());
        botonPlay.addActionListener(e -> playSong());
        botonPause.addActionListener(e -> pauseSong());
        botonStop.addActionListener(e -> stopSong());

        vista.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) selectSong();
            }
        });

        timer = new Timer(300, e -> actualizarTiempo());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveLibrary();
            }
        });

        loadLibrary();
    }

    private JButton crearBoton(String t) {
        JButton b = new JButton(t);
        b.setBackground(accent);
        b.setForeground(Color.black);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setFont(b.getFont().deriveFont(Font.BOLD, 13f));
        return b;
    }

    private boolean isSameSong(Cancion a, Cancion b) {
        return a != null && b != null && a.archivoAudio != null && b.archivoAudio != null
                && a.archivoAudio.equals(b.archivoAudio);
    }

    private void clearUI() {
        stopSong();
        actual = null;
        portada.setIcon(null);
        portada.setText("Sin imagen");
        info.setText("Sin canción");
        tiempo.setText("--:-- / --:--");
        slider.setValue(0);
        vista.clearSelection();
    }

    private void addSong() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File audio = fc.getSelectedFile();

        AddSongDialog dlg = new AddSongDialog(this, quitarExt(audio.getName()));
        dlg.setVisible(true);
        if (!dlg.ok) return;

        String nombre = dlg.fNombre.getText().trim();
        String artista = dlg.fArtista.getText().trim();
        String genero = dlg.fGenero.getText().trim();
        File imagen = dlg.imagenElegida;

        File wav = ensureWav(audio);
        long durSeg = 0;
        try {
            if (wav != null) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(wav);
                AudioFormat fmt = ais.getFormat();
                long frames = ais.getFrameLength();
                if (fmt.getFrameRate() > 0) durSeg = (long) Math.round(frames / fmt.getFrameRate());
                ais.close();
            }
        } catch (Exception ex) { }

        Cancion c = new Cancion(
                nombre.isEmpty() ? quitarExt(audio.getName()) : nombre,
                artista.isEmpty() ? "Unknown" : artista,
                Math.max(0, durSeg),
                genero.isEmpty() ? "General" : genero,
                audio,
                imagen
        );
        lista.add(c);
        modelo.addElement(c.texto());
    }

    private void selectSong() {
        int i = vista.getSelectedIndex();
        if (i < 0 || i >= lista.tamano()) return;
        Cancion nueva = lista.get(i);
        if (isSameSong(nueva, actual)) {
            mostrarInfo();
            return;
        }
        actual = nueva;
        pauseAtMicros = 0;
        stopClip();
        mostrarInfo();
        tiempo.setText("--:-- / --:--");
        slider.setValue(0);
    }

    private void removeSong() {
        int i = vista.getSelectedIndex();
        if (i < 0 || lista.tamano() == 0) return;
        Cancion aEliminar = lista.get(i);
        boolean esActual = isSameSong(aEliminar, actual);
        lista.remove(i);
        modelo.remove(i);
        if (esActual) clearUI();
    }

    private void playSong() {
        if (actual == null) {
            int i = vista.getSelectedIndex();
            if (i < 0 && lista.tamano() > 0) {
                vista.setSelectedIndex(0);
                i = 0;
            }
            if (i < 0) return;
            actual = lista.get(i);
            mostrarInfo();
        }
        File wav = ensureWav(actual.archivoAudio);
        if (wav == null) return;
        try {
            if (clip == null || !clip.isOpen()) {
                AudioInputStream ais = AudioSystem.getAudioInputStream(wav);
                clip = AudioSystem.getClip();
                clip.open(ais);
            }
            if (pausado && pauseAtMicros > 0) clip.setMicrosecondPosition(pauseAtMicros);
            clip.start();
            pausado = false;
            timer.start();
        } catch (Exception e) { }
    }

    private void pauseSong() {
        if (actual == null) return;
        if (clip == null || !clip.isOpen()) return;
        if (!pausado) {
            pauseAtMicros = clip.getMicrosecondPosition();
            clip.stop();
            pausado = true;
            timer.stop();
            actualizarTiempo();
        } else {
            clip.setMicrosecondPosition(Math.max(0, pauseAtMicros));
            clip.start();
            pausado = false;
            timer.start();
        }
    }

    private void stopSong() {
        pausado = false;
        pauseAtMicros = 0;
        stopClip();
        timer.stop();
        tiempo.setText("--:-- / --:--");
        slider.setValue(0);
    }

    private void stopClip() {
        try {
            if (clip != null) {
                clip.stop();
                clip.flush();
                clip.close();
            }
        } catch (Exception e) { }
        clip = null;
    }

    private void actualizarTiempo() {
        if (clip == null || !clip.isOpen()) return;
        long pos = clip.getMicrosecondPosition();
        long len = clip.getMicrosecondLength();
        tiempo.setText(formatearMicros(pos) + " / " + formatearMicros(len));
        if (len > 0 && !userDragging) {
            int val = (int) Math.min(1000, Math.max(0, (pos * 1000L) / len));
            slider.setValue(val);
        }
        if (!clip.isRunning() && !pausado && pos >= len) {
            timer.stop();
            slider.setValue(1000);
        }
    }

    private void seekFromSlider() {
        if (clip == null || !clip.isOpen()) return;
        long len = clip.getMicrosecondLength();
        int v = slider.getValue();
        long target = (len * v) / 1000L;
        pauseAtMicros = target;
        if (!pausado) {
            clip.setMicrosecondPosition(Math.max(0, target));
            clip.start();
        }
    }

    private void tiempoPreview() {
        if (clip == null || !clip.isOpen()) return;
        long len = clip.getMicrosecondLength();
        int v = slider.getValue();
        long target = (len * v) / 1000L;
        tiempo.setText(formatearMicros(target) + " / " + formatearMicros(len));
    }

    private File ensureWav(File mp3) {
        try {
            String key = mp3.getAbsolutePath();
            if (wavCache.containsKey(key) && wavCache.get(key).exists()) return wavCache.get(key);
            File tmpDir = new File(System.getProperty("java.io.tmpdir"), "repro_wav");
            if (!tmpDir.exists()) tmpDir.mkdirs();
            File wav = File.createTempFile("song_", ".wav", tmpDir);
            Converter conv = new Converter();
            conv.convert(mp3.getAbsolutePath(), wav.getAbsolutePath());
            wavCache.put(key, wav);
            return wav;
        } catch (Exception e) { return null; }
    }

    private void mostrarInfo() {
        if (actual == null) return;
        String dur = actual.duracionSeg <= 0 ? "" : " | " + formatearSeg(actual.duracionSeg);
        info.setText(actual.nombre + " | " + actual.artista + " | " + actual.genero + dur);
        if (actual.archivoImagen != null) {
            ImageIcon raw = new ImageIcon(actual.archivoImagen.getAbsolutePath());
            Image img = raw.getImage().getScaledInstance(340, 340, Image.SCALE_SMOOTH);
            portada.setIcon(new ImageIcon(img));
            portada.setText("");
        } else {
            portada.setIcon(null);
            portada.setText("Sin imagen");
        }
    }

    private String quitarExt(String n) {
        int p = n.lastIndexOf('.');
        if (p == -1) return n;
        return n.substring(0, p);
    }

    private String formatearSeg(long s) {
        long m = s / 60;
        long r = s % 60;
        return String.format("%02d:%02d", m, r);
    }

    private String formatearMicros(long micros) {
        long total = micros / 1_000_000L;
        long m = total / 60;
        long s = total % 60;
        return String.format("%02d:%02d", m, s);
    }

    private void writeString(RandomAccessFile raf, String s) throws IOException {
        if (s == null) s = "";
        byte[] b = s.getBytes("UTF-8");
        raf.writeInt(b.length);
        raf.write(b);
    }

    private String readString(RandomAccessFile raf) throws IOException {
        int len = raf.readInt();
        if (len < 0 || len > 10_000_000) throw new IOException("len");
        byte[] b = new byte[len];
        raf.readFully(b);
        return new String(b, "UTF-8");
    }

    private void saveLibrary() {
        try (RandomAccessFile raf = new RandomAccessFile(storeFile, "rw")) {
            raf.setLength(0);
            raf.writeInt(0x52504C59); 
            raf.writeShort(1);        
            raf.writeInt(lista.tamano());
            for (int i = 0; i < lista.tamano(); i++) {
                Cancion c = lista.get(i);
                writeString(raf, c.nombre);
                writeString(raf, c.artista);
                writeString(raf, c.genero);
                raf.writeLong(Math.max(0, c.duracionSeg));
                writeString(raf, c.archivoAudio == null ? "" : c.archivoAudio.getAbsolutePath());
                writeString(raf, c.archivoImagen == null ? "" : c.archivoImagen.getAbsolutePath());
            }
        } catch (Exception ignored) { }
    }

    private void loadLibrary() {
        if (!storeFile.exists()) return;
        try (RandomAccessFile raf = new RandomAccessFile(storeFile, "r")) {
            int magic = raf.readInt();
            short ver = raf.readShort();
            if (magic != 0x52504C59 || ver != 1) return;
            int n = raf.readInt();
            for (int i = 0; i < n; i++) {
                String nombre = readString(raf);
                String artista = readString(raf);
                String genero = readString(raf);
                long dur = raf.readLong();
                String audioPath = readString(raf);
                String imgPath = readString(raf);
                File audio = audioPath.isEmpty() ? null : new File(audioPath);
                File img = imgPath.isEmpty() ? null : new File(imgPath);
                if (audio == null || !audio.exists()) continue;
                if (img != null && !img.exists()) img = null;
                Cancion c = new Cancion(nombre, artista, dur, genero, audio, img);
                lista.add(c);
                modelo.addElement(c.texto());
            }
        } catch (Exception ignored) { }
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { }
        SwingUtilities.invokeLater(() -> new Reproductor().setVisible(true));
    }
}