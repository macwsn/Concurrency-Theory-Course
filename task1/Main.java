import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.Random;

//1: NAIWNE
class NaiveSolution {
    static class Fork {
        private final int id;
        
        public Fork(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
    }
    
    static class Philosopher extends Thread {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        private final int meals;
        
        public Philosopher(int id, Fork leftFork, Fork rightFork, int meals) {
            this.id = id;
            this.leftFork = leftFork;
            this.rightFork = rightFork;
            this.meals = meals;
        }
        
        private void think() throws InterruptedException {
            System.out.println("Filozof " + id + " myśli");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        private void eat() throws InterruptedException {
            System.out.println("Filozof " + id + " je");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < meals; i++) {
                    think();
                    
                    synchronized (leftFork) {
                        System.out.println("Filozof " + id + " podnosi lewy widelec " + leftFork.getId());
                        
                        synchronized (rightFork) {
                            System.out.println("Filozof " + id + " podnosi prawy widelec " + rightFork.getId());
                            eat();
                            System.out.println("Filozof " + id + " odkłada prawy widelec " + rightFork.getId());
                        }
                        
                        System.out.println("Filozof " + id + " odkłada lewy widelec " + leftFork.getId());
                    }
                }
                System.out.println("Filozof " + id + " zakończył jedzenie");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void run() throws InterruptedException {
        int n = 5;
        Fork[] forks = new Fork[n];
        Philosopher[] philosophers = new Philosopher[n];
        
        for (int i = 0; i < n; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < n; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, leftFork, rightFork, (int) Math.pow(10, 6));
            philosophers[i].start();
        }
        
        for (Philosopher p : philosophers) {
            p.join();
        }
    }
}

//2: Z MOŻLIWOŚCIĄ ZAGŁODZENIA
class StarvationSolution {
    static class Fork {
        private final int id;
        private final ReentrantLock lock = new ReentrantLock();
        
        public Fork(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public boolean tryAcquire() {
            return lock.tryLock();
        }
        
        public void release() {
            lock.unlock();
        }
    }
    
    static class Philosopher extends Thread {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        private final int meals;
        
        public Philosopher(int id, Fork leftFork, Fork rightFork, int meals) {
            this.id = id;
            this.leftFork = leftFork;
            this.rightFork = rightFork;
            this.meals = meals;
        }
        
        private void think() throws InterruptedException {
            System.out.println("Filozof " + id + " myśli");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        private void eat() throws InterruptedException {
            System.out.println("Filozof " + id + " je");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < meals; i++) {
                    think();
                    
                    while (true) {
                        if (leftFork.tryAcquire()) {
                            if (rightFork.tryAcquire()) {
                                System.out.println("Filozof " + id + " podnosi oba widelce");
                                eat();
                                rightFork.release();
                                leftFork.release();
                                System.out.println("Filozof " + id + " odkłada oba widelce");
                                break;
                            } else {
                                leftFork.release();
                            }
                        }
                        Thread.sleep(10);
                    }
                }
                System.out.println("Filozof " + id + " zakończył jedzenie");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void run() throws InterruptedException {
        int n = 5;
        Fork[] forks = new Fork[n];
        Philosopher[] philosophers = new Philosopher[n];
        
        for (int i = 0; i < n; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < n; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, leftFork, rightFork, (int) Math.pow(10, 6));
            philosophers[i].start();
        }
        
        for (Philosopher p : philosophers) {
            p.join();
        }
    }
}

//3: ASYMETRYCZNE
class AsymmetricSolution {
    static class Fork {
        private final int id;
        private final Lock lock = new ReentrantLock();
        
        public Fork(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public void acquire() {
            lock.lock();
        }
        
        public void release() {
            lock.unlock();
        }
    }
    
    static class Philosopher extends Thread {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        private final int meals;
        
        public Philosopher(int id, Fork leftFork, Fork rightFork, int meals) {
            this.id = id;
            this.leftFork = leftFork;
            this.rightFork = rightFork;
            this.meals = meals;
        }
        
