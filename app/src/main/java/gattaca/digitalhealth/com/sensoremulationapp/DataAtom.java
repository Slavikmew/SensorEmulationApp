package gattaca.digitalhealth.com.sensoremulationapp;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by as on 05.03.2016.
 */
public class DataAtom implements Serializable {
    static private final String LOG_TAG = DataAtom.class.getSimpleName();
    private BodyTemperature mBodyTemperature;
    private Pulse mPulse;

    public DataAtom(double bodyTemperature, int pulse) {
        mBodyTemperature = new BodyTemperature(bodyTemperature);
        mPulse = new Pulse(pulse);
    }

    public DataAtom(byte[] data) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(bis);
            DataAtom local = (DataAtom) in.readObject();
            this.mBodyTemperature = local.mBodyTemperature;
            this.mPulse = local.mPulse;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Convert from byte[] to DataAtom problem");
        } catch (ClassNotFoundException c) {
            //impossible situation
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public double getTemperatute() {
        return mBodyTemperature.bodyTemperature;
    }

    public int getPulse() {
        return mPulse.pulse;
    }

    public byte[] getBytes() {
        byte[] returnData = {(byte) 0xFF};
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(this);
            returnData = bos.toByteArray();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Convert from DataAtom to byte[] problem");
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return returnData;
    }

    public static class DataAtomFactory {
        public static DataAtom getDataAtomFromBytes(byte[] data) {
            DataAtom local = null;
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(bis);
                local = (DataAtom) in.readObject();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Convert from byte[] to DataAtom problem");
            } catch (ClassNotFoundException c) {
                //impossible situation
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                    // ignore close exception
                }
                try {
                    bis.close();
                } catch (IOException ex) {
                    // ignore close exception
                }
            }
         return local;
        }
    }
}

class BodyTemperature implements Serializable {
    double bodyTemperature;

    BodyTemperature(double bodyTemperature) {
        this.bodyTemperature = bodyTemperature;
    }
}

class Pulse implements Serializable {
    int pulse;

    Pulse(int pulse) {
        this.pulse = pulse;
    }
}

