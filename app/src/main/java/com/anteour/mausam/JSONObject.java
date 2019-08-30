package com.anteour.mausam;

import java.util.ArrayList;

/*This Class is in accordance with the layout of JSON Object we receive from https://openweathermap.org/city
 *      JSON Layout:
 *       -----Object
 *               |-----List
 *                       |-----List(0)
 *                               |-----main
 *                                       |-----temp_min
 *                                       |-----temp_max
 *                               |-----dt_txt
 *                        |-----List(1)
 *                               |-----main
 *                                       |-----temp_min
 *                                       |-----temp_max
 *                               |-----dt_txt
 *                       #
 *                       #
 *                       #
 *                       #
 *                       #
 *                       |-----List(n)
 *                               |-----main
 *                                       |-----temp_min
 *                                       |-----temp_max
 *                               |-----dt_txt
 *               |-----City
 *                       |-----name
 *
 *
 *      Every Nesting is represented here by Inner Class
 *      Class Layout:
 *      ----JSONObject
 *                |-----ArrayList
 *                          |-----list(0)
 *                                  |-----Temperature
 *                                              |-----temp_min
 *                                              |-----temp_max
 *                                  |-----dt_txt
 *                           |-----list(1)
 *                                  |-----Temperature
 *                                              |-----temp_min
 *                                              |-----temp_max
 *                                  |-----dt_txt
 *                            #
 *                            #
 *                            #
 *                            #
 *                            #
 *                            |-----list(n)
 *                                  |-----Temperature
 *                                              |-----temp_min
 *                                              |-----temp_max
 *                                  |-----dt_txt
 *                |-----City
 *                        |-----name;
 */
class JSONObject {
    private ArrayList<ListObject> list = new ArrayList<>(8);
    private City city = null;

    ArrayList<ListObject> getList() {
        return list;
    }

    City getCity() {
        return city;
    }

    class ListObject {
        private Temperature main = null;
        private String dt_txt = null;

        Temperature getMain() {
            return main;
        }

        String getDt_txt() {
            return dt_txt;
        }

        class Temperature {

            private float temp_min = 0.0f;
            private float temp_max = 0.0f;

            float getTemp_min() {
                return temp_min;
            }

            float getTemp_max() {
                return temp_max;
            }
        }
    }

    class City {
        private String name = null;

        String getName() {
            return name;
        }
    }


}