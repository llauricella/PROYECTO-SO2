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
public class TreeNode<T> {
    private T data;
    private LinkedList<TreeNode<T>> children; // Usamos tu propia LinkedList

    public TreeNode(T data) {
        this.data = data;
        this.children = new LinkedList<>();
    }

    public void addChild(TreeNode<T> child) {
        this.getChildren().add(child);
    }

    // Getters & Setters
    /**
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * @return the children
     */
    public LinkedList<TreeNode<T>> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(LinkedList<TreeNode<T>> children) {
        this.children = children;
    }

}
