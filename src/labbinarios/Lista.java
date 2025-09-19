/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package labbinarios;

/**
 *
 * @author Usuario
 */
public class Lista {
    
public Nodo head;
    public int size; 

    public void add(Cancion c) {
        Nodo n = new Nodo(c);
        if (head == null) head = n;
        else {
            Nodo a = head;
            while (a.next != null) a = a.next;
            a.next = n;
        }
        size++;
    }

    public  Cancion get(int idx) {
        if (idx < 0) return null;
        int i = 0;
        Nodo a = head;
        while (a != null) {
            if (i == idx) return a.dato;
            a = a.next;
            i++;
        }
        return null;
    }

    public void remove(int idx) {
        if (idx < 0 || head == null) return;
        if (idx == 0) {
            head = head.next;
            size--;
            return;
        }
        int i = 0;
        Nodo a = head;
        while (a.next != null) {
            if (i + 1 == idx) {
                a.next = a.next.next;
                size--;
                return;
            }
            a = a.next;
            i++;
        }
    }

    public int tamano() {
        return size;
    }
}