        private void think() throws InterruptedException {
            System.out.println("Filozof " + id + " myśli");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        private void eat() throws InterruptedException {
            System.out.println("Filozof " + id + " je");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < meals; i++) {
                    think();
                    if (id % 2 == 0) {
                        rightFork.acquire();
                        System.out.println("Filozof " + id + " (parzysty) podnosi prawy widelec " + rightFork.getId());
                        leftFork.acquire();
                        System.out.println("Filozof " + id + " (parzysty) podnosi lewy widelec " + leftFork.getId());
                    } else {
                        leftFork.acquire();
                        System.out.println("Filozof " + id + " (nieparzysty) podnosi lewy widelec " + leftFork.getId());
                        rightFork.acquire();
                        System.out.println("Filozof " + id + " (nieparzysty) podnosi prawy widelec " + rightFork.getId());
                    }
                    
                    eat();
                    
                    leftFork.release();
                    rightFork.release();
                    System.out.println("Filozof " + id + " odkłada widelce");
                }
                System.out.println("Filozof " + id + " zakończył jedzenie");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void run() throws InterruptedException {
        
        int n = 5;
        Fork[] forks = new Fork[n];
        Philosopher[] philosophers = new Philosopher[n];
        
        for (int i = 0; i < n; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < n; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, leftFork, rightFork, (int) Math.pow(10, 6));
            philosophers[i].start();
        }
        
        for (Philosopher p : philosophers) {
            p.join();
        }
    }
}

//4: STOCHASTYCZNE
class StochasticSolution {
    static class Fork {
        private final int id;
        private final AtomicBoolean available = new AtomicBoolean(true);
        
        public Fork(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public boolean tryAcquire() {
            return available.compareAndSet(true, false);
        }
        
        public void release() {
            available.set(true);
        }
    }
    
    static class Philosopher extends Thread {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        private final int meals;
        private final Random random = new Random();
        
        public Philosopher(int id, Fork leftFork, Fork rightFork, int meals) {
            this.id = id;
            this.leftFork = leftFork;
            this.rightFork = rightFork;
            this.meals = meals;
        }
        
        private void think() throws InterruptedException {
            System.out.println("Filozof " + id + " myśli");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        private void eat() throws InterruptedException {
            System.out.println("Filozof " + id + " je");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < meals; i++) {
                    think();
                    
                    boolean leftFirst = random.nextBoolean();
                    Fork first = leftFirst ? leftFork : rightFork;
                    Fork second = leftFirst ? rightFork : leftFork;
                    
                    System.out.println("Filozof " + id + " rzuca monetą: " + (leftFirst ? "LEWY pierwszy" : "PRAWY pierwszy"));
                    
                    while (true) {
                        if (first.tryAcquire()) {
                            System.out.println("Filozof " + id + " podnosi pierwszy widelec " + first.getId());
                            if (second.tryAcquire()) {
                                System.out.println("Filozof " + id + " podnosi drugi widelec " + second.getId());
                                eat();
                                second.release();
                                first.release();
                                System.out.println("Filozof " + id + " odkłada widelce");
                                break;
                            } else {
                                first.release();
                            }
                        }
                        Thread.sleep(10);
                    }
                }
                System.out.println("Filozof " + id + " zakończył jedzenie");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void run() throws InterruptedException {
        int n = 5;
        Fork[] forks = new Fork[n];
        Philosopher[] philosophers = new Philosopher[n];
        
        for (int i = 0; i < n; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < n; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, leftFork, rightFork, (int) Math.pow(10, 6));
            philosophers[i].start();
        }
        
        for (Philosopher p : philosophers) {
            p.join();
        }
    }
}

//5: Z ARBITREM
class ArbiterSolution {
    static class Fork {
        private final int id;
        
        public Fork(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
    }
    
    static class Philosopher extends Thread {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        private final int meals;
        private final Semaphore arbiter;
        
        public Philosopher(int id, Fork leftFork, Fork rightFork, int meals, Semaphore arbiter) {
            this.id = id;
            this.leftFork = leftFork;
            this.rightFork = rightFork;
            this.meals = meals;
            this.arbiter = arbiter;
        }
        
        private void think() throws InterruptedException {
            System.out.println("Filozof " + id + " myśli");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        private void eat() throws InterruptedException {
            System.out.println("Filozof " + id + " je");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < meals; i++) {
                    think();
                    
                    arbiter.acquire();
                    System.out.println("Filozof " + id + " otrzymał pozwolenie od arbitra");
                    
                    synchronized (leftFork) {
                        System.out.println("Filozof " + id + " podnosi lewy widelec " + leftFork.getId());
                        
                        synchronized (rightFork) {
                            System.out.println("Filozof " + id + " podnosi prawy widelec " + rightFork.getId());
                            eat();
                            System.out.println("Filozof " + id + " odkłada widelce");
                        }
                    }
                    
                    arbiter.release();
                    System.out.println("Filozof " + id + " zwraca pozwolenie arbitrowi");
                }
                System.out.println("Filozof " + id + " zakończył jedzenie");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void run() throws InterruptedException {
        int n = 5;
        Fork[] forks = new Fork[n];
        Philosopher[] philosophers = new Philosopher[n];
        Semaphore arbiter = new Semaphore(n - 1); // Maksymalnie n-1 filozofów
        
        for (int i = 0; i < n; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < n; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, leftFork, rightFork, (int) Math.pow(10, 6), arbiter);
            philosophers[i].start();
        }
        
        for (Philosopher p : philosophers) {
            p.join();
        }
    }
}

//6: Z JADALNIĄ
class DiningRoomSolution {
    static class Fork {
        private final int id;
        
