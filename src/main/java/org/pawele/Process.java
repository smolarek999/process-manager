package org.pawele;

public class Process {
    private final String id;
    private final Priority priority;

    public Process(Priority priority, String id) {
        this.id = id;
        this.priority = priority;
    }

    public String getId() {
        return id;
    }

    public Priority getPriority() {
        return priority;
    }

    void kill(){
        //TODO: implement me
    }
    public enum Priority{
        LOW,
        MEDIUM,
        HIGH;
        public boolean isMoreImportantThan(Priority other){
            return ordinal() > other.ordinal();
        }

    }
}
