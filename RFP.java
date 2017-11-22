public class RFP {
  public static int modPow(int base, int exp, int mod) {
    if (mod == 1) return 0;
    int c = 1;
    for (int i = 1; i < exp+1; i++)
      c = (c*base) % (mod);
    // System.out.printf("%d^%d mod %d = %d\n", base, exp, mod, c);
    return c;
  }

  private static final int min_chunk = 5;
  private static final int avg_chunk = 13;
  private static final int d = 10;

  public static void main (String[] args) {
    int[] array = {2,3,1,4,1,5,2,6,7,3,9,9,2,1};
    // int[] array = {1,9,6,4,8,6,3,5};

    int temp = 0;
    

    int window_size = min_chunk;

    int prevRFP = 0;


    for (int s = 0; s < array.length-min_chunk+1; s++){
      // if (reset) {
      if (s == 0) {
        temp = 0;
        window_size = min_chunk;
        for (int i = s; i < s+min_chunk; i++) {
          temp += ((array[i] % avg_chunk) * modPow(d, min_chunk-((int)(i-s))-1, avg_chunk)) % avg_chunk;
        }
        temp = temp % avg_chunk;
      } else {
        int start = array[s-1];
        int end = array[s+min_chunk-1];

        temp = (prevRFP - (start * modPow(d,min_chunk-1,avg_chunk)) % avg_chunk) % avg_chunk;

        if (temp < 0)
          temp += avg_chunk;

        // System.out.printf("Test: %d\n", temp);



        temp = (((temp * (d % avg_chunk)) % avg_chunk) + (end % avg_chunk)) % avg_chunk;

      }
      // System.out.printf("Result: %d\n", temp);

      System.out.printf("%d ", temp);
      prevRFP = temp;
    }
    System.out.println();
  }
}
