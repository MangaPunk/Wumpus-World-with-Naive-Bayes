package wumpusworld;


import java.util.ArrayList;

public class Naive_Bayes {

    private ArrayList<int[]> frontier = new ArrayList<int[]>();
    private ArrayList<double[]> probability_set = new ArrayList<double[]>();
    private World w,w2;
    private int[] wumpus_pos = new int[2];
    private  int pit_c = 0;
    private boolean found_wumpus;

    /*probability of different cases*/
    private static double PIT_PROB = 0.2;
    private static double WUMPUS_PROB = 0.0667;


    /*condition constants*/
    private static final int PIT = 1;
    private static final int WUMPUS = 2;


    /*reference parameters for decision making*/
    private static final double OFFSET = 0.1;
    private static final double RISK = 0.25;


    /**
     * w2克隆当前世界情况，用于标注frontier
     * @param world
     */
    public Naive_Bayes(World world) {
        w = world;
        w2=w.cloneWorld();  /*world model for get_frontier function*/
        get_frontier(1,1);
    }


    /**
     *
     * @param x   递归起点的坐标
     * @param y
     */
    private void get_frontier(int x, int y) {

        System.out.println("checking the ("+x+","+y+")");
        if (!w2.isValidPosition(x, y)) {
            return;
        }
        if (w2.isUnknown(x, y)) {
            if (!(w2.hasMarked(x, y))) {
                frontier.add(new int[]{x, y, 0});
                probability_set.add(new double[]{0,0});   /*Synchronously initialize corresponding probability set*/
                w2.setMarked(x, y);
                System.out.println("set ("+x+","+y+") as frontier");
            }
            else         System.out.println("("+x+","+y+") has been set");
            return;
        }
        else if(w2.hasMarked(x,y)) return;
        else
            {
            w2.setMarked(x,y);
            get_frontier(x + 1, y );
            get_frontier(x - 1, y);
            get_frontier(x, y + 1);
            get_frontier(x, y - 1);
        }
    }



    private void get_probability(int condition) {

        System.out.println("===============");
        double p;
        if (condition == PIT) {
            p = PIT_PROB;
            System.out.println("calculating P(pit).....");
            System.out.println("now the p of pit is: "+p);
        } else if (condition == WUMPUS) {
            p = WUMPUS_PROB;
            System.out.println("calculating P(Wumpus).....");
            System.out.println("now the p of wumpus is: "+p);
        } else {
                System.out.println("OUT OF CONDITION RANGE");
                return;
            }

        System.out.println("===============");
        for (int i = 0; i < frontier.size(); i++) {
            int[] query_true = new int[]{frontier.get(i)[0],frontier.get(i)[1],1};
            int[] query_false = new int[]{frontier.get(i)[0],frontier.get(i)[1],0};
            double total_pro_true=0;
            double total_pro_false=0;
            double f = 0;

            ArrayList<int[]> portion = cloneList(frontier);
            portion.remove(i);
            ArrayList<ArrayList<int []>> combinations = new ArrayList<ArrayList<int[]>>();
            int[] count = combination(portion,combinations);

            System.out.println("##### Query ("+frontier.get(i)[0]+","+frontier.get(i)[1]+")##### ");

            /*iteratively calculate P(combination) in list combinations*/
            for(int j = 0; j < combinations.size(); j++)
            {
                int sum = combinations.get(j).size();
                System.out.println("combination "+j+")  ");
                String msg = "Not consistent";
                for(int k=0; k<combinations.get(j).size();k++){
                    System.out.print(combinations.get(j).get(k)[2]);
                }
                System.out.println(" ");



                if((condition==PIT && count[j]<4) || (condition==WUMPUS && count[j]<2))
                {
                    System.out.print("when P(query) is true: ");
                    if(check_consistent(combinations.get(j),condition,query_true))
                    {
                        double add = Math.pow(p,count[j]) * Math.pow(1-p,sum-count[j]);
                        total_pro_true += add;
                        System.out.println("P = "+add);
                    }else System.out.println(msg);


                    System.out.print("when P(query) is false: ");
                    if(check_consistent(combinations.get(j),condition,query_false))
                    {
                        double add = Math.pow(p,count[j]) * Math.pow(1-p,sum-count[j]);
                        total_pro_false += add;
                        System.out.println("P = "+add);
                    }else System.out.println(msg);
                }


            }

            System.out.println("---------------------");
            total_pro_true = p*total_pro_true;
            System.out.println("when frontier ("+frontier.get(i)[0]+","+frontier.get(i)[1]+") is true,"+
                                            "the total probability is "+total_pro_true);
            total_pro_false = (1-p)*total_pro_false;
            System.out.println("when frontier ("+frontier.get(i)[0]+","+frontier.get(i)[1]+") is false,"+
                    "the total probability is "+total_pro_false);

            try{
                f= total_pro_true/(total_pro_true+total_pro_false);
            }catch (ArithmeticException e){
                System.out.println("ERROR: You shouldn't divide a number by zero!");
            }catch (Exception e){
                System.out.println("WARNING: Some other exception");
            }

            probability_set.get(i)[condition-1]=f;
            System.out.println("final probability of ("+frontier.get(i)[0]+","+frontier.get(i)[1]+"):"+f);
            System.out.println("---------------------");

            if(condition==WUMPUS && f==1){
                found_wumpus = true;
                wumpus_pos[0] = frontier.get(i)[0];
                wumpus_pos[1] = frontier.get(i)[1];
                System.out.println("Oops! I know where is the wumpus now:)");
                return;
            }

        }

        System.out.println("===============");
    }


