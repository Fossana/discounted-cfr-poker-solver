package application;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class HandEvaluator
{
	int[] HR;
	
    private static HandEvaluator instance = null; 
 
    public static HandEvaluator getInstance() 
    { 
        if (instance == null) 
        	instance = new HandEvaluator(); 
  
        return instance; 
    } 
	
    void initTheEvaluator()
    {
		try (
			InputStream inputStream = new FileInputStream("res/HandRanks.dat");
		)
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			
			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = inputStream.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, nRead);
			}
			
			IntBuffer intBuf = ByteBuffer.wrap(buffer.toByteArray())
								.order(ByteOrder.LITTLE_ENDIAN)
								.asIntBuffer();
			HR = new int[intBuf.remaining()];
			intBuf.get(HR);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
    }
    
    public HandEvaluator()
    {
    	initTheEvaluator();
    }
    
    public void test()
    {
        // Now let's enumerate every possible 7-card poker hand
        int u0, u1, u2, u3, u4, u5;
        int c0, c1, c2, c3, c4, c5, c6;
        int[] handTypeSum = new int[10];  // Frequency of hand category (flush, 2 pair, etc)
        int count = 0; // total number of hands enumerated

        System.out.println("Enumerating and evaluating all 133,784,560 possible 7-card poker hands...\n\n");

        for (c0 = 1; c0 < 47; c0++)
        {
            u0 = HR[53+c0];
            for (c1 = c0+1; c1 < 48; c1++)
            {
                u1 = HR[u0+c1];
                for (c2 = c1+1; c2 < 49; c2++)
                {
                    u2 = HR[u1+c2];
                    for (c3 = c2+1; c3 < 50; c3++)
                    {
                        u3 = HR[u2+c3];
                        for (c4 = c3+1; c4 < 51; c4++)
                        {
                            u4 = HR[u3+c4];
                            for (c5 = c4+1; c5 < 52; c5++)
                            {
                                u5 = HR[u4+c5];
                                for (c6 = c5+1; c6 < 53; c6++)
                                {
                                    handTypeSum[HR[u5+c6] >> 12]++;

                                    // JMD: The above line of code is equivalent to:
                                    //int finalValue = HR[u5+c6];
                                    //int handCategory = finalValue >> 12;
                                    //handTypeSum[handCategory]++;

                                    count++;
                                }
                            }
                        }
                    }
                }
            }
        }
   
        System.out.println("BAD:              " + handTypeSum[0]);
        System.out.println("High Card:        " + handTypeSum[1]);
        System.out.println("One Pair:         " + handTypeSum[2]);
        System.out.println("Two Pair:         " + handTypeSum[3]);
        System.out.println("Trips:            " + handTypeSum[4]);
        System.out.println("Straight:         " + handTypeSum[5]);
        System.out.println("Flush:            " + handTypeSum[6]);
        System.out.println("Full House:       " + handTypeSum[7]);	
        System.out.println("Quads:            " + handTypeSum[8]);
        System.out.println("Straight Flush:   " + handTypeSum[9]);

        // Perform sanity checks.. make sure numbers are where they should be
        int testCount = 0;
        for (int index = 0; index < 10; index++)
            testCount += handTypeSum[index];
        if (testCount != count || count != 133784560 || handTypeSum[0] != 0)
        {
        	System.out.println("\nERROR!\nERROR!\nERROR!");
            return;
        }
    }
    
    public int getHandValue(int holeCard1, int holeCard2, int[] board)
    {
	    int p = HR[53 + holeCard1+1];
	    p = HR[p + holeCard2+1];
	    p = HR[p + board[0]+1];
	    p = HR[p + board[1]+1];
	    p = HR[p + board[2]+1];
	    p = HR[p + board[3]+1];
	    return HR[p + board[4]+1];
    }
}
