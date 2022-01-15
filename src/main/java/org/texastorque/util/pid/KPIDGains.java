package org.texastorque.util.pid;

public class KPIDGains {
        public final double k, p, i, d;
        
        public KPIDGains(double k, double p, double i, double d) {
            this.k = k;
            this.p = p;
            this.i = i;
            this.d = d;
        }
    }