    private boolean check_consistent(ArrayList<int[]> arrayList, int condition, int[] query) {

        World cw = w.cloneWorld();  /*create a world of conjecture*/
        int size = cw.getSize();
        boolean is_consist = true;
        ArrayList<int[]> conject = cloneList(arrayList);
        conject.add(query);

        if(condition==PIT)
        {
            for (int x = 1; x <= size; x++) {
                for (int y = 1; y <= size; y++)
                {
                    if(!cw.isUnknown(x,y)&&cw.hasPit(x,y)){
                        cw.markSurrounding(x,y);
                    }
                }
            }
        }


        for (int i = 0; i < conject.size(); i++) {
            int cx, cy;
            cx = conject.get(i)[0];
            cy = conject.get(i)[1];
            if (conject.get(i)[2] == 1)
            {
                cw.markSurrounding(cx,cy);
            }
        }

        for (int x = 1; x <= size; x++) {
            for (int y = 1; y <= size; y++) {
                if (!(cw.isUnknown(x, y))) {

                        if(condition==PIT) {
                            if (!(cw.hasBreeze(x, y) == cw.hasMarked(x, y))) {
                                is_consist = false;
                            }
                        }

                        else if(condition==WUMPUS) {
                            if (!(cw.hasStench(x, y) == cw.hasMarked(x, y))) {
                                is_consist = false;
                            }
                        }
                }

                if (!is_consist) {
                    return is_consist;   /*once the combination is confirmed as inconsistent, break out of for loop*/
                }
            }

        }

        return is_consist;
    }

    private ArrayList<int[]> cloneList(ArrayList<int[]> arrayList){
        ArrayList<int[]> clone = new ArrayList<int[]>(arrayList.size());
        for (int i=0;i<arrayList.size(); i++)
        {
            clone.add(arrayList.get(i).clone());

        }
        return  clone;
    }


    private int[] combination(ArrayList<int[]> elements, ArrayList<ArrayList<int[]>> result){
          /*elements item format :  int[] { pos_x, pos_y, status }
                          status:  1 = true | 0 = false */

        int n= elements.size();
        int nbit = 1 << n;    /* bit =2^n, indicates the number of different combinations*/
        int[] count = new int[nbit];

        for(int i=0; i<nbit; i++)
        {
            int c = 0;   /*count of positions whose statuses are true*/
            ArrayList<int[]> comb = cloneList(elements);
            for(int j=0; j<n; j++)
            {
                int tmp = 1<<j;
                if((tmp & i)!= 0){
                    comb.get(j)[2]=1; /*set the status in comb[j] as true*/
                    c++;
                }
            }

            result.add(comb);
            count[i] = c;
        }

        return  count;
    }

