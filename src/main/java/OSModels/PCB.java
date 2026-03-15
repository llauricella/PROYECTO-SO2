/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package OSModels;

/**
 *
 * @author Luigi Lauricella & Sebastián González
 */
public class PCB {
    private int pid; // Process ID
    private ProcessState state;
    private String operation; // Ej: "READ", "CREATE", "UPDATE", "DELETE"
    private String fileName; // Archivo sobre el que opera
    private Object[] args;
    private int targetBlock;

    public PCB(int pid, String operation, String fileName, int targetBlock) {
        this.pid = pid;
        this.state = ProcessState.NEW;
        this.operation = operation;
        this.fileName = fileName;
        this.targetBlock = targetBlock;
        this.args = null;
    }

    // Getters & Setters
    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     * @return the state
     */
    public ProcessState getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(ProcessState state) {
        this.state = state;
    }

    /**
     * @return the operation
     */
    public String getOperation() {
        return operation;
    }

    /**
     * @param operation the operation to set
     */
    public void setOperation(String operation) {
        this.operation = operation;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public Object[] getArgs() { return args; }
    public void setArgs(Object[] args) { this.args = args; }
    public int getTargetBlock() { return targetBlock; }
    public void setTargetBlock(int targetBlock) { this.targetBlock = targetBlock; }
}
