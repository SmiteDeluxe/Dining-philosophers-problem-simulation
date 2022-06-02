import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;

import java.util.Random;

public class Philosoph extends Thread {
    Philosoph pLeft, pRight;
    boolean left =false, right=false;
    int keepRunning, option;
    String who;
    Monitor monitor;
    Circle circle;
    Label label;
    int round;
    final Object self;
    final Object onRight;
    private final Random rand = new Random();

    public Philosoph(String w, Monitor Monitor, int keepRunning, Circle circle, Label label, Object self, Object onRight, int option) {
        this.who =w;
        this.monitor = Monitor;
        this.keepRunning = keepRunning;
        this.circle = circle;
        this.label = label;
        this.round = 0;
        this.self =self;
        this.onRight = onRight;
        this.option = option;
    }
    public void assignPhilos(Philosoph l, Philosoph r) {
        this.pLeft=l;
        this.pRight=r;
    }
    public void run() {
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        while(keepRunning >0) {
            if(option ==0) {
                basic();
            } else if(option ==1) {
                noDeadStarve();
            } else {
                noDeadNoStarve(this.pRight);
            }
        }

        //after one of the algos has run
        Platform.runLater(() -> {
            label.setText("thinking");
            circle.setStyle("-fx-fill: red");
        });
    }

    /**
     * random picking up of forks
     */
    public void basic() {
        int random = (Math.abs(rand.nextInt())%100+1);
        if(random<50) {
            if (!pLeft.hasRightFork()) {
                monitor.waitLeftFork(this, circle, label);
                monitor.waitRightFork(this, circle, label);
            } else {
                monitor.waitRightFork(this, circle, label);
                monitor.waitLeftFork(this, circle, label);
            }
        } else {
            if(!pRight.hasLeftFork()) {
                monitor.waitRightFork(this, circle, label);
                monitor.waitLeftFork(this, circle, label);
            } else {
                monitor.waitLeftFork(this, circle, label);
                monitor.waitRightFork(this, circle, label);
            }
        }
        defaultStuff(1000);
        try {
            sleep(Math.abs(rand.nextInt())%1000+1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Every Thread gets both forks as soon as it can
     */
    public void noDeadStarve(){
        monitor.waitBothForks(this);
        defaultStuff(1000);
        try {
            sleep(Math.abs(rand.nextInt())%1000+1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Every Thread waits for its turn and then starts eating, always 2 at a time
     */
    public void noDeadNoStarve(Philosoph pr){
        synchronized (onRight) {
            // runs first
            if ((this.who.equals("1") || this.who.equals("         4"))&&this.round==0) {
                noDeadNoStarveDo();
                round++;
            } else if(this.round==0) {
                //runs if not first but still first round of process
                this.round++;
                try {
                    onRight.wait();
                    noDeadNoStarveDo();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                try {
                    // only has effect after eating and then notifies next Thread that it's done
                    synchronized (self){
                        self.notify();
                    }
                    // Thread waits for previous one to finish and then picks up forks
                    onRight.wait();
                    noDeadNoStarveDo();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
    // just picks up forks and holds + sleeps for a random amount of time (total of the too is fixed)
    public void noDeadNoStarveDo() {
            monitor.waitBothForks(this);
            int r = Math.abs(rand.nextInt(700)+100);
            defaultStuff(r);
            try {
                sleep(800 - r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }

    /**
     * default eating stuff
     * @param duration for how long to eat
     */
    public void defaultStuff(int duration) {
        Platform.runLater(() -> {
            label.setText("eating");
            circle.setStyle("-fx-fill: greenyellow;");
        });
        System.out.println(this.who+"\u001B[32m"+" Starts eating"+"\u001B[0m");
        try {
            sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        monitor.dropForks(this, circle, label);
    }
    public boolean hasLeftFork() {
        return left;
    }
    public boolean hasRightFork() {
        return right;
    }
    public boolean pickLeftFork() {
        if(!pLeft.hasRightFork()&&!this.hasLeftFork()){
            this.left=true;
            return true;
        } else {
            return false;
        }
    }
    public boolean pickRightFork() {
        if(!pRight.hasLeftFork()&&!this.hasRightFork()) {
            this.right=true;
            return true;
        } else {
            return false;
        }
    }
    public void dropLeftFork() {
        this.left=false;
    }
    public void dropRightFork() {
        this.right=false;
    }
    public boolean hasBothForks() {
        return this.hasLeftFork() && this.hasRightFork();
    }
    public void pickBothForks() {
        if(!pRight.hasLeftFork()&&!this.hasRightFork()) {
            this.right = true;
        }
        if(!pLeft.hasRightFork()&&!this.hasLeftFork()) {
            this.left = true;
        }
    }
    public void stopMe() {
        keepRunning = 0;
        this.dropLeftFork();
        this.dropRightFork();
    }
}