        public Fork(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
    }
    
    static class Philosopher extends Thread {
        private final int id;
        private final Fork leftFork;
        private final Fork rightFork;
        private final int meals;
        private final Semaphore diningRoom;
        
        public Philosopher(int id, Fork leftFork, Fork rightFork, int meals, Semaphore diningRoom) {
            this.id = id;
            this.leftFork = leftFork;
            this.rightFork = rightFork;
            this.meals = meals;
            this.diningRoom = diningRoom;
        }
        
        private void think() throws InterruptedException {
            System.out.println("Filozof " + id + " myśli");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        private void eat() throws InterruptedException {
            System.out.println("Filozof " + id + " je");
            Thread.sleep(ThreadLocalRandom.current().nextInt(100, 300));
        }
        
        @Override
        public void run() {
            try {
                for (int i = 0; i < meals; i++) {
                    think();
                    
                    if (diningRoom.tryAcquire()) {
                        System.out.println("Filozof " + id + " wchodzi do jadalni");
                        
                        synchronized (leftFork) {
                            System.out.println("Filozof " + id + " (w jadalni) podnosi lewy widelec " + leftFork.getId());
                            
                            synchronized (rightFork) {
                                System.out.println("Filozof " + id + " (w jadalni) podnosi prawy widelec " + rightFork.getId());
                                eat();
                                System.out.println("Filozof " + id + " (w jadalni) odkłada widelce");
                            }
                        }
                        
                        diningRoom.release();
                        System.out.println("Filozof " + id + " opuszcza jadalnię");
                    } else {
                        System.out.println("Filozof " + id + " je na korytarzu (odwrotna kolejność)");
                        
                        synchronized (rightFork) {
                            System.out.println("Filozof " + id + " (korytarz) podnosi prawy widelec " + rightFork.getId());
                            
                            synchronized (leftFork) {
                                System.out.println("Filozof " + id + " (korytarz) podnosi lewy widelec " + leftFork.getId());
                                eat();
                                System.out.println("Filozof " + id + " (korytarz) odkłada widelce");
                            }
                        }
                    }
                }
                System.out.println("Filozof " + id + " zakończył jedzenie");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void run() throws InterruptedException {
        int n = 5;
        Fork[] forks = new Fork[n];
        Philosopher[] philosophers = new Philosopher[n];
        Semaphore diningRoom = new Semaphore(n - 1);
        
        for (int i = 0; i < n; i++) {
            forks[i] = new Fork(i);
        }
        
        for (int i = 0; i < n; i++) {
            Fork leftFork = forks[i];
            Fork rightFork = forks[(i + 1) % n];
            philosophers[i] = new Philosopher(i, leftFork, rightFork, (int) Math.pow(10, 6), diningRoom);
            philosophers[i].start();
        }
        
        for (Philosopher p : philosophers) {
            p.join();
        }
    }
}
// Main
class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Wybierz rozwiązanie (1-6):");
        System.out.println("1. Naiwne");
        System.out.println("2. Z możliwością zagłodzenia)");
        System.out.println("3. Asymetryczne");
        System.out.println("4. Stochastyczne");
        System.out.println("5. Z arbitrem ");
        System.out.println("6. Z jadalnią");
        
        int choice = 6;// WYBÓR ROZWIĄZANIA
        
        switch (choice) {
            case 1:
                NaiveSolution.run();
                break;
            case 2:
                StarvationSolution.run();
                break;
            case 3:
                AsymmetricSolution.run();
                break;
            case 4:
                StochasticSolution.run();
                break;
            case 5:
                ArbiterSolution.run();
                break;
            case 6:
                DiningRoomSolution.run();
                break;
            default:
                System.out.println("Wybierz rozwiązanie (1-6):");
        }
    }
}