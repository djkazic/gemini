package net.listeners;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReplyThread implements Runnable {
	
	private ArrayList<Runnable> runnables = new ArrayList<Runnable> ();
	private ExecutorService pool = Executors.newCachedThreadPool();
	
	public void run() {
		while(true) {
			if(runnables.size() > 0) {
				pool.execute(runnables.remove(0));
			} else {
				try {
					Thread.sleep(10);
				} catch(InterruptedException e) {}
			}
		}
	}
	
	public void queue(Runnable run) {
		runnables.add(run);
	}
}
