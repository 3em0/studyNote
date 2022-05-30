package org.richfaces.demo.datafilterslider;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Wesley
 * Date: Jan 26, 2007
 * Time: 8:20:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataFilterSliderDaoImpl implements DataFilterSliderDao {

    private Map itemKeyMap = new HashMap();
    private static int DECIMALS = 1;
    private static int ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;
    private static DataFilterSliderDaoImpl ourInstance = new DataFilterSliderDaoImpl();

    public static DataFilterSliderDaoImpl getInstance() {
        return ourInstance;
    }

    private DataFilterSliderDaoImpl() {
        loadCarList("0");
        loadCarList("1");
        loadCarList("2");
        loadCarList("3");
        loadCarList("4");
        loadCarList("5");

    }

    public List getAllCarMakes(){
        List retVal = new ArrayList();

        retVal.add("Chevrolet");
        retVal.add("Ford");
        retVal.add("Nissan");
        retVal.add("Toyota");
        retVal.add("GMC");
        retVal.add("Infiniti");

        return retVal;
    }

    public List getCarsById(String id){
        List retVal = new ArrayList();

        retVal = (ArrayList)itemKeyMap.get( id );

        return retVal;
    }

    public List loadCarList(String id) {

        ArrayList retVal = new ArrayList();
        if ( itemKeyMap.containsKey( id ) ){
            retVal = (ArrayList)itemKeyMap.get( id );
            //log.info("***************************YES we already have this one in cache: " + id);
        }else{
            try{

                if (id.equals("0")){
                    retVal.addAll(createCar("Chevrolet","Corvette"));
                    retVal.addAll(createCar("Chevrolet","Malibu"));
                    retVal.addAll(createCar("Chevrolet","S-10"));
                    retVal.addAll(createCar("Chevrolet","Tahoe"));
                }else if(id.equals("1")){
                    retVal.addAll(createCar("Ford","Taurus"));
                    retVal.addAll(createCar("Ford","Explorer"));
                }else if(id.equals("2")){
                    retVal.addAll(createCar("Nissan","Maxima"));
                }else if(id.equals("3")){
                    retVal.addAll(createCar("Toyota","4-Runner"));
                    retVal.addAll(createCar("Toyota","Camry"));
                    retVal.addAll(createCar("Toyota","Avalon"));
                }else if(id.equals("4")){
                    retVal.addAll(createCar("GMC","Sierra"));
                    retVal.addAll(createCar("GMC","Yukon"));
                }else if(id.equals("5")){
                    retVal.addAll(createCar("Infiniti","G35"));
                }

            }catch(Exception e){
                System.out.println("!!!!!!loadAllCars Error: " + e.getMessage());
                e.printStackTrace();
            }

            itemKeyMap.put(id,retVal);
        }


        return retVal;
    }

    public int genRand() {
        return rand(1,10000);
    }

    public List createCar(String make, String model){

           ArrayList iiList = null;

           try{
                int arrayCount = rand(5,20);

                DemoInventoryItem[] demoInventoryItemArrays = new DemoInventoryItem[arrayCount];

                for (int j = 0; j < demoInventoryItemArrays.length; j++){
                    DemoInventoryItem ii = new DemoInventoryItem();

                    ii.setMake(make);
                    ii.setModel(model);
                    ii.setStock(randomstring(6,7));
                    ii.setVin(randomstring(14,15));
                    ii.setMileage(new BigDecimal(rand(5000,80000)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setMileageMarket(new BigDecimal(rand(25000,45000)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setPrice(new Integer(rand(15000,55000)));
                    ii.setPriceMarket(new BigDecimal(rand(15000,55000)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setDaysLive(rand(1,90));
                    ii.setChangeSearches(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setChangePrice(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setExposure(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setActivity(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setPrinted(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
                    ii.setInquiries(new BigDecimal(rand(0,5)).setScale(DECIMALS, ROUNDING_MODE));
                    demoInventoryItemArrays[j] = ii;

                }

                iiList = new ArrayList(Arrays.asList(demoInventoryItemArrays));

                }catch(Exception e){
                    System.out.println("!!!!!!createCategory Error: " + e.getMessage());
                    e.printStackTrace();
                }
            return iiList;
            }

        public static int rand(int lo, int hi)
        {
            Random rn2 = new Random();
            //System.out.println("**" + lo);
            //System.out.println("**" + hi);
                int n = hi - lo + 1;
                int i = rn2.nextInt() % n;
                if (i < 0)
                        i = -i;
                return lo + i;
        }

        public static String randomstring(int lo, int hi)
        {
                int n = rand(lo, hi);
                byte b[] = new byte[n];
                for (int i = 0; i < n; i++)
                        b[i] = (byte)rand('A', 'Z');
                return new String(b, 0);
        }

}
