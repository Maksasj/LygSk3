/** Simple system multithreading performance test.
 Version 1.3
 @author R.Vaicekauskas
 */
class TTest extends Thread
{
    volatile boolean finished = false;
    public TTest() {}

    //-----------------------------------------
    // work unit for 1 thread
    boolean work (long i)
    {
        return i < 0;
    }

    //-----------------------------------------
    // Thread.run()
    public void run()
    {
        // To have total work independed of the number of threads
        final long  localWorkload = TTest.workload * 1000000L / nThreads;
        for (long i = 0; i < localWorkload; i++)
        {
            boolean doPrint = work(i);
            if (doPrint) System.out.println("Never be printed");
        }
        //System.out.println(this+" finished");
        this.finished = true;
    }

    //-----------------------------------------
    // Global parameters
    static int workload = 0;
    static int nThreads = 0; // Number working threads

    //-----------------------------------------
    // Make tests for given workload and number threads
    // Returns working time
    static double makePerformanceTest() throws Exception
    {
        long time0 = System.currentTimeMillis();

        // Create and start threads
        TTest[] aThreads = new TTest[nThreads];
        for (int i = 0; i < nThreads; i++)
        {
            (aThreads[i] = new TTest()).start();
        }

        // Wait until all threads finish
        for (int i = 0; i < nThreads; i++)
        {
            while (!aThreads[i].finished)
            {
                aThreads[i].join();
            }
        }
        long time1 = System.currentTimeMillis();
        double dtime = (time1-time0)/1000.;
        return dtime;
    }

    //-----------------------------------------
    public static void main(String[] args)
    {
        try
        {
            if (args.length < 2
                    || ! ( (nThreads  = Integer.parseInt(args[0])) >=1 && nThreads  <= 16 &&
                    (workload = Integer.parseInt(args[1])) >=1 && workload <= 100000000))
            {
                System.err.println("Simple system multithreading performance test. Ver 1.3");
                System.err.println("Parameters: <number threads 1..16> <workload: 1..100000000>");

                System.err.println("#Make auto test: find workload for > 1 sec...");

                TTest.nThreads = 1;
                TTest.workload = 1;

                for (double dtime = 0; ; workload *= 2)
                {
                    dtime = makePerformanceTest();
                    if (dtime > 1.) break; //>>>>>>>
                }

                System.out.println("#nThreads #workload #timeS #speedup");

                double dtime1=0.;
                for (nThreads = 1; nThreads <= 32; nThreads *= 2)
                {
                    double dtime = makePerformanceTest();
                    dtime1 = nThreads==1 ? dtime : dtime1;
                    double speedup = dtime1 / dtime;
                    System.out.println( nThreads + " " + workload  + " " +dtime + " " + speedup);
                }

                System.out.println("#completed");
                System.exit(1); //>>>>>>>
            }
            else
            {
                System.err.println("#Test for: nThreads="+nThreads+" workload="+workload);
                double dtime = makePerformanceTest();
                System.err.println("#Completed. Running time: " + dtime + "s");
                System.out.println( nThreads + " " + workload  + " " +dtime );
                System.exit(0); //>>>>>>>
            }
        }
        catch (Exception exc)
        {
            System.out.println(exc);
            exc.printStackTrace();
            System.exit(4);
        }
    }
}