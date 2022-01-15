package org.texastorque.torquelib.util;

public class Torque2DVector{

    private double magnitude;
    private double angle;

    public Torque2DVector(double magnitude, double angle){
        this.magnitude = magnitude;
        this.angle = angle;
    }

    //components found differently from basic unit circle since theta=0 is the front of the robot
    //*****************Accesors*********************** *
    public double getMagnitude(){
        return magnitude;
    }

    public double getAngle(){
        return angle;
    }
    public double getXComponent(){
        return Math.sin(angle)*magnitude;
    }

    public double getYComponent(){
        return Math.cos(angle)*magnitude;
    }

    //******************************Modifiers*************************************
    public void setMagnitude(double magnitude){
        this.magnitude = magnitude;
    }

    public void setAngle(double angle){
        this.angle = angle;
    }

//********************Math Stuff**************************
    public Torque2DVector add(Torque2DVector vector){
        double addedX = getXComponent() + vector.getXComponent();
        double addedY = getYComponent()+ vector.getYComponent();
        double addedAngle = Math.atan2(addedX, addedY);
        double addedMagnitude = Math.hypot(addedX, addedY);
        
        return new Torque2DVector(addedMagnitude, addedAngle);
    }

    public Torque2DVector subtract(Torque2DVector vector){
        double addedX = getXComponent() - vector.getXComponent();
        double addedY = getYComponent() - vector.getYComponent();
        double addedAngle = Math.atan2(addedX, addedY);
        double addedMagnitude = Math.hypot(addedX, addedY);
        
        return new Torque2DVector(addedMagnitude, addedAngle);
    }

}