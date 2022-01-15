package org.texastorque.util;


public class KPID {

    private double pGains;
    private double iGains;
    private double dGains;
    private double fGains;
    private double minOutput;
    private double maxOutput;
  

    public KPID(){
        pGains = 0;
        iGains = 0;
        dGains = 0;
        fGains = 0;
        minOutput = -1;
        maxOutput = 1;
    }

    public KPID(double pGains, double iGains, double dGains, double fGains, double minOutput, double maxOutput){
        this.pGains = pGains;
        this.iGains = iGains;
        this.dGains = dGains;
        this.fGains = fGains;
        this.minOutput = minOutput;
        this.maxOutput = maxOutput;
    }
    //---------------Set Methods---------------
    public void setP(double pGains){
        this.pGains = pGains;
    }

    public void setI(double iGains){
        this.iGains = iGains;
    }

    public void setD(double dGains){
        this.dGains = dGains;
    }

    public void setF(double fGains){
        this.fGains = fGains;
    }

    public void setMin(double minOutput){
        this.minOutput = minOutput;
    }

    public void setMax(double maxOutput){
        this.maxOutput = maxOutput;
    }

    //----------Accessor Methods-----------
    public double p(){
        return pGains;
    }

    public double i(){
        return iGains;
    }

    public double d(){
        return dGains;
    }

    public double f(){
        return fGains;
    }

    public double min(){
        return minOutput;
    }

    public double max(){
        return maxOutput;
    }
}