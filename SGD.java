/**
 * Created by Joshua on 4/10/2015.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * 存储某用户对某电影的单项评分
 */
class rating {
    long timeStamp;
    int userId;
    int movieId;
    int value;

    public rating(long ts, int ui, int mi, int v) {
        this.timeStamp = ts;
        this.userId = ui;
        this.movieId = mi;
        this.value = v;
    }
}

class movie {
    int movieId;
    String movieName;
    String movieClass;
}

class user {
    int userId;
    boolean gender; // 0(False) for M:: 1(True) for F
    short age;
    short occupation;
    String zipCode;
}

/**
 * main
 */
public class SGD {

    /**
     * 读取用户方法
     * @param filePath
     * @param userList
     * @return 用户总数
     */
    public static int readUserFile(String filePath, ArrayList<user> userList) {
        int count = 0;
        try {
            File file = new File(filePath);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader bReader = new BufferedReader(reader);
            String lineTxt;

            while ((lineTxt = bReader.readLine()) != null) {
                ++count;
                //System.out.print(count + " " + lineTxt + "\n");
                String temp[] = lineTxt.split("::");
                user u = new user();
                u.userId = Integer.parseInt(temp[0]);
                if (temp[1] == "M") {
                    u.gender = true;
                } else u.gender = false;
                u.age = Short.parseShort(temp[2]);
                u.occupation = Short.parseShort(temp[3]);
                u.zipCode = temp[4];
                userList.add(u);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 读取记录方法
     * @param filePath
     * @param ratingList
     * @return 评分记录总数
     */
    public static int readRatingFile(String filePath, ArrayList<rating> ratingList) {
        int count = 0;
        try {
            File file = new File(filePath);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader bReader = new BufferedReader(reader);
            String lineTxt;

            while ((lineTxt = bReader.readLine()) != null) {
                ++count;
                //System.out.print(count + " " + lineTxt + "\n");
                String temp[] = lineTxt.split("::");
                rating r = new rating(-1, -1, -1, -1);
                r.userId = Integer.parseInt(temp[0]);
                r.movieId = Integer.parseInt(temp[1]);
                r.value = Integer.parseInt(temp[2]);
                r.timeStamp = Long.parseLong(temp[3]);
                ratingList.add(r);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 读取电影方法
     * @param filePath
     * @param movieList
     * @return 电影记录总数
     */
    public static int readMovieFile(String filePath, ArrayList<movie> movieList) {
        int count = 0;
        try {
            File file = new File(filePath);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file), "GBK");
            BufferedReader bReader = new BufferedReader(reader);
            String lineTxt;

            while ((lineTxt = bReader.readLine()) != null) {
                ++count;
                //System.out.print(count + " " + lineTxt + "\n");
                String temp[] = lineTxt.split("::");
                movie m = new movie();
                m.movieId = Integer.parseInt(temp[0]);
                m.movieName = temp[1];
                m.movieClass = temp[2];
                movieList.add(m);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * 对读入记录的基本处理
     * 初始化、链接
     * @param r
     * @param movieA
     * @param userA
     * @param m
     * @param u
     */
    public static void aofMovie(ArrayList<rating> r, double movieA[], double userA[], int m, int u) {
        double countMovie[] = new double[m];
        double countUser[] = new double[u];

        // setting to 0s
        for (int i = 0; i < m; i++) {
            countMovie[i] = 0;
            movieA[i] = 0;
        }
        for (int i = 0; i < u; i++) {
            countUser[i] = 0;
            userA[i] = 0;
        }

        // adding up loop
        for (int i = 0; i < r.size(); i++) {
            int temp_m = r.get(i).movieId - 1;
            int temp_u = r.get(i).userId - 1;
            int temp_v = r.get(i).value;

            movieA[temp_m] += temp_v;
            userA[temp_u] += temp_v;

            countMovie[temp_m]++;
            countUser[temp_u]++;
        }

        // making average
        for (int i = 0; i < m; i++) {
            movieA[i] /= countMovie[i];
        }
        for (int i = 0; i < u; i++) {
            userA[i] /= countUser[i];
        }
    }

    /**
     * R表生成方法
     * R表示真实数据、稀疏矩阵
     * @param R
     * @param x
     * @param y
     * @param ratingList
     */
    public static void makeR(rating R[][], int x, int y, ArrayList<rating> ratingList) {

        for (int j = 0; j < y; j++) {
            for (int i = 0; i < x; i++) { // 全部rating的value和timestamp为-1
                R[i][j] = new rating(-1, j + 1, i + 1, -1);
            }
        }
        for (int i = 0; i < ratingList.size(); i++) {
            rating r = ratingList.get(i);
            R[r.movieId - 1][r.userId - 1] = r;
        }
    }

    /**
     * PQ矩阵的生成方法
     * @param P
     * @param Q
     * @param a 平均数
     * @param x 对应R
     * @param y 对应R
     * @param f factor
     */
    public static void fillinPQ(double P[][], double Q[][], double a, int x, int y, int f) {
        // P[f][y]
        int count = 0;
        double temp = Math.sqrt(a/f); // a 是所有电影的平均得分，f是factor
        for (int j = 0; j < y; j++) {
            for (int i = 0; i < f; i++) {
                P[i][j] = temp + (Math.random() * 0.5) - 0.5; //[-0.5,0.5];的随机数
                count++;
            }
        }
        // Q[x][f]
        for (int j = 0; j < f; j++) {
            for (int i = 0; i < x; i++) {
                Q[i][j] = temp + (Math.random() * 0.5) - 0.5; //[-0.5,0.5];的随机数
                count++;
            }
        }
    }

    /**
     *
     * @param RATING_NB
     * @param ratingList
     * @param e 误差距离
     * @param P
     * @param Q
     * @param bu 预留
     * @param bi 预留
     * @param f factor
     * @param a 均值
     * @param aMovie
     * @param aUser
     */
    public static void calculateSGD(int RATING_NB, ArrayList<rating> ratingList,
                                    double e[][], double P[][], double Q[][], double bu[], double bi[], int f,
                                    double a, double aMovie[], double aUser[]) {
        Comparator<rating> comp = new Comparator<rating>() {
            @Override
            public int compare(rating r1, rating r2) {
                return (int)(r1.timeStamp - r2.timeStamp);
            }
        };
        Collections.sort(ratingList, comp); // 以timestamp作为唯一识别符，并为排序依据
        /*for (int i = 0; i < ratingList.size(); i++) {
            System.out.println(ratingList.get(i).ratingId);
        }*/

        // 收敛判断#1 @@@@@@@@@@@@@@
        int times = 20; // 进行20次收敛 /还有更好地解决方案
        for (int i = 0; i < RATING_NB * times; i++) { // 每次都是完整地所有rating的循环
            int j = i;
            while (j >= RATING_NB) {// 确定每次的j都是0到RATING_NB-1
                j -= RATING_NB;
            }

            double r = ratingList.get(j).value; // 只对有rating值的进行处理
            if (r == -1) {
                continue;
            }
            int x = ratingList.get(j).movieId - 1;
            int y = ratingList.get(j).userId - 1;

            double pq = 0;//pq
            for (int k = 0; k < f; k++) {
                pq += P[k][y] * Q[x][k];
            }

            //e[x][y] = 2 * (r - (a + bu[y] + bi[x] + pq)); // r是实际的值 e表示模拟和实际的差值
            e[x][y] = 2 * (r - (a + pq));//bu[y] + bi[x] + pq));

            // 收敛判断#2 @@@@@@@@@@@@@@
            double thisTime = pq;//bu[y] + bi[x] + pq;

            // one of the most important problem is how to fix this two value
            //l is regularization parameter
            double l = 0.01;
            //m is learning rate
            double m = 0.001;

            //double lastTime = 0; // better solution
            //double thisTime = 0;
            //double buTemp;
            //double biTemp;
            double pTemp;
            double qTemp;
            for (int k = 0; k < f; k++) {
                //buTemp = bu[y] + m * (e[x][y] * bu[y] - (2 * l * bu[y]));
                //biTemp = bi[x] + m * (e[x][y] * bi[x] - (2 * l * bi[x]));
                qTemp = Q[x][k] + m * (e[x][y] * P[k][y] - (2 * l * Q[x][k]));
                pTemp = P[k][y] + m * (e[x][y] * Q[x][k] - (2 * l * P[k][y]));

                //bu[y] = buTemp;
                //bi[x] = biTemp;
                P[k][y] = pTemp;
                Q[x][k] = qTemp;
            }

            // 收敛判断#2 @@@@@@@@@@@@@@
            for (int k = 0; k < f; k++) {
                pq += P[k][y] * Q[x][k];
            }
            double nextTime = pq;//bu[y] + bi[x] + pq;
            if (abs(thisTime - nextTime) < 0.05) break;
        }
    }

    /**
     * page 79 D
     * @param R
     * @param RHat
     * @param B
     * @param C
     * @return
     */
    public static double movieD(rating R[][], double RHat[][], int B, int C){
        double result;
        double up = 0;
        double bottmLeft = 0;
        double bottmRigt = 0;

        B--; C--;
        //for (int i = 0; i < 3952; i++) { // Movie
            for (int j = 0; j < 6040; j++) { // User
                if (R[B][j].value != -1 && R[C][j].value != -1){
                    up += RHat[B][j] * RHat[C][j];
                    bottmLeft += RHat[B][j] * RHat[B][j];
                    bottmRigt += RHat[C][j] * RHat[C][j];
                }
            }
        //}
        result = up / Math.sqrt(bottmLeft * bottmRigt);
        return result;
    }

    /**
     * page 80 r_3d
     * @param R
     * @param RHat
     * @param a
     * @param user
     * @param movie
     * @return
     */
    public static double ratingResult(rating R[][], double RHat[][], double a, int user, int movie) {
        user--;
        double max = 0;
        int maxID = 0;
        double sec = -1;
        int secID = 0;
        double temp = 0;

        for (int i = 0; i < 3952; i++) { // Movie
            if (i == movie-1) continue;
            temp = movieD(R, RHat, i + 1, movie);
            if (abs(temp) >= max) { // 最大值
                sec = max;
                max = temp;
                maxID = i;
                continue;
            }
            if (abs(temp) >= sec) { // 次大
                sec = temp;
                secID = i;
            }
        }
        double result = (RHat[maxID][user] * max + RHat[secID][user] * sec) / (abs(max) + abs(sec));
        result += a + RHat[movie-1][user];
        //result += a;

        return result;
    }

    /**
     * absolute value
     * @param inp
     * @return
     */
    public static double abs(double inp) {
        if (inp >= 0) return inp;
        else return -inp;
    }

    /**
     * calculate Root Mean Squared Error
     * @param R
     * @param P
     * @param Q
     * @param f
     * @param MOVIE_NB
     * @param user
     * @param a
     * @param bu
     * @param bi
     */
    public static void assuRating(rating R[][], double P[][], double Q[][], int f, int MOVIE_NB, int user, double a, double bu[], double bi[]) {

        double rmse = 0;
        int c = 0;
        double temp[] = new double[MOVIE_NB];
        for (int m = 0; m < MOVIE_NB; m++) { // 全部置0
            temp[m] = 0;
        }

        for (int m = 0; m < MOVIE_NB; m++) {
            for (int k = 0; k < f; k++) {
                temp[m] += P[k][user] * Q[m][k];
            }
            temp[m] = temp[m] + a;//bu[user] + bi[m] + a;
            //System.out.println(temp[m]);
            if (R[m][user].value == -1) continue;
            //System.out.println(m+1 + ": " + (R[m][user].value - temp[m]));
            c++;
            rmse += Math.pow(R[m][user].value - temp[m], 2);
        }
        rmse = Math.sqrt(rmse/c);
        System.out.println(rmse);
    }

    /**
     * recommend three movies according to the calculation
     * @param R
     * @param P
     * @param Q
     * @param f
     * @param MOVIE_NB
     * @param user
     * @return
     */
    public static int[] recommendMovies(rating R[][], double P[][], double Q[][], int f, int MOVIE_NB, int user) {

        double temp[] = new double[MOVIE_NB];
        for(int m = 0; m < MOVIE_NB; m++) { // 全部置0
            temp[m] = 0;
        }

        for (int m = 0; m < MOVIE_NB; m++) {
            for (int k = 0; k < f; k++) {
                temp[m] += P[k][user] * Q[m][k];
            }
        }
        double[] result = {0,0,0};
        int [] rst = {-1,-1,-1};
        for(int m = 0; m < MOVIE_NB; m++) {
            if (R[m][user].value == -1) {
                if (temp[m] >= result[0]) {
                    result[2] = result[1];
                    rst[2] = rst[1];
                    result[1] = result[0];
                    rst[1] = rst[0];
                    result[0] = temp[m];
                    rst[0] = m;
                    continue;
                }
                if (temp[m] >= result[1]) {
                    result[2] = result[1];
                    rst[2] = rst[1];
                    result[1] = temp[m];
                    rst[1] = m;
                    continue;
                }
                if (temp[m] >= result[2]) {
                    result[2] = temp[m];
                    rst[2] = m;
                    continue;
                }
            }
        }
        return rst;
    }

    /**
     * main
     * @param argv
     */
    public static void main(String argv[]) {
        String mFile = "D://Joshua/Doc/IdeaProjects/Recommendation_System/src/ml-1m/movies.dat";
        String rFile = "D://Joshua/Doc/IdeaProjects/Recommendation_System/src/ml-1m/ratings.dat";
        String uFile = "D://Joshua/Doc/IdeaProjects/Recommendation_System/src/ml-1m/users.dat";

        // for saving data
        ArrayList<movie> m = new ArrayList<movie>();
        ArrayList<rating> r = new ArrayList<rating>();
        ArrayList<user> u = new ArrayList<user>();

        // the entry number of each file and read the file then save data to the array above
        final int MOVIE_NB = 3952;
        readMovieFile(mFile, m); // 3952 is movie id range but there are only 3883 available
        final int RATING_NB = readRatingFile(rFile, r); // 1000209
        final int USER_NB = readUserFile(uFile, u); // 6040

        // test
        System.out.println(MOVIE_NB + " " + RATING_NB + " " + USER_NB);

        // factor is the K in textbook on page 84
        final int FACTOR = 100; // 1000209 / (3883 + 6040) = 100.797

        // a is the average rating
        double a = 0;
        for (int i = 0; i < r.size(); i++) {
            a += r.get(i).value;
        }
        a /= r.size();
        System.out.print("the ave mark is: " + a + "\n");

        //test for random number between [-1, 1]
        /*for (int w = 0; w < 20; w++) {
            System.out.print((Math.random() * 2) - 1 + "\n");
        }*/

        // the average rating of every movie and user
        double aMovie[] = new double[MOVIE_NB];
        double aUser[] = new double[USER_NB];
        aofMovie(r, aMovie, aUser, MOVIE_NB, USER_NB);

        /*for (int g = 0; g < MOVIE_NB; g++) {
            System.out.print(aMovie[g] + "\n");
        }*/
        //for (int g = 0; g < USER_NB; g++) {
        //    System.out.print(aUser[g] + "\n");
        //}

        //double RHat[][] = new double[MOVIE_NB][USER_NB];
        //for (int i = 0; i < MOVIE_NB; i++) {
        //    for (int j = 0; j < USER_NB; j++) {
        //        RHat[i][j] = a + (aMovie[i] - a) + (aUser[j] - a);
        //        //RHat[i][j] = (aMovie[i] - a) + (aUser[j] - a);
        //    }
        //}

        //Jama.QRDecomposition.solve();


        rating R[][] = new rating[MOVIE_NB][USER_NB];
        makeR(R, MOVIE_NB, USER_NB, r);

        //double compare[] = new double[MOVIE_NB];
        //for (int i = 0; i < MOVIE_NB; i++) {
        //   compare[i] = R[i][0].value;
        //}

        // the static pra of user
        double bu[] = new double[USER_NB];

        // the static pra of Movie
        double bi[] = new double[MOVIE_NB];

        // 关于 bu bi 的初始值设定
        for (int y = 0; y < USER_NB; y++) {
            //6040
            bu[y] = (aUser[y] - a);//(Math.random() * 0.2) - 0.2;//bu
        }
        for (int x = 0; x < MOVIE_NB; x++) {
            //3952
            bi[x] = (aMovie[x] - a);//(Math.random() * 0.2) - 0.2;//bi
        }

        // the matrix of p
        double p[][] = new double[FACTOR][USER_NB];
        // the matrix of q
        double q[][] = new double[MOVIE_NB][FACTOR];
        fillinPQ(p, q, a, MOVIE_NB, USER_NB, FACTOR);

        // e(x,i) = 2(r(x,i) - <Q(i),P(x)>)
        double e[][] = new double[MOVIE_NB][USER_NB];

        calculateSGD(RATING_NB, r, e, p, q, bu, bi, FACTOR, a, aMovie, aUser);

        //int rst[];
        //rst = recommendMovies(R, p, q, FACTOR,MOVIE_NB, 1);

        for (int i = 0; i < 100; i++) {
            assuRating(R, p, q, FACTOR, MOVIE_NB, i, a, bu, bi);
        }
    }
}