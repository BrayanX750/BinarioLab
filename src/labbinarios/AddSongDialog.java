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
import java.awt.*;
import java.io.File;

public class AddSongDialog extends JDialog {
    JTextField fNombre;
    JTextField fArtista;
    JTextField fGenero;
    JLabel lPreview;
    JButton bImagen;
    JButton bOk;
    JButton bCancel;
    File imagenElegida;
    boolean ok = false;

    Color bg = new Color(24, 24, 24);
    Color text = new Color(245, 245, 245);
    Color sub = new Color(179, 179, 179);
    Color accent = new Color(29, 185, 84);

    AddSongDialog(Frame owner, String nombreSugerido) {
        super(owner, "Add song", true);
        setSize(520, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bg);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(bg);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;

        JLabel ln = new JLabel("Nombre");
        ln.setForeground(text);
        form.add(ln, c);
        c.gridx = 1;
        fNombre = new JTextField(nombreSugerido, 22);
        form.add(fNombre, c);

        c.gridx = 0; c.gridy++;
        JLabel la = new JLabel("Artista");
        la.setForeground(text);
        form.add(la, c);
        c.gridx = 1;
        fArtista = new JTextField("Desconocido", 22);
        form.add(fArtista, c);

        c.gridx = 0; c.gridy++;
        JLabel lg = new JLabel("Genero");
        lg.setForeground(text);
        form.add(lg, c);
        c.gridx = 1;
        fGenero = new JTextField("General", 22);
        form.add(fGenero, c);

        c.gridx = 0; c.gridy++;
        JLabel li = new JLabel("Cover");
        li.setForeground(text);
        form.add(li, c);
        c.gridx = 1;
        JPanel imgRow = new JPanel(new BorderLayout());
        imgRow.setBackground(bg);
        bImagen = new JButton("Escoger Imagen");
        bImagen.setBackground(accent);
        bImagen.setForeground(Color.black);
        imgRow.add(bImagen, BorderLayout.WEST);
        lPreview = new JLabel("Sin imagen", SwingConstants.CENTER);
        lPreview.setForeground(sub);
        imgRow.add(lPreview, BorderLayout.CENTER);
        form.add(imgRow, c);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acciones.setBackground(bg);
        bOk = new JButton("OK");
        bOk.setBackground(accent);
        bOk.setForeground(Color.black);
        bCancel = new JButton("Cancel");
        bCancel.setBackground(accent);
        bCancel.setForeground(Color.black);
        acciones.add(bCancel);
        acciones.add(bOk);

        add(form, BorderLayout.CENTER);
        add(acciones, BorderLayout.SOUTH);

        bImagen.addActionListener(e -> {
            JFileChooser fi = new JFileChooser();
            fi.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "jpeg"));
            if (fi.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imagenElegida = fi.getSelectedFile();
                ImageIcon raw = new ImageIcon(imagenElegida.getAbsolutePath());
                Image img = raw.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                lPreview.setText("");
                lPreview.setIcon(new ImageIcon(img));
            }
        });
        bOk.addActionListener(e -> {
            ok = true;
            setVisible(false);
        });
        bCancel.addActionListener(e -> {
            ok = false;
            setVisible(false);
        });
    }
}
