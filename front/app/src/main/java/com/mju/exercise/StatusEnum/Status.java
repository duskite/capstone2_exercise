package com.mju.exercise.StatusEnum;

public class Status {

    public static enum Request{
        GET,
        POST;
    }

    public static enum FilterTypeJoin{
        JOIN_DEFAULT,
        JOIN_CAN
    }
    public static enum FilterTypeDistance{
        DISTANCE_DEFAULT,
        DISTANCE_NEAR,
        DISTANCE_DIFFERENCE
    }
    public static enum FilterTypeDay{
        DAY_DEFAULT,
        DAY_NEAR,
        DAY_FAVDAY,
        DAY_PICK
    }

    public static enum DistanceDiff{
        DEFAULT,
        M100,
        M500,
        M1KM,
        M3KM,
        M3KMUP
    }

    public static enum FavDayType{
        DEFAULT,
        MON,
        TUE,
        WED,
        THU,
        FRI,
        SAT,
        SUN
    }

    public static enum FragType{
        LIST,
        JOINED,
        OPENED,
        NOTICE
    }
}
