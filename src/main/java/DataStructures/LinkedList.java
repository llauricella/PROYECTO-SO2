/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStructures;

/**
 *
 * @author Luigi Lauricella
 * @param <T>
 */
public class LinkedList<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Add element at the end
    public void add(T data) {
        Node<T> newNode = new Node<>(data);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.setNext(newNode);
            tail = newNode;
        }
        size++;
    }

    // Get element by index
    public T get(int index) {
        if (index < 0 || index >= size) return null;
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.getNext();
        }
        return current.getData();
    }

    public int getSize() { return size; }
    public boolean isEmpty() { return size == 0; }
    
    // Remove element
    public boolean remove(T data) {
        if (head == null) return false;
        
        // Si es el primer elemento (head)
        if (head.getData().equals(data)) {
            head = head.getNext();
            if (head == null) {
                tail = null;
            }
            size--;
            return true;
        }
        
        // Buscar en el resto de la lista
        Node<T> current = head;
        while (current.getNext() != null) {
            if (current.getNext().getData().equals(data)) {
                current.setNext(current.getNext().getNext()); // Nos saltamos el nodo a borrar
                if (current.getNext() == null) {
                    tail = current; // Si borramos el último, actualizamos el tail
                }
                size--;
                return true;
            }
            current = current.getNext();
        }
        return false;
    }
}
