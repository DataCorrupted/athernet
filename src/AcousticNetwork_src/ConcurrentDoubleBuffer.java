package AcousticNetwork;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

class ConcurrentDoubleBuffer{
	private final ReentrantReadWriteLock rwl_ = new ReentrantReadWriteLock();
	private final Lock w_ = rwl_.writeLock();
	private double[] buf_;
	private int cap_;
	private int r_prt_;
	private int w_prt_;
	private boolean has_data_;
	public ConcurrentDoubleBuffer(){
		this(5000);
	}
	public ConcurrentDoubleBuffer(int cap){
		cap_  = cap;
		buf_ = new double[cap];
		r_prt_ = 0;
		w_prt_ = 0;
		has_data_ = false;
	}
	public void putDouble(double data){
		if (w_prt_ == r_prt_ && has_data_){
			r_prt_ ++;
		}
		has_data_ = true;
		buf_[w_prt_] = data;
		w_prt_ = (w_prt_ + 1) % cap_;
	}
	public double getDouble(){
		double data = 0;
		if (has_data_){
			data = buf_[r_prt_];
			r_prt_ = (r_prt_ + 1) % cap_;
		}
		if (r_prt_ == w_prt_){
			has_data_ = false;
		}
		return data;
	}
	public void write(double[] data, int offset, int length){
		w_.lock();
		for (int i=0; i<length; i++){
			if (i == data.length) { break; }
			putDouble(data[i + offset]);
		}
		w_.unlock();
	}
	public double read(){
		// Read is also write operation as the buffer will 
		// change after each read.
		w_.lock();
		double data = getDouble();
		w_.unlock();
		return data;
	}
	public boolean hasData(){ return has_data_; }
}

/*
https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/locks/ReentrantReadWriteLock.html
class RWDictionary {
    private final Map<String, Data> m = new TreeMap<String, Data>();
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public Data get(String key) {
        r.lock();
        try { return m.get(key); }
        finally { r.unlock(); }
    }
    public String[] allKeys() {
        r.lock();
        try { return m.keySet().toArray(); }
        finally { r.unlock(); }
    }
    public Data put(String key, Data value) {
        w.lock();
        try { return m.put(key, value); }
        finally { w.unlock(); }
    }
    public void clear() {
        w.lock();
        try { m.clear(); }
        finally { w.unlock(); }
    }
 }*/