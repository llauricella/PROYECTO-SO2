/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DataStructures;

/**
 *
 * @author Luigi Lauricella & Sebastián González
 * @param <T>
 */
public class Stack<T> {
    private Node<T> top;
    private int size;

    public Stack() {
        this.top = null;
        this.size = 0;
    }

    // Push (Agregar a la cima)
    public void push(T data) {
        Node<T> newNode = new Node<>(data);
        newNode.setNext(top);
        top = newNode;
        size++;
    }

    // Pop (Sacar de la cima)
    public T pop() {
        if (top == null) return null;
        T data = top.getData();
        top = top.getNext();
        size--;
        return data;
    }

    // Peek (Ver el elemento en la cima sin sacarlo)
    public T peek() {
        if (top == null) return null;
        return top.getData();
    }

    public boolean isEmpty() { return top == null; }
    public int getSize() { return size; }
}
