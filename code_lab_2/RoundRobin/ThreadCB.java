
/*
//////////////////////////////////////////////////////////////////////
/////	Principles Of Operating Systems: LAB #2 TASKS	    	 /////
/////	M003 - Group # 4: 			 		    	 			 /////
/////	GROUP MEMBERS:		    	 							 /////
/////		Ankur Pandey			                             /////
/////		Saurav Prakash		                                 /////
/////		Tushar Gupta		                                 /////
/////	    Tushar Bhatia			                             /////
//////////////////////////////////////////////////////////////////////
*/

// ROUND-ROBIN SCHEDULING ALGORITHM
/* =============================================================
 * 						ROUND-ROBIN READINGS
 * ==============================================================
 * Begin Time            : 1445450803945 
 * End Time              : 1445450809877
 * difference            : 5932 ms or 5.932 s
 * Total Number of Tasks : 11
 * 
 * 
 * Throughput =  Number of jobs/ tasks processed per unit time
 * Throughput =  (11/5.932) = 1.854 jobs /sec
 * 
 * 
 */
package osp.Threads;
import java.util.Vector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.Enumeration;
import java.sql.Timestamp;
import java.util.Date;
import osp.Tasks.*;
import osp.EventEngine.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;
import osp.Devices.*;
import osp.Memory.*;
import osp.Resources.*;
import java.util.concurrent.TimeUnit;


public class ThreadCB extends IflThreadCB 
{
    static GenericList readyQueue;
	static int threadCount;
	static int taskCount;
	static HashMap<Integer,Long> threadTime=new HashMap<Integer,Long>();
	static HashMap<Integer,Boolean> check=new HashMap<Integer,Boolean>();
	long systemStartTime;
	
	// Throughput Start
	static boolean flag1 = false;
    static boolean flag2 = false;
    static long beginTime = 0;
    static long endTime = 0;
    static int taskID = 0;
    static int count =0 ;
    static HashSet<Integer> threadIds = new HashSet<Integer>();
	static int totalTask=0;
	static int numberOfTask=0;
    // Throughput End
    
    public ThreadCB()
    {
	super();
    }



