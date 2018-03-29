package AcousticNetwork;

class ConcurrentDoubleBuffer{
	private double[] buf_;
	private int cap_;
	private boolean has_data_;
	int r_prt_;
	int w_prt_;
	public ConcurrentDoubleBuffer(){
		this(5000);
	}
	public ConcurrentDoubleBuffer(int cap){
		cap_  = cap;
		buf_ = new double[cap];
		r_prt_ = -1;
		w_prt_ = 0;
	}
	public void putDouble(double data){
		// Possible data race:
		// When w_prt_ is added, there is data, read will be executed.
		// After read this thread resumes and it realized that the pointers are the same
		// And raise a warning.
		if (w_prt_ == r_prt_){
			System.out.println("Warning: Buffer overflowed, all data in the buffer dumped.");
		}

		buf_[w_prt_] = data;
		w_prt_ = (w_prt_ + 1) % cap_;
	}
	public double getDouble(){
		while (!hasData()) {;} // Wait for data.
		double data = buf_[r_prt_];
		r_prt_ = (r_prt_ + 1) % cap_;
		return data;
	}
	public void write(double[] data, int offset, int length){
		for (int i=0; i<length; i++){
			if (i == data.length) { break; }
			putDouble(data[i + offset]);
		}
	}
	public double read(){
		return getDouble();
	}
	public boolean hasData(){ return r_prt_ != w_prt_ && r_prt_ != -1; }
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