    public boolean get_goal(int[] position,int wumpus_status){

        /*update PIT_PROB*/
        for (int x = 1; x <= w.getSize(); x++) {
            for (int y = 1; y <= w.getSize(); y++)
            {
                if(!w.isUnknown(x,y)&&w.hasPit(x,y)){
                    pit_c += 1;
                }
            }
        }
        double d_knowns = w.getKnowns();
        double d_pits = pit_c;
        PIT_PROB = (3-d_pits)/(16-d_knowns);


        int index;
        boolean shoot=false;
        boolean is_safe=false;
        double pit_upper = 0.2;

        get_probability(PIT);

        if(wumpus_status==MyAgent.NOT_FOUND){

            double min_wumpus=1;
            int n = -1;
            while(n<0)
            {
                for(int i=0; i<probability_set.size(); i++)
                {
                    //*get the probability of a wumpus or a pit in the frontier[i]*//*
                    double pw = probability_set.get(i)[1];
                    double pp = probability_set.get(i)[0];

                    if(pw<=min_wumpus && pp<pit_upper) {
                        if (pw == min_wumpus && n>=0 && is_farther(frontier.get(i),frontier.get(n))) {
                            continue;
                        }
                        min_wumpus=pw;
                        n=i;
                    }
                }

                pit_upper += OFFSET;
            }

            index = n;
            if(probability_set.get(index)[1]>RISK && w.hasArrow()){
                System.out.println("Anyway, I will shoot though not sure! ");
                shoot = true;
            }
        }



        else{

            double min_pit=1;
            int n=-1;

            while(!is_safe)
            {
                for(int i=0; i<probability_set.size(); i++){

                    /*get the probability of containing a pit in frontier[i]*/
                    double p = probability_set.get(i)[0];
                    if(p<=min_pit) {
                        if (p == min_pit && n>=0 && is_farther(frontier.get(i),frontier.get(n))) {
                            continue;
                        }
                        min_pit=p;
                        n=i;
                    }
                }

                if(wumpus_status==MyAgent.DEAD)is_safe=true;
                else{

                    /*check if the selected position contains a wumpus*/
                    if(wumpus_pos[0]==frontier.get(n)[0] && wumpus_pos[1]==frontier.get(n)[1])
                    {
                        System.out.println("there is a wumpus in my goal!!");

                        if(w.hasArrow()){
                            shoot = true;
                            is_safe = true;
                            System.out.println("but I have a arrow :)");
                        }
                        else{
                            if(probability_set.size()>1){
                                probability_set.remove(probability_set.get(n));
                                min_pit=1;
                                System.out.println("so I quit ~");
                            }
                            else is_safe = true;    /*只剩最后一个未知格子，同时存在金块和wumpus，箭已经使用，必输局*/

                        }
                    }
                    else is_safe=true;
                }
            }

            index = n;

        }

        position[0] = frontier.get(index)[0];
        position[1] = frontier.get(index)[1];

        return shoot;

    }



    public boolean query_wumpus(int[] position){

        /*update WUMPUS_PROB*/
        double d_knowns = w.getKnowns();
        WUMPUS_PROB = 1/(16-d_knowns);

        get_probability(WUMPUS);

        if(found_wumpus){
            position[0]=wumpus_pos[0];
            position[1]=wumpus_pos[1];
        }
        return found_wumpus;
    }


    public void set_wumpus_pos(int[] position){
        wumpus_pos[0]=position[0];
        wumpus_pos[1]=position[1];
    }

    public boolean is_farther(int[] goalA, int[] goalB){

        int x = w.getPlayerX();
        int y = w.getPlayerY();
        int distanceA = Math.abs(x-goalA[0])+Math.abs(y-goalA[1]);
        int distanceB = Math.abs(x-goalB[0])+Math.abs(y-goalB[1]);

        if(distanceA>distanceB) return true;
        else return  false;

    }



}