	public static void calculateTime(int current, long time)
	{
		if(threadTime.containsKey(current))
		{
			long startTime=threadTime.get(current);
			//MyOut.print(current, "Creation Time:" + startTime + " ID:"+current);
			//long total=TimeUnit.NANOSECONDS.toMillis(time)-startTime;
			long total=time-startTime;
			MyOut.print(current, "Response Time:" + total + " ID:"+current);
			threadTime.put(current,total);
		}
		else
		{		
			//threadTime.put(current,TimeUnit.NANOSECONDS.toMillis(time));
			threadTime.put(current,time);
			//MyOut.print(current, "CREATION TIME:" + TimeUnit.NANOSECONDS.toMillis(time)+ " ID:"+current);
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

	/**
       This method is called once at the beginning of the
       simulation. Can be used to initialize static variables.

       @OSPProject Tasks
    */
    public static void init()
    {
		System.out.println("In init");
        readyQueue = new GenericList();

    }

    static public ThreadCB do_create(TaskCB task)
    {
    	

    	// Throughput Start
    	if(!threadIds.contains(task.getID())){
    		count++;
			totalTask++;
			numberOfTask++;
    		threadIds.add(task.getID());	
    	}
    	if(!flag1){
    		flag1 = true;
    		beginTime = System.currentTimeMillis();;
    		MyOut.print("osp.Threads.ThreadCB","##### Begin Time :"+beginTime+"####");
    		
    	}
    	// Throughput End
    	
		if(task == null)
    	{
    		ThreadCB.dispatch();
    		return null;
    	}
        else if(task.getThreadCount() >= MaxThreadsPerTask)				// Check thread Creation permitted or not
    	{
    		System.out.println("Cannot create as max number of threads");
    		ThreadCB.dispatch();										//dispatcher called else warning 
    		return null;		
    	}
	
		ThreadCB thread= new ThreadCB();					//creation of thread 	
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
			thread.setPriority(task.getPriority());			//getting, querying and setting the priority of task & Thread
			thread.setStatus(ThreadReady);				
			
			threadCount++;
		//	calculateTime(thread,thread.getCreationTime());
			calculateTime(thread.getID(),System.nanoTime());
		//	MyOut.print(thread,"TIME TIME TIME TIME:"+thread.getCreationTime());
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			readyQueue.append(thread);				//thread placed in the readyQueue
			ThreadCB.dispatch();					//dispatcher called else warning
			return thread;
		}	

    }

    public void do_kill()
    {
	
        TaskCB theTask = null;
        
        // ThreadReady
        if (this.getStatus() == ThreadReady) {
            readyQueue.remove(this);
            this.setStatus(ThreadKill);
        }
        // ThreadRunning
        if (this.getStatus() == ThreadRunning) {
            if (MMU.getPTBR().getTask().getCurrentThread() == this) {
                MMU.setPTBR(null);
                getTask().setCurrentThread(null);
            }
        }
        
        // ThreadWaiting
        if (this.getStatus() >= ThreadWaiting) {
            this.setStatus(ThreadKill);
        }
        // Get the associated task and kill thread
        theTask = this.getTask();
        theTask.removeThread(this);	
        this.setStatus(ThreadKill);

        // Removing IORB from its queue by canceling IO on each device
        for (int i = 0; i < Device.getTableSize(); i++) {
            Device.get(i).cancelPendingIO(this);
        } 
        ResourceCB.giveupResources(this);
        
        // Kill the task if no remaining threads
        if (this.getTask().getThreadCount() == 0) {
            
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
			taskCount++;
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

    }

    public void do_resume()
    {
		if(this.getStatus() < ThreadWaiting) {
            return;
        }
               
        // Set the thread's status
        if(this.getStatus() == ThreadWaiting) {
            this.setStatus(ThreadReady);
        } else if (this.getStatus() > ThreadWaiting) {
            this.setStatus(getStatus()-1);
        }
        
        // Put the thread on the ready queue, if appropriate
        if (this.getStatus() == ThreadReady) {
            readyQueue.append(this);
        }
        
        ThreadCB.dispatch(); // dispatch a thread
    }
	
    public static int do_dispatch()
    {								
	//Implementation of RoundRobin Scheduling
	System.out.println("Implementation of RoundRobin Scheduling");
	//java.util.Date date= new java.util.Date();
	//System.out.println(new Timestamp(date.getTime()));
	ThreadCB thread1 = null;
	ThreadCB thread2 = null;

	try									//try-catch block to handle exceptions
	 {
		thread1 = MMU.getPTBR().getTask().getCurrentThread();
	 }
	catch (NullPointerException e)
	{
		System.out.println("Null pointer exception"+e);
	}

	if (thread1 != null) 							// checking if thread1 is not null
	{
		System.out.println("Context swtich taking place");
		thread1.setStatus(ThreadReady);					// changing status to ThreadReady
		readyQueue.append(thread1);					// add it to the readyQueue
		MMU.setPTBR(null);
		thread1.getTask().setCurrentThread(null);			// Context switch taking place
																					
	}

	
	if (readyQueue.isEmpty()) 						// checking if readyQueue is empty or not
	{
		MMU.setPTBR(null);
		System.out.println("Cannot Dispatch Thread because readyQueue is Empty");						
		return FAILURE;							// Cannot dispatch
	}			
	else									// readyQueue has some thread thread2 
	{
		thread2 = (ThreadCB)readyQueue.removeHead();	
		thread2.setStatus(ThreadRunning);				// set status of thread2 as running
		
		if(!ifCalculated(thread2.getID()))
			calculateTime(thread2.getID(),System.nanoTime());
		
		MMU.setPTBR(thread2.getTask().getPageTable());			// get thread2 task 
		thread2.getTask().setCurrentThread(thread2);			// set thread2 task as current
						
	}

	return SUCCESS; 
    }

    public static void atError()						// Display Error Message
    {
        System.out.println("!!!ERROR message!!!");
    }

    public static void atWarning()						// Display Warning Messages
    {
        System.out.println("***WARNING***");
    }

}
