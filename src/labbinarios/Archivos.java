/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package labbinarios;

/**
 *
 * @author Usuario
 */
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.DefaultListModel;

public class Archivos {
    private final File archivoPlaylist;
    private final File legacyHomeBin;
    private final File legacyProjectBin;

    public Archivos() {
        File baseDir = new File(System.getProperty("user.dir"), "data");
        if (!baseDir.exists()) baseDir.mkdirs();
        this.archivoPlaylist = new File(baseDir, "playlist.rpl");
        this.legacyHomeBin = new File(System.getProperty("user.home"), "reproductor_playlist.fnf");
        this.legacyProjectBin = new File(System.getProperty("user.dir"), "reproductor_playlist.fnf");
    }

    public Archivos(String rutaCompleta) {
        this.archivoPlaylist = new File(rutaCompleta);
        this.legacyHomeBin = new File(System.getProperty("user.home"), "reproductor_playlist.fnf");
        this.legacyProjectBin = new File(System.getProperty("user.dir"), "reproductor_playlist.fnf");
    }

    public void guardar(Lista lista) {
        try (RandomAccessFile raf = new RandomAccessFile(archivoPlaylist, "rw")) {
            raf.setLength(0);
            raf.writeInt(0x52504C59);
            raf.writeShort(1);
            raf.writeInt(lista.tamano());
            for (int i = 0; i < lista.tamano(); i++) {
                Cancion c = lista.get(i);
                escribirCadena(raf, c.nombre);
                escribirCadena(raf, c.artista);
                escribirCadena(raf, c.genero);
                raf.writeLong(Math.max(0, c.duracionSeg));
                escribirCadena(raf, c.archivoAudio == null ? "" : c.archivoAudio.getAbsolutePath());
                escribirCadena(raf, c.archivoImagen == null ? "" : c.archivoImagen.getAbsolutePath());
            }
        } catch (Exception ignored) { }
        ocultarEnWindows(archivoPlaylist.toPath());
    }

    public void cargar(Lista lista, DefaultListModel<String> modelo) {
        File fuente = archivoPlaylist.exists() ? archivoPlaylist
                      : (legacyProjectBin.exists() ? legacyProjectBin
                      : (legacyHomeBin.exists() ? legacyHomeBin : null));
        if (fuente == null) return;
        try (RandomAccessFile raf = new RandomAccessFile(fuente, "r")) {
            int magic = raf.readInt();
            short ver = raf.readShort();
            if (magic != 0x52504C59 || ver != 1) return;
            int n = raf.readInt();
            for (int i = 0; i < n; i++) {
                String nombre = leerCadena(raf);
                String artista = leerCadena(raf);
                String genero = leerCadena(raf);
                long dur = raf.readLong();
                String audioPath = leerCadena(raf);
                String imgPath = leerCadena(raf);
                File audio = audioPath.isEmpty() ? null : new File(audioPath);
                File img = imgPath.isEmpty() ? null : new File(imgPath);
                if (audio == null || !audio.exists()) continue;
                if (img != null && !img.exists()) img = null;
                Cancion c = new Cancion(nombre, artista, dur, genero, audio, img);
                lista.add(c);
                if (modelo != null) modelo.addElement(c.texto());
            }
        } catch (Exception ignored) { }
    }

    private void escribirCadena(RandomAccessFile raf, String s) throws IOException {
        if (s == null) s = "";
        byte[] b = s.getBytes("UTF-8");
        raf.writeInt(b.length);
        raf.write(b);
    }

    private String leerCadena(RandomAccessFile raf) throws IOException {
        int len = raf.readInt();
        if (len < 0 || len > 10_000_000) throw new IOException("len");
        byte[] b = new byte[len];
        raf.readFully(b);
        return new String(b, "UTF-8");
    }

    private void ocultarEnWindows(Path p) {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                if (Files.exists(p) && !Files.getAttribute(p, "dos:hidden").equals(Boolean.TRUE)) {
                    Files.setAttribute(p, "dos:hidden", Boolean.TRUE);
                }
            }
        } catch (Exception ignored) { }
    }

    public File getArchivoPlaylist() {
        return archivoPlaylist;
    }
}
