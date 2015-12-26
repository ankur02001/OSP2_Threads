

//IMPLEMENTED FIrst Come First Serve SCHEDULING ALGORITHM

//	This class is used to Implement most common operations on threads
//	Parallel Processing in terms of threads executing in parallel and doing different sort of works
//	Program Structuring as modular means of structuring application via threads to perform mulitple independent actvities
//	Foreground Vs Background to give a pipelining effect and increased execution
//	Asynchronous Activity used as periodic backups
/*
 * =============================================================
 * 						FCFS READINGS
 * ==============================================================
 * Begin Time            : 1445450305967 
 * End Time              : 1445450312323
 * difference            : 6356 ms or 6.356 s
 * Total Number of Tasks : 10
 * 
 * 
 * Throughput =  Number of jobs/ tasks processed per unit time
 * Throughput =  (10/6.356) = 1.57 jobs /sec
 * 
 * 
 */
package osp.Threads;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;
import java.util.Enumeration;

import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;

public class ThreadCB extends IflThreadCB 
{
    static GenericList readyQueue;
    static boolean flag1 = false;
    static boolean flag2 = false;
    static long beginTime = 0;
    static long endTime = 0;
    static int taskID = 0;
    static int count =0 ;
    static HashSet<Integer> taskIDs = new HashSet<Integer>();

	//Response time
	static HashMap<Integer,Long> threadTime=new HashMap<Integer,Long>();
	static HashMap<Integer,Boolean> check=new HashMap<Integer,Boolean>();
    static int totalTask=0;
	static int numberOfTask=0;

	public ThreadCB()
    {
	super();
	System.out.println("This is under the constructor call");
    }


	public static void calculateTime(int current, long time)
	{
		if(threadTime.containsKey(current))
		{
			long startTime=threadTime.get(current);
		//	MyOut.print(current, "Creation Time:" + startTime + " ID:"+current);
		//	long total=TimeUnit.NANOSECONDS.toMillis(time)-startTime;
			long total=time-startTime;
			MyOut.print(current, "Response Time:" + total + " ID:"+current);
			threadTime.put(current,total);
		}
		else
		{		
			//threadTime.put(current,TimeUnit.NANOSECONDS.toMillis(time));
			
		//	MyOut.print(current, "CREATION TIME:" + TimeUnit.NANOSECONDS.toMillis(time)+ " ID:"+current);
			threadTime.put(current,time);
			
		//	MyOut.print(current, "CREATION TIME:" + time+ " ID:"+current);
		}
	}


	public static boolean ifCalculated(int currentID)
	{
		if(check.containsKey(currentID))
		{		
			check.put(currentID,true);
			return true;
		}
		else
		{		
			check.put(currentID,false);
		}
		
	  return false;
	}


    public static void init()
    {
	System.out.println("In init");
        readyQueue = new GenericList();
    }

    static public ThreadCB do_create(TaskCB task)
    {
    	
    	if(!taskIDs.contains(task.getID())){
    		count++;
			totalTask++;
			numberOfTask++;
    		taskIDs.add(task.getID());	
    	}
    	if(!flag1){
    		flag1 = true;
    		beginTime = System.currentTimeMillis();;
    		MyOut.print("osp.Threads.ThreadCB","##### Begin Time :"+beginTime+"####");
    		
    	}
    	
	if(task == null)
    	{
    		ThreadCB.dispatch();
    		return null;
    	}
        else if(task.getThreadCount() == MaxThreadsPerTask)				// Check if thread Creation is permitted or not
    	{
    		System.out.println("Cannot create because it already has maximum number of threads");
    		ThreadCB.dispatch();						//dispatcher should be called else warning will be recieved
    		return null;		
    	}
        else if(task.getThreadCount() > MaxThreadsPerTask)				// Check if thread Creation is permitted or not
    	{
    		System.out.println("Cannot create because it already has maximum number of threads");
    		ThreadCB.dispatch();						//dispatcher should be called else warning will be recieved
    		return null;		
    	}
	
	else
	{
		ThreadCB thread= new ThreadCB();					//creation of thread Object	
		if(task.addThread(thread) == FAILURE) 				//addThread() is used to link a thread to its task
		{	
			System.out.println("Failed");	
			ThreadCB.dispatch();
			return null;			
		}		
		else
		{
			System.out.println("Creating a new Thread");
			thread.setTask(task);					//setting the thread's task
			thread.setPriority(0);			//getting, querying and setting the priority of task & Thread
			thread.setStatus(ThreadReady);				//setting the status to ThreadReady
			
		//	calculateTime(thread,thread.getCreationTime());
			calculateTime(thread.getID(),System.nanoTime());

			readyQueue.insert(thread);				//thread placed in the readyQueue
			ThreadCB.dispatch();					//dispatcher should be called else warning will be recieved
			return thread;
		}	

	}

    }

