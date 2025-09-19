/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package labbinarios;

import java.io.File;

public class Cancion {
    public String nombre;
    public String artista;
    public long duracionSeg;
    public String genero;
    public File archivoAudio;
    public File archivoImagen;

    public Cancion(String nombre, String artista, long duracionSeg, String genero, File archivoAudio, File archivoImagen) {
        this.nombre = nombre;
        this.artista = artista;
        this.duracionSeg = duracionSeg;
        this.genero = genero;
        this.archivoAudio = archivoAudio;
        this.archivoImagen = archivoImagen;
    }

    public String texto() {
        return nombre + " - " + artista;
    }
}