    public void do_kill()
    {
	
        TaskCB theTask = null;
        
        // Handle the ThreadReady case
        if (this.getStatus() == ThreadReady) {
            readyQueue.remove(this);
            this.setStatus(ThreadKill);
        }
        // Handle the ThreadRunning case
        else if (this.getStatus() == ThreadRunning) {
            // Do the right stuff when we find out it is the thread
            // that we want to kill
        	//*****************************************************
            if (MMU.getPTBR().getTask().getCurrentThread() == this) {
                MMU.setPTBR(null);
                getTask().setCurrentThread(null);
            }
        }
        else{
        	
        	 // Make sure IO resources are released
            for (int i = 0; i < Device.getTableSize(); i++) {
                Device.get(i).cancelPendingIO(this);
            } 
        }
        
        
        // Handle the ThreadWaiting case
            this.setStatus(ThreadKill);
            
            
         ResourceCB.giveupResources(this);

        
        // Get the associated task and kill thread
        theTask = this.getTask();
        theTask.removeThread(this);

       
        
        // Kill the task if no remaining threads
        // Sad, but an extra task without threads
        // is just a it wasteful ;-)
        if (this.getTask().getThreadCount() == 0) {
        	{
            	MyOut.print("osp.Threads.ThreadCB","$$$$"+count+"$$$");
            	endTime = System.currentTimeMillis();
            	MyOut.print("osp.Threads.ThreadCB","##### End Time :"+endTime+"####");
            	this.getTask().kill();
				numberOfTask--;
			if(totalTask > 2)
			{
				double diff=(double)(endTime-beginTime)/1000;
				double throughput= (double)totalTask/diff;
				MyOut.print("osp.Threads.ThreadCB","Throughput is:"+throughput+"tasks/sec");
			}

            }
            
        }
            
    ThreadCB.dispatch();
    }

    public void do_suspend(Event event)
    {
     	if(this.getStatus() == ThreadRunning)					//if status is ThreadRunning then suspend to ThreadWaiting
    	{
		System.out.println("Suspending a Running Thread to ThreadWaiting");
    		this.setStatus(ThreadWaiting);					// setting status as ThreadWaiting
    		MMU.setPTBR(null);						
    		this.getTask().setCurrentThread(null);
    	}
    	else if (this.getStatus() >= ThreadWaiting)					// Increasing suspension level
	{
		System.out.println("Suspending an already waiting Thread");    	        
		setStatus(this.getStatus()+1);
	} 
	if(!event.contains(this))
    	event.addThread(this);   		
	
    	ThreadCB.dispatch();							// New thread to be dispatched

    	System.out.println("Do kill finished");
    }

    public void do_resume()
    {
		/*if(this.getStatus() < ThreadWaiting) {
            return;
        }*/
        // Set the thread's status
        if(this.getStatus() == ThreadWaiting) {
            this.setStatus(ThreadReady);
            readyQueue.insert(this);
        } else if (this.getStatus() > ThreadWaiting) {
            this.setStatus(getStatus()-1);
        }
        
        ThreadCB.dispatch(); // dispatch a thread

    }
	
    public static int do_dispatch()
    {	
    	
	//Implementation of FCFS
	//System.out.println("Implementation of RoundRobin Scheduling");
	ThreadCB thread1 = null;

	if (readyQueue.isEmpty()) 						// checking if readyQueue is empty or not
	{
		if(MMU.getPTBR() == null){
		System.out.println("Cannot Dispatch Thread because readyQueue is Empty");						
		return FAILURE;				
		}// Cannot dispatch
		return SUCCESS;
	}			
			
	if (MMU.getPTBR() != null) 							// checking if thread1 is not null
	{
		
		thread1 = MMU.getPTBR().getTask().getCurrentThread();

		//System.out.println("Context swtich taking place");
		thread1.setStatus(ThreadReady);					// changing status to ThreadReady
		readyQueue.insert(thread1);					// add it to the readyQueue
		MMU.setPTBR(null);
		thread1.getTask().setCurrentThread(null);			// Context switch taking place
																					
	}
	
	// readyQueue has some thread thread2 
	
		thread1 = (ThreadCB)readyQueue.removeTail();	
		thread1.setStatus(ThreadRunning);				// set status of thread2 as running
	
		if(!ifCalculated(thread1.getID()))
			calculateTime(thread1.getID(),System.nanoTime());

		MMU.setPTBR(thread1.getTask().getPageTable());			// get thread2 task 
		thread1.getTask().setCurrentThread(thread1);			// set thread2 task as current
						
	return SUCCESS; 
	
    }

    public static void atError()						// Display Error Message
    {
        System.out.println("This is ERROR message!!");
    }

    public static void atWarning()						// Display Warning Messages
    {
        System.out.println("This is WARNING message!!");
    }

